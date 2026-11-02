package com.menstyle.service;

import com.menstyle.dto.request.CheckoutRequest;
import com.menstyle.dto.response.OrderResponse;
import com.menstyle.entity.*;
import com.menstyle.entity.enums.*;
import com.menstyle.exception.BadRequestException;
import com.menstyle.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {
    @Mock UserRepository users;
    @Mock CartRepository carts;
    @Mock CartItemRepository cartItems;
    @Mock InventoryRepository inventories;
    @Mock OrderRepository orders;
    @Mock OrderItemRepository orderItems;
    @Mock OrderAddressRepository addresses;
    @Mock OrderStatusHistoryRepository histories;
    @Mock PaymentRepository payments;
    @Mock InventoryTransactionRepository transactions;
    @Mock PlatformTransactionManager transactionManager;
    @Mock TransactionStatus transactionStatus;

    CheckoutService service;
    User user;
    Cart cart;
    ProductVariant variant;
    CartItem cartItem;
    Inventory inventory;
    CheckoutRequest request;

    @BeforeEach
    void setUp() {
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        user = new User();
        user.setId(7L);
        user.setEmail("customer@example.com");
        cart = new Cart();
        cart.setId(8L);
        cart.setUser(user);
        variant = new ProductVariant();
        variant.setId(29L);
        variant.setSku("MST-TEE-OVR-WHT-M");
        variant.setPrice(BigDecimal.valueOf(259000));
        Product product = new Product();
        product.setId(9L);
        product.setName("Áo Thun Oversize");
        product.setBasePrice(BigDecimal.valueOf(259000));
        variant.setProduct(product);
        Size size = new Size();
        size.setName("M");
        variant.setSize(size);
        Color color = new Color();
        color.setName("Trắng");
        variant.setColor(color);
        cartItem = new CartItem();
        cartItem.setId(10L);
        cartItem.setCart(cart);
        cartItem.setVariant(variant);
        cartItem.setQuantity(1);
        inventory = new Inventory();
        inventory.setId(100L);
        inventory.setVariant(variant);
        inventory.setQuantity(10);
        inventory.setReservedQuantity(0);
        request = new CheckoutRequest("Nguyen Van A", "0900000000", "Hà Nội", "Cầu Giấy",
                "Dịch Vọng", "1 Đường Mẫu", null, PaymentMethod.COD, "checkout-1");
        service = new CheckoutService(users, carts, cartItems, inventories, orders, orderItems,
                addresses, histories, payments, transactions, transactionManager);
    }

    @Test
    void firstCheckoutCreatesOneOrderAndDecrementsInventory() {
        stubCart();
        when(orders.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(101L);
            order.setCreatedAt(LocalDateTime.now());
            return order;
        });
        when(addresses.findByOrderId(101L)).thenReturn(Optional.of(address(request, 101L)));
        when(orderItems.findByOrderIdOrderById(101L)).thenReturn(List.of());

        OrderResponse result = service.checkout(user.getEmail(), request);

        assertEquals(101L, result.id());
        assertEquals(BigDecimal.valueOf(259000), result.totalAmount());
        assertEquals(9, inventory.getQuantity());
        verify(inventories).save(inventory);
        verify(orders, times(1)).save(any(Order.class));
        verify(cartItems).deleteAll(List.of(cartItem));
    }

    @Test
    void replayAfterCartWasDeletedReturnsOriginalOrderWithoutLockingInventory() {
        Order existing = existingOrder(101L, request);
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(orders.findByUserIdAndIdempotencyKey(7L, "checkout-1")).thenReturn(Optional.of(existing));
        when(addresses.findByOrderId(101L)).thenReturn(Optional.of(address(request, 101L)));
        when(orderItems.findByOrderIdOrderById(101L)).thenReturn(List.of());

        OrderResponse result = service.checkout(user.getEmail(), request);

        assertEquals(101L, result.id());
        verifyNoInteractions(carts, cartItems, inventories);
        verify(orders, never()).save(any());
    }

    @Test
    void sameKeyWithDifferentPayloadIsRejected() {
        Order existing = existingOrder(101L, request);
        CheckoutRequest different = new CheckoutRequest("Other Name", request.receiverPhone(), request.province(),
                request.district(), request.ward(), request.addressLine(), request.note(),
                PaymentMethod.COD, request.idempotencyKey());
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(orders.findByUserIdAndIdempotencyKey(7L, "checkout-1")).thenReturn(Optional.of(existing));
        when(addresses.findByOrderId(101L)).thenReturn(Optional.of(address(request, 101L)));

        assertThrows(BadRequestException.class, () -> service.checkout(user.getEmail(), different));
        verifyNoInteractions(carts, cartItems, inventories);
    }

    @Test
    void uniqueIndexRaceReplaysOrderFromNewTransaction() {
        Order existing = existingOrder(101L, request);
        stubCart();
        when(orders.save(any(Order.class))).thenThrow(new DataIntegrityViolationException("duplicate key"));
        when(orders.findByUserIdAndIdempotencyKey(7L, "checkout-1"))
                .thenReturn(Optional.empty(), Optional.of(existing));
        when(addresses.findByOrderId(101L)).thenReturn(Optional.of(address(request, 101L)));
        when(orderItems.findByOrderIdOrderById(101L)).thenReturn(List.of());

        OrderResponse result = service.checkout(user.getEmail(), request);

        assertEquals(101L, result.id());
        verify(orders, times(2)).findByUserIdAndIdempotencyKey(7L, "checkout-1");
        verify(orderItems, never()).save(any());
    }

    private void stubCart() {
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(orders.findByUserIdAndIdempotencyKey(7L, "checkout-1")).thenReturn(Optional.empty());
        when(carts.findByUserId(7L)).thenReturn(Optional.of(cart));
        when(cartItems.findByCartIdWithDetails(8L)).thenReturn(new ArrayList<>(List.of(cartItem)));
        when(inventories.findByVariantIdForUpdate(29L)).thenReturn(Optional.of(inventory));
    }

    private Order existingOrder(Long id, CheckoutRequest checkoutRequest) {
        Order order = new Order();
        order.setId(id);
        order.setCreatedAt(LocalDateTime.now());
        order.setUser(user);
        order.setIdempotencyKey(checkoutRequest.idempotencyKey());
        order.setCustomerNote(checkoutRequest.note());
        order.setPaymentMethod(PaymentMethod.COD);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setSubtotal(BigDecimal.valueOf(259000));
        order.setShippingFee(BigDecimal.ZERO);
        order.setTotalAmount(BigDecimal.valueOf(259000));
        return order;
    }

    private OrderAddress address(CheckoutRequest checkoutRequest, Long orderId) {
        OrderAddress address = new OrderAddress();
        address.setOrder(existingOrder(orderId, checkoutRequest));
        address.setReceiverName(checkoutRequest.receiverName());
        address.setReceiverPhone(checkoutRequest.receiverPhone());
        address.setProvince(checkoutRequest.province());
        address.setDistrict(checkoutRequest.district());
        address.setWard(checkoutRequest.ward());
        address.setAddressDetail(checkoutRequest.addressLine());
        return address;
    }
}
