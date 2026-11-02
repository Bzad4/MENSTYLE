package com.menstyle.service;

import com.menstyle.dto.request.CancelOrderRequest;
import com.menstyle.dto.response.*;
import com.menstyle.entity.*;
import com.menstyle.entity.enums.*;
import com.menstyle.exception.*;
import com.menstyle.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderManagementService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderAddressRepository addressRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    public OrderManagementService(UserRepository userRepository, OrderRepository orderRepository,
                                  OrderItemRepository orderItemRepository, OrderAddressRepository addressRepository,
                                  OrderStatusHistoryRepository historyRepository, InventoryRepository inventoryRepository,
                                  InventoryTransactionRepository inventoryTransactionRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.addressRepository = addressRepository;
        this.historyRepository = historyRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> list(String email, OrderStatus status, int page, int size) {
        User user = user(email);
        if (page < 0 || size < 1 || size > 50) throw new BadRequestException("Invalid pagination");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")
                .and(Sort.by(Sort.Direction.DESC, "id")));
        Page<Order> orders = status == null
                ? orderRepository.findByUserId(user.getId(), pageable)
                : orderRepository.findByUserIdAndOrderStatus(user.getId(), status, pageable);
        List<Long> ids = orders.getContent().stream().map(Order::getId).toList();
        Map<Long, Integer> counts = orderItemRepository.findByOrderIdIn(ids).stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getId(),
                        Collectors.summingInt(OrderItem::getQuantity)));
        return PageResponse.from(orders.map(order ->
                OrderSummaryResponse.from(order, counts.getOrDefault(order.getId(), 0))));
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse detail(String email, Long orderId) {
        User user = user(email);
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return detailResponse(order);
    }

    @Transactional
    public OrderDetailResponse cancel(String email, Long orderId, CancelOrderRequest request) {
        User user = user(email);
        Order order = orderRepository.findByIdAndUserIdForUpdate(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new ConflictException("Only pending orders can be cancelled");
        }
        List<OrderItem> items = orderItemRepository.findByOrderIdOrderById(order.getId()).stream()
                .sorted(Comparator.comparing(item -> item.getVariant().getId()))
                .toList();
        for (OrderItem item : items) {
            Inventory inventory = inventoryRepository.findByVariantIdForUpdate(item.getVariant().getId())
                    .orElseThrow(() -> new BadRequestException("Inventory not found for order item"));
            inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
            inventoryRepository.save(inventory);
            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setVariant(item.getVariant());
            transaction.setTransactionType(InventoryTransactionType.IMPORT);
            transaction.setQuantity(item.getQuantity());
            transaction.setReferenceType("ORDER_CANCEL");
            transaction.setReferenceId(order.getId());
            transaction.setNote("Customer cancelled order");
            transaction.setCreatedBy(user);
            inventoryTransactionRepository.save(transaction);
        }
        OrderStatus oldStatus = order.getOrderStatus();
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancellationReason(request.reason().trim());
        orderRepository.save(order);
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setOldStatus(oldStatus);
        history.setNewStatus(OrderStatus.CANCELLED);
        history.setChangedBy(user);
        history.setNote(request.reason().trim());
        historyRepository.save(history);
        return detailResponse(order);
    }

    private User user(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private OrderDetailResponse detailResponse(Order order) {
        OrderAddress address = addressRepository.findByOrderId(order.getId()).orElse(null);
        OrderResponse.AddressResponse addressResponse = address == null ? null :
                new OrderResponse.AddressResponse(address.getReceiverName(), address.getReceiverPhone(),
                        address.getProvince(), address.getDistrict(), address.getWard(), address.getAddressDetail());
        List<OrderDetailResponse.Item> items = orderItemRepository.findByOrderIdOrderById(order.getId()).stream()
                .map(item -> new OrderDetailResponse.Item(item.getProductName(), item.getSku(), item.getSizeName(),
                        item.getColorName(), item.getUnitPrice(), item.getQuantity(), item.getTotalPrice())).toList();
        List<OrderDetailResponse.StatusHistory> history = historyRepository.findByOrderIdOrderByCreatedAtAsc(order.getId())
                .stream().map(item -> new OrderDetailResponse.StatusHistory(
                        item.getOldStatus() == null ? null : item.getOldStatus().name(),
                        item.getNewStatus().name(), item.getNote(), item.getCreatedAt())).toList();
        return new OrderDetailResponse(order.getId(), order.getOrderCode(), order.getCreatedAt(),
                order.getOrderStatus().name(), order.getPaymentMethod().name(), order.getPaymentStatus().name(),
                order.getSubtotal(), order.getDiscountAmount(), order.getShippingFee(), order.getTotalAmount(),
                order.getCancellationReason(), addressResponse, items, history);
    }
}
