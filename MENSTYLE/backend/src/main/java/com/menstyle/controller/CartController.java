package com.menstyle.controller;

import com.menstyle.dto.request.*;
import com.menstyle.dto.response.*;
import com.menstyle.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {
    private final CartService service;
    public CartController(CartService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> get(Authentication auth) {
        return ok("Cart retrieved", service.get(auth.getName()));
    }
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> add(Authentication auth, @Valid @RequestBody AddCartItemRequest request) {
        return ok("Cart item added", service.add(auth.getName(), request));
    }
    @PatchMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> update(Authentication auth, @PathVariable Long itemId,
                                                             @Valid @RequestBody UpdateCartItemRequest request) {
        return ok("Cart item updated", service.update(auth.getName(), itemId, request));
    }
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> remove(Authentication auth, @PathVariable Long itemId) {
        return ok("Cart item removed", service.remove(auth.getName(), itemId));
    }
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clear(Authentication auth) {
        service.clear(auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null, 200, "/api/v1/cart"));
    }
    private ResponseEntity<ApiResponse<CartResponse>> ok(String message, CartResponse data) {
        return ResponseEntity.ok(ApiResponse.success(message, data, 200, "/api/v1/cart"));
    }
}
