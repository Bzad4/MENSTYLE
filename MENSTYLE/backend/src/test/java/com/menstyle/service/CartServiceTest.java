package com.menstyle.service;

import com.menstyle.dto.request.AddCartItemRequest;
import com.menstyle.entity.*;
import com.menstyle.exception.BadRequestException;
import com.menstyle.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    @Mock UserRepository users;
    @Mock CartRepository carts;
    @Mock CartItemRepository items;
    @Mock ProductVariantRepository variants;
    @Mock InventoryRepository inventory;
    @Mock ProductImageRepository images;
    CartService service;

    @BeforeEach void setUp() { service = new CartService(users, carts, items, variants, inventory, images); }

    @Test void addRejectsInsufficientStock() {
        User user = new User(); user.setId(1L);
        Cart cart = new Cart(); cart.setId(2L); cart.setUser(user);
        ProductVariant variant = variant(3L);
        when(users.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(carts.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(variants.findActiveById(3L)).thenReturn(Optional.of(variant));
        Inventory stock = new Inventory(); stock.setVariant(variant); stock.setQuantity(1); stock.setReservedQuantity(0);
        when(inventory.findByVariantId(3L)).thenReturn(Optional.of(stock));
        assertThrows(BadRequestException.class, () -> service.add("customer@example.com", new AddCartItemRequest(3L, 2)));
    }

    @Test void addExistingVariantIncrementsQuantity() {
        User user = new User(); user.setId(1L);
        Cart cart = new Cart(); cart.setId(2L); cart.setUser(user);
        ProductVariant variant = variant(3L);
        CartItem existing = new CartItem(); existing.setId(4L); existing.setCart(cart); existing.setVariant(variant); existing.setQuantity(1);
        Inventory stock = new Inventory(); stock.setVariant(variant); stock.setQuantity(5); stock.setReservedQuantity(0);
        when(users.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(carts.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(variants.findActiveById(3L)).thenReturn(Optional.of(variant));
        when(inventory.findByVariantId(3L)).thenReturn(Optional.of(stock));
        when(items.findByCartIdAndVariantId(2L, 3L)).thenReturn(Optional.of(existing));
        when(items.findByCartIdWithDetails(2L)).thenReturn(List.of(existing));
        when(images.findByProductIdOrderBySortOrderAsc(anyLong())).thenReturn(List.of());
        service.add("customer@example.com", new AddCartItemRequest(3L, 2));
        assertEquals(3, existing.getQuantity());
        verify(items).save(existing);
    }

    @Test void addCatalogOversizeVariantUsesInventoryByVariantId() {
        assertCatalogVariantCanBeAdded("ao-thun-oversize", 29L);
    }

    @Test void addCatalogBomberVariantUsesInventoryByVariantId() {
        assertCatalogVariantCanBeAdded("ao-khoac-bomber", 38L);
    }

    private void assertCatalogVariantCanBeAdded(String slug, Long variantId) {
        User user = new User(); user.setId(1L);
        Cart cart = new Cart(); cart.setId(2L); cart.setUser(user);
        ProductVariant variant = variant(variantId);
        variant.getProduct().setSlug(slug);
        Inventory stock = new Inventory(); stock.setId(500L); stock.setVariant(variant);
        stock.setQuantity(10); stock.setReservedQuantity(0);
        when(users.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(carts.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(variants.findActiveById(variantId)).thenReturn(Optional.of(variant));
        when(inventory.findByVariantId(variantId)).thenReturn(Optional.of(stock));
        when(items.findByCartIdAndVariantId(2L, variantId)).thenReturn(Optional.empty());
        when(items.findByCartIdWithDetails(2L)).thenReturn(List.of());

        service.add("customer@example.com", new AddCartItemRequest(variantId, 1));

        ArgumentCaptor<CartItem> saved = ArgumentCaptor.forClass(CartItem.class);
        verify(items).save(saved.capture());
        assertEquals(variantId, saved.getValue().getVariant().getId());
        assertEquals(1, saved.getValue().getQuantity());
    }

    private ProductVariant variant(Long id) {
        Product product = new Product(); product.setId(8L); product.setName("Polo"); product.setSlug("polo"); product.setBasePrice(BigDecimal.TEN);
        Size size = new Size(); size.setName("M"); Color color = new Color(); color.setName("Black");
        ProductVariant variant = new ProductVariant(); variant.setId(id); variant.setProduct(product); variant.setSize(size); variant.setColor(color);
        variant.setSku("POLO-M-BLK"); variant.setPrice(BigDecimal.TEN); return variant;
    }
}
