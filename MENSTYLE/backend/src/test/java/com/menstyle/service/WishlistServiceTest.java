package com.menstyle.service;

import com.menstyle.entity.*;
import com.menstyle.exception.ResourceNotFoundException;
import com.menstyle.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {
    @Mock UserRepository users;
    @Mock WishlistRepository wishlists;
    @Mock WishlistItemRepository items;
    @Mock ProductRepository products;
    @Mock ProductImageRepository images;
    @Mock InventoryRepository inventory;
    WishlistService service;

    @BeforeEach void setUp() { service = new WishlistService(users, wishlists, items, products, images, inventory); }

    @Test void addMissingProductReturnsNotFound() {
        User user = new User(); user.setId(1L);
        Wishlist wishlist = new Wishlist(); wishlist.setId(2L); wishlist.setUser(user);
        when(users.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(wishlists.findByUserId(1L)).thenReturn(Optional.of(wishlist));
        when(products.findActiveById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.add("customer@example.com", 99L));
    }

    @Test void addingDuplicateProductDoesNotSaveDuplicate() {
        User user = new User(); user.setId(1L);
        Wishlist wishlist = new Wishlist(); wishlist.setId(2L); wishlist.setUser(user);
        Product product = new Product(); product.setId(9L); product.setName("Polo"); product.setSlug("polo");
        Category category = new Category(); category.setId(1L); category.setName("Category"); category.setSlug("category");
        Brand brand = new Brand(); brand.setId(1L); brand.setName("Brand"); brand.setSlug("brand");
        product.setCategory(category); product.setBrand(brand);
        WishlistItem existing = new WishlistItem(); existing.setWishlist(wishlist); existing.setProduct(product);
        when(users.findByEmail("customer@example.com")).thenReturn(Optional.of(user));
        when(wishlists.findByUserId(1L)).thenReturn(Optional.of(wishlist));
        when(products.findActiveById(9L)).thenReturn(Optional.of(product));
        when(items.findByWishlistIdAndProductId(2L, 9L)).thenReturn(Optional.of(existing));
        when(items.findByWishlistIdWithDetails(2L)).thenReturn(List.of(existing));
        when(images.findByProductIdOrderBySortOrderAsc(anyLong())).thenReturn(List.of());
        when(inventory.findByProductIds(any())).thenReturn(List.of());
        service.add("customer@example.com", 9L);
        verify(items, never()).save(any());
    }
}
