package com.menstyle.service;

import com.menstyle.dto.request.CheckoutRequest;
import com.menstyle.dto.response.OrderResponse;
import com.menstyle.entity.*;
import com.menstyle.entity.enums.*;
import com.menstyle.exception.*;
import com.menstyle.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CheckoutService {
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderAddressRepository orderAddressRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final PaymentRepository paymentRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final TransactionTemplate transactionTemplate;

    public CheckoutService(UserRepository userRepository, CartRepository cartRepository,
                           CartItemRepository cartItemRepository, InventoryRepository inventoryRepository,
                           OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                           OrderAddressRepository orderAddressRepository,
                           OrderStatusHistoryRepository historyRepository, PaymentRepository paymentRepository,
                           InventoryTransactionRepository inventoryTransactionRepository,
                           PlatformTransactionManager transactionManager) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderAddressRepository = orderAddressRepository;
        this.historyRepository = historyRepository;
        this.paymentRepository = paymentRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public OrderResponse checkout(String email, CheckoutRequest request) {
        try {
            return transactionTemplate.execute(status -> checkoutInTransaction(email, request));
        } catch (DataIntegrityViolationException exception) {
            OrderResponse replay = transactionTemplate.execute(status -> findReplay(email, request));
            if (replay != null) return replay;
            throw exception;
        }
    }

    private OrderResponse checkoutInTransaction(String email, CheckoutRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        if (request.paymentMethod() != PaymentMethod.COD) {
            throw new BadRequestException("Only COD payment is supported");
        }
        Optional<Order> existing = orderRepository.findByUserIdAndIdempotencyKey(user.getId(), request.idempotencyKey());
        if (existing.isPresent()) return replay(existing.get(), request);

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Cart is empty"));
        List<CartItem> items = new ArrayList<>(cartItemRepository.findByCartIdWithDetails(cart.getId()));
        if (items.isEmpty()) throw new BadRequestException("Cart is empty");
        items.sort(Comparator.comparing(item -> item.getVariant().getId()));

        Map<Long, Inventory> lockedInventory = new LinkedHashMap<>();
        List<Long> variantIds = items.stream().map(item -> item.getVariant().getId()).distinct().toList();
        for (Long variantId : variantIds) {
            Inventory inventory = inventoryRepository.findByVariantIdForUpdate(variantId)
                    .orElseThrow(() -> new BadRequestException("Product is out of stock"));
            lockedInventory.put(variantId, inventory);
        }
        for (CartItem item : items) {
            Inventory inventory = lockedInventory.get(item.getVariant().getId());
            int available = inventory.getQuantity() - inventory.getReservedQuantity();
            if (item.getQuantity() > available) {
                throw new BadRequestException("Insufficient stock for " + item.getVariant().getProduct().getName());
            }
        }

        BigDecimal subtotal = items.stream()
                .map(item -> price(item).multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setIdempotencyKey(request.idempotencyKey());
        order.setUser(user);
        order.setSubtotal(subtotal);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setShippingFee(BigDecimal.ZERO);
        order.setTotalAmount(subtotal);
        order.setPaymentMethod(PaymentMethod.COD);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setCustomerNote(request.note());
        orderRepository.save(order);

        OrderAddress address = new OrderAddress();
        address.setOrder(order);
        address.setReceiverName(request.receiverName().trim());
        address.setReceiverPhone(request.receiverPhone().trim());
        address.setProvince(request.province().trim());
        address.setDistrict(request.district().trim());
        address.setWard(request.ward().trim());
        address.setAddressDetail(request.addressLine().trim());
        orderAddressRepository.save(address);

        for (CartItem item : items) {
            ProductVariant variant = item.getVariant();
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setVariant(variant);
            orderItem.setProductName(variant.getProduct().getName());
            orderItem.setSku(variant.getSku());
            orderItem.setSizeName(variant.getSize().getName());
            orderItem.setColorName(variant.getColor().getName());
            orderItem.setUnitPrice(price(item));
            orderItem.setQuantity(item.getQuantity());
            orderItem.setTotalPrice(price(item).multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItemRepository.save(orderItem);

            Inventory inventory = lockedInventory.get(variant.getId());
            inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
            inventoryRepository.save(inventory);
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setVariant(variant);
            transaction.setTransactionType(InventoryTransactionType.EXPORT);
            transaction.setQuantity(item.getQuantity());
            transaction.setReferenceType("ORDER");
            transaction.setReferenceId(order.getId());
            transaction.setNote("COD checkout");
            inventoryTransactionRepository.save(transaction);
        }

        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setNewStatus(OrderStatus.PENDING);
        history.setNote("Order created");
        historyRepository.save(history);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(PaymentMethod.COD);
        payment.setAmount(subtotal);
        payment.setPaymentStatus(PaymentStatus.UNPAID);
        paymentRepository.save(payment);

        cartItemRepository.deleteAll(items);
        return response(order);
    }

    private OrderResponse findReplay(String email, CheckoutRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        return orderRepository.findByUserIdAndIdempotencyKey(user.getId(), request.idempotencyKey())
                .map(order -> replay(order, request))
                .orElse(null);
    }

    private OrderResponse replay(Order order, CheckoutRequest request) {
        OrderAddress address = orderAddressRepository.findByOrderId(order.getId()).orElse(null);
        if (address == null || !same(address.getReceiverName(), request.receiverName())
                || !same(address.getReceiverPhone(), request.receiverPhone())
                || !same(address.getProvince(), request.province())
                || !same(address.getDistrict(), request.district())
                || !same(address.getWard(), request.ward())
                || !same(address.getAddressDetail(), request.addressLine())
                || !same(order.getCustomerNote(), request.note())
                || order.getPaymentMethod() != request.paymentMethod()) {
            throw new BadRequestException("Idempotency key was already used with a different checkout request");
        }
        return response(order);
    }

    private boolean same(String first, String second) {
        return Objects.equals(first == null ? null : first.trim(), second == null ? null : second.trim());
    }

    public OrderResponse getOrder(String email, Long orderId) {
        return transactionTemplate.execute(status -> {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UnauthorizedException("User not found"));
            Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
            return response(order);
        });
    }

    private BigDecimal price(CartItem item) {
        return item.getVariant().getPrice() != null
                ? item.getVariant().getPrice()
                : item.getVariant().getProduct().getBasePrice();
    }

    private String generateOrderCode() {
        return "MST-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
    }

    private OrderResponse response(Order order) {
        OrderAddress address = orderAddressRepository.findByOrderId(order.getId()).orElse(null);
        List<OrderResponse.OrderItemResponse> items = orderItemRepository.findByOrderIdOrderById(order.getId())
                .stream().map(item -> new OrderResponse.OrderItemResponse(item.getProductName(), item.getSku(),
                        item.getSizeName(), item.getColorName(), item.getUnitPrice(), item.getQuantity(),
                        item.getTotalPrice())).toList();
        OrderResponse.AddressResponse addressResponse = address == null ? null :
                new OrderResponse.AddressResponse(address.getReceiverName(), address.getReceiverPhone(),
                        address.getProvince(), address.getDistrict(), address.getWard(), address.getAddressDetail());
        return new OrderResponse(order.getId(), order.getOrderCode(), order.getOrderStatus().name(),
                order.getSubtotal(), order.getShippingFee(), order.getTotalAmount(),
                order.getPaymentMethod().name(), order.getPaymentStatus().name(), order.getCreatedAt(),
                addressResponse, items);
    }
}
