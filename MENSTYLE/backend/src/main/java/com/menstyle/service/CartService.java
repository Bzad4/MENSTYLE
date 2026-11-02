package com.menstyle.service;

import com.menstyle.dto.request.*;
import com.menstyle.dto.response.*;
import com.menstyle.entity.*;
import com.menstyle.exception.*;
import com.menstyle.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
public class CartService {
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository itemRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductImageRepository imageRepository;

    public CartService(UserRepository userRepository, CartRepository cartRepository, CartItemRepository itemRepository,
                       ProductVariantRepository variantRepository, InventoryRepository inventoryRepository,
                       ProductImageRepository imageRepository) {
        this.userRepository = userRepository; this.cartRepository = cartRepository; this.itemRepository = itemRepository;
        this.variantRepository = variantRepository; this.inventoryRepository = inventoryRepository; this.imageRepository = imageRepository;
    }

    @Transactional
    public CartResponse get(String email) {
        return response(getOrCreate(email));
    }

    @Transactional
    public CartResponse add(String email, AddCartItemRequest request) {
        Cart cart = getOrCreate(email);
        ProductVariant variant = variantRepository.findActiveById(request.variantId())
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found"));
        int stock = stock(variant.getId());
        CartItem item = itemRepository.findByCartIdAndVariantId(cart.getId(), variant.getId()).orElse(null);
        int quantity = request.quantity() + (item == null ? 0 : item.getQuantity());
        ensureStock(quantity, stock);
        if (item == null) { item = new CartItem(); item.setCart(cart); item.setVariant(variant); }
        item.setQuantity(quantity); itemRepository.save(item);
        return response(cart);
    }

    @Transactional
    public CartResponse update(String email, Long itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreate(email);
        CartItem item = itemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        ensureStock(request.quantity(), stock(item.getVariant().getId()));
        item.setQuantity(request.quantity()); itemRepository.save(item);
        return response(cart);
    }

    @Transactional
    public CartResponse remove(String email, Long itemId) {
        Cart cart = getOrCreate(email);
        CartItem item = itemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        itemRepository.delete(item);
        return response(cart);
    }

    @Transactional
    public void clear(String email) {
        Cart cart = getOrCreate(email);
        itemRepository.deleteAll(itemRepository.findByCartIdWithDetails(cart.getId()));
    }

    private Cart getOrCreate(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UnauthorizedException("User not found"));
        return cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart cart = new Cart(); cart.setUser(user); return cartRepository.save(cart);
        });
    }

    private int stock(Long variantId) {
        return inventoryRepository.findByVariantId(variantId)
                .map(i -> Math.max(0, i.getQuantity() - i.getReservedQuantity())).orElse(0);
    }

    private void ensureStock(int quantity, int stock) {
        if (quantity < 1) throw new BadRequestException("Quantity must be at least 1");
        if (quantity > stock) throw new BadRequestException("Requested quantity exceeds available stock");
    }

    private CartResponse response(Cart cart) {
        List<CartItemResponse> items = itemRepository.findByCartIdWithDetails(cart.getId()).stream().map(item -> {
            int stock = stock(item.getVariant().getId());
            String image = imageRepository.findByProductIdOrderBySortOrderAsc(item.getVariant().getProduct().getId())
                    .stream().findFirst().map(ProductImage::getImageUrl).orElse(null);
            return CartItemResponse.from(item, image, stock);
        }).toList();
        int total = items.stream().mapToInt(CartItemResponse::quantity).sum();
        BigDecimal subtotal = items.stream().map(i -> i.lineTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(cart.getId(), items, total, subtotal);
    }
}
