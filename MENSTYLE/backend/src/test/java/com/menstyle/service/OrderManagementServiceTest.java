package com.menstyle.service;

import com.menstyle.dto.request.CancelOrderRequest;
import com.menstyle.entity.*;
import com.menstyle.entity.enums.*;
import com.menstyle.exception.ConflictException;
import com.menstyle.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderManagementServiceTest {
    @Mock UserRepository users;
    @Mock OrderRepository orders;
    @Mock OrderItemRepository items;
    @Mock OrderAddressRepository addresses;
    @Mock OrderStatusHistoryRepository histories;
    @Mock InventoryRepository inventories;
    @Mock InventoryTransactionRepository transactions;
    OrderManagementService service;
    User user;
    Order order;
    OrderItem item;
    Inventory inventory;

    @BeforeEach
    void setUp() {
        service = new OrderManagementService(users, orders, items, addresses, histories, inventories, transactions);
        user = new User(); user.setId(3L); user.setEmail("customer@example.com");
        order = new Order(); order.setId(20L); order.setOrderCode("MST-1"); order.setUser(user);
        order.setOrderStatus(OrderStatus.PENDING); order.setPaymentMethod(PaymentMethod.COD);
        order.setPaymentStatus(PaymentStatus.UNPAID); order.setSubtotal(BigDecimal.TEN);
        order.setDiscountAmount(BigDecimal.ZERO); order.setShippingFee(BigDecimal.ZERO);
        order.setTotalAmount(BigDecimal.TEN);
        ProductVariant variant = new ProductVariant(); variant.setId(29L); variant.setSku("SKU");
        Product product = new Product(); product.setName("Tee"); product.setBasePrice(BigDecimal.TEN); variant.setProduct(product);
        item = new OrderItem(); item.setId(21L); item.setOrder(order); item.setVariant(variant);
        item.setProductName("Tee"); item.setSku("SKU"); item.setSizeName("M"); item.setColorName("Trắng");
        item.setUnitPrice(BigDecimal.TEN); item.setQuantity(2); item.setTotalPrice(BigDecimal.valueOf(20));
        inventory = new Inventory(); inventory.setId(30L); inventory.setVariant(variant);
        inventory.setQuantity(5); inventory.setReservedQuantity(0);
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(orders.findByIdAndUserIdForUpdate(20L, 3L)).thenReturn(Optional.of(order));
        lenient().when(items.findByOrderIdOrderById(20L)).thenReturn(List.of(item));
        lenient().when(inventories.findByVariantIdForUpdate(29L)).thenReturn(Optional.of(inventory));
        lenient().when(addresses.findByOrderId(20L)).thenReturn(Optional.empty());
        lenient().when(histories.findByOrderIdOrderByCreatedAtAsc(20L)).thenReturn(List.of());
    }

    @Test
    void cancelPendingOrderRestocksAndAuditsOnce() {
        var result = service.cancel(user.getEmail(), 20L, new CancelOrderRequest("Đổi ý"));

        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
        assertEquals("Đổi ý", order.getCancellationReason());
        assertEquals(7, inventory.getQuantity());
        verify(transactions).save(argThat(transaction ->
                transaction.getTransactionType() == InventoryTransactionType.IMPORT
                        && transaction.getQuantity() == 2
                        && transaction.getReferenceId().equals(20L)));
        verify(histories).save(argThat(history ->
                history.getOldStatus() == OrderStatus.PENDING
                        && history.getNewStatus() == OrderStatus.CANCELLED));
        assertEquals("CANCELLED", result.status());
    }

    @Test
    void secondCancellationIsRejectedWithoutRestockingAgain() {
        order.setOrderStatus(OrderStatus.CANCELLED);

        assertThrows(ConflictException.class,
                () -> service.cancel(user.getEmail(), 20L, new CancelOrderRequest("Lần hai")));

        assertEquals(5, inventory.getQuantity());
        verifyNoInteractions(inventories, transactions, histories);
    }

    @Test
    void anotherCustomerCannotCancelOrder() {
        when(orders.findByIdAndUserIdForUpdate(20L, 3L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.cancel(user.getEmail(), 20L, new CancelOrderRequest("Không phải đơn của tôi")));
        verifyNoInteractions(inventories, transactions);
    }
}
