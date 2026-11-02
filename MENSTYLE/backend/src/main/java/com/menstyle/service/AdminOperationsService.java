package com.menstyle.service;

import com.menstyle.dto.request.*;
import com.menstyle.dto.response.*;
import com.menstyle.entity.*;
import com.menstyle.entity.enums.*;
import com.menstyle.exception.*;
import com.menstyle.repository.*;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
public class AdminOperationsService {
    private final OrderRepository orders;
    private final OrderItemRepository items;
    private final OrderAddressRepository addresses;
    private final OrderStatusHistoryRepository histories;
    private final InventoryRepository inventories;
    private final InventoryTransactionRepository transactions;
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;

    public AdminOperationsService(OrderRepository orders, OrderItemRepository items, OrderAddressRepository addresses,
            OrderStatusHistoryRepository histories, InventoryRepository inventories,
            InventoryTransactionRepository transactions, UserRepository users, RoleRepository roles,
            PasswordEncoder passwordEncoder) {
        this.orders = orders; this.items = items; this.addresses = addresses; this.histories = histories;
        this.inventories = inventories; this.transactions = transactions; this.users = users; this.roles = roles;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminOrderResponse> orders(String keyword, OrderStatus status, int page, int size) {
        Page<Order> result = orders.searchAdmin(blank(keyword), status, pageRequest(page, size));
        return PageResponse.from(result.map(this::orderResponse));
    }

    @Transactional(readOnly = true)
    public AdminOrderResponse order(Long id) {
        return orderResponse(orders.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found")));
    }

    @Transactional
    public AdminOrderResponse transition(Long id, OrderStatus target, String note, String actorEmail) {
        User actor = user(actorEmail);
        Order order = orders.findByIdForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        OrderStatus current = order.getOrderStatus();
        boolean elevated = "ROLE_ADMIN".equals(actor.getRole().getName()) || "ROLE_SUPER_ADMIN".equals(actor.getRole().getName());
        if (!elevated && (target == OrderStatus.CANCELLED || target == OrderStatus.COMPLETED))
            throw new ForbiddenException("Insufficient permission for this order action");
        if (!allowed(current, target)) throw new ConflictException("Invalid order status transition");
        if (target == OrderStatus.CANCELLED) restock(order, actor, note);
        if (target == OrderStatus.COMPLETED && order.getPaymentMethod() == PaymentMethod.COD)
            order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(target);
        orders.save(order);
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order); history.setOldStatus(current); history.setNewStatus(target);
        history.setChangedBy(actor); history.setNote(note.trim()); histories.save(history);
        return orderResponse(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminCustomerResponse> customers(String keyword, int page, int size) {
        Page<User> result = users.searchCustomers(blank(keyword), pageRequest(page, size));
        return PageResponse.from(result.map(u -> new AdminCustomerResponse(u.getId(), u.getFullName(), u.getEmail(),
                u.getPhone(), u.getStatus(), ordersByUser(u.getId()), spentByUser(u.getId()), u.getCreatedAt())));
    }

    @Transactional
    public AdminCustomerResponse customerStatus(Long id, UserStatus status) {
        User user = users.findByIdForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        if (!"ROLE_CUSTOMER".equals(user.getRole().getName())) throw new BadRequestException("User is not a customer");
        user.setStatus(status); users.save(user);
        return new AdminCustomerResponse(user.getId(), user.getFullName(), user.getEmail(), user.getPhone(),
                user.getStatus(), ordersByUser(id), spentByUser(id), user.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> staff(int page, int size) {
        Page<User> result = users.findByRoleNameIn(List.of("ROLE_ADMIN", "ROLE_STAFF", "ROLE_SUPER_ADMIN"), pageRequest(page, size));
        return PageResponse.from(result.map(this::userResponse));
    }

    @Transactional
    public AdminUserResponse createStaff(AdminUserRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(email)) throw new ConflictException("Email already exists");
        Role role = roles.findByName("ROLE_" + request.role()).orElseThrow(() -> new BadRequestException("Role not found"));
        User user = new User(); user.setFullName(request.fullName().trim()); user.setEmail(email);
        user.setPhone(request.phone()); user.setRole(role); user.setStatus(UserStatus.ACTIVE);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmailVerified(false); return userResponse(users.save(user));
    }

    @Transactional
    public AdminUserResponse staffStatus(Long id, UserStatus status, String actorEmail) {
        User actor = user(actorEmail);
        if (!"ROLE_ADMIN".equals(actor.getRole().getName()) && !"ROLE_SUPER_ADMIN".equals(actor.getRole().getName()))
            throw new ForbiddenException("Insufficient permission");
        User target = users.findByIdForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if ("ROLE_SUPER_ADMIN".equals(target.getRole().getName()) || "ROLE_ADMIN".equals(target.getRole().getName()))
            throw new ForbiddenException("Protected account cannot be changed");
        if (!"ROLE_STAFF".equals(target.getRole().getName()))
            throw new BadRequestException("User is not staff");
        target.setStatus(status); return userResponse(users.save(target));
    }

    @Transactional
    public AdminInventoryResponse adjustInventory(Long variantId, InventoryAdjustmentRequest request, String actorEmail) {
        User actor = user(actorEmail);
        Inventory inventory = inventories.findByVariantIdForUpdate(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
        if (request.quantity() < inventory.getReservedQuantity()) throw new BadRequestException("Quantity cannot be below reserved stock");
        int delta = request.quantity() - inventory.getQuantity();
        inventory.setQuantity(request.quantity()); inventory.setReorderLevel(request.reorderLevel()); inventories.save(inventory);
        if (delta != 0) {
            InventoryTransaction tx = new InventoryTransaction(); tx.setVariant(inventory.getVariant());
            tx.setTransactionType(delta > 0 ? InventoryTransactionType.IMPORT : InventoryTransactionType.ADJUST);
            tx.setQuantity(Math.abs(delta)); tx.setReferenceType("ADMIN_ADJUSTMENT"); tx.setNote(request.note().trim()); tx.setCreatedBy(actor);
            transactions.save(tx);
        }
        return inventoryResponse(inventory);
    }

    @Transactional(readOnly = true)
    public List<AdminInventoryResponse> inventory(String keyword) {
        return inventories.searchAdmin(blank(keyword)).stream().map(this::inventoryResponse).toList();
    }

    private void restock(Order order, User actor, String note) {
        List<OrderItem> orderItems = items.findByOrderIdOrderById(order.getId()).stream()
                .sorted(Comparator.comparing(item -> item.getVariant().getId())).toList();
        for (OrderItem item : orderItems) {
            Inventory inventory = inventories.findByVariantIdForUpdate(item.getVariant().getId())
                    .orElseThrow(() -> new BadRequestException("Inventory not found"));
            inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
            inventories.save(inventory);
            InventoryTransaction tx = new InventoryTransaction(); tx.setVariant(item.getVariant());
            tx.setTransactionType(InventoryTransactionType.IMPORT); tx.setQuantity(item.getQuantity());
            tx.setReferenceType("ORDER_CANCEL"); tx.setReferenceId(order.getId()); tx.setNote(note.trim()); tx.setCreatedBy(actor);
            transactions.save(tx);
        }
    }

    private boolean allowed(OrderStatus from, OrderStatus to) {
        return switch (from) {
            case PENDING -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED -> to == OrderStatus.PACKING || to == OrderStatus.CANCELLED;
            case PACKING -> to == OrderStatus.SHIPPING || to == OrderStatus.CANCELLED;
            case SHIPPING -> to == OrderStatus.COMPLETED;
            default -> false;
        };
    }

    private AdminOrderResponse orderResponse(Order o) {
        OrderAddress a = addresses.findByOrderId(o.getId()).orElse(null);
        OrderResponse.AddressResponse address = a == null ? null : new OrderResponse.AddressResponse(a.getReceiverName(), a.getReceiverPhone(), a.getProvince(), a.getDistrict(), a.getWard(), a.getAddressDetail());
        List<OrderResponse.OrderItemResponse> orderItems = items.findByOrderIdOrderById(o.getId()).stream().map(i -> new OrderResponse.OrderItemResponse(i.getProductName(), i.getSku(), i.getSizeName(), i.getColorName(), i.getUnitPrice(), i.getQuantity(), i.getTotalPrice())).toList();
        List<OrderDetailResponse.StatusHistory> history = histories.findByOrderIdOrderByCreatedAtAsc(o.getId()).stream().map(h -> new OrderDetailResponse.StatusHistory(h.getOldStatus() == null ? null : h.getOldStatus().name(), h.getNewStatus().name(), h.getNote(), h.getCreatedAt())).toList();
        return new AdminOrderResponse(o.getId(), o.getOrderCode(), o.getUser().getFullName(), o.getUser().getEmail(), o.getOrderStatus().name(), o.getPaymentMethod().name(), o.getPaymentStatus().name(), o.getTotalAmount(), o.getCreatedAt(), address, orderItems, history);
    }
    private AdminInventoryResponse inventoryResponse(Inventory i) { return new AdminInventoryResponse(i.getId(), i.getVariant().getId(), i.getVariant().getProduct().getName(), i.getVariant().getSku(), i.getQuantity(), i.getReservedQuantity(), i.getQuantity() - i.getReservedQuantity(), i.getReorderLevel()); }
    private AdminUserResponse userResponse(User u) { return new AdminUserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole().getName(), u.getStatus(), u.getCreatedAt()); }
    private User user(String email) { return users.findByEmail(email).orElseThrow(() -> new UnauthorizedException("User not found")); }
    private long ordersByUser(Long id) { return orders.findByUserId(id, PageRequest.of(0, 1)).getTotalElements(); }
    private BigDecimal spentByUser(Long id) { return orders.findByUserId(id, PageRequest.of(0, 1000)).getContent().stream().filter(o -> o.getOrderStatus() != OrderStatus.CANCELLED).map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add); }
    private Pageable pageRequest(int page, int size) { if (page < 0 || size < 1 || size > 100) throw new BadRequestException("Invalid pagination"); return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))); }
    private String blank(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
