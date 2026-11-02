package com.menstyle.service;

import com.menstyle.dto.response.*;
import com.menstyle.entity.*;
import com.menstyle.exception.*;
import com.menstyle.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class WishlistService {
    private final UserRepository userRepository;
    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository itemRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final InventoryRepository inventoryRepository;

    public WishlistService(UserRepository userRepository, WishlistRepository wishlistRepository,
                           WishlistItemRepository itemRepository, ProductRepository productRepository,
                           ProductImageRepository imageRepository, InventoryRepository inventoryRepository) {
        this.userRepository = userRepository; this.wishlistRepository = wishlistRepository; this.itemRepository = itemRepository;
        this.productRepository = productRepository; this.imageRepository = imageRepository; this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public WishlistResponse get(String email) { return response(getOrCreate(email)); }

    @Transactional
    public WishlistResponse add(String email, Long productId) {
        Wishlist wishlist = getOrCreate(email);
        Product product = productRepository.findActiveById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (itemRepository.findByWishlistIdAndProductId(wishlist.getId(), productId).isEmpty()) {
            WishlistItem item = new WishlistItem(); item.setWishlist(wishlist); item.setProduct(product); itemRepository.save(item);
        }
        return response(wishlist);
    }

    @Transactional
    public WishlistResponse remove(String email, Long productId) {
        Wishlist wishlist = getOrCreate(email);
        itemRepository.findByWishlistIdAndProductId(wishlist.getId(), productId).ifPresent(itemRepository::delete);
        return response(wishlist);
    }

    private Wishlist getOrCreate(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UnauthorizedException("User not found"));
        return wishlistRepository.findByUserId(user.getId()).orElseGet(() -> {
            Wishlist wishlist = new Wishlist(); wishlist.setUser(user); return wishlistRepository.save(wishlist);
        });
    }

    private WishlistResponse response(Wishlist wishlist) {
        List<WishlistItemResponse> items = itemRepository.findByWishlistIdWithDetails(wishlist.getId()).stream().map(item -> {
            Product product = item.getProduct();
            String image = imageRepository.findByProductIdOrderBySortOrderAsc(product.getId()).stream()
                    .findFirst().map(ProductImage::getImageUrl).orElse(null);
            boolean inStock = inventoryRepository.findByProductIds(List.of(product.getId())).stream()
                    .anyMatch(i -> i.getQuantity() > i.getReservedQuantity());
            return WishlistItemResponse.from(product, image, inStock);
        }).toList();
        return new WishlistResponse(wishlist.getId(), items);
    }
}
