package com.menstyle.controller;

import com.menstyle.dto.response.*;
import com.menstyle.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wishlist")
public class WishlistController {
    private final WishlistService service;
    public WishlistController(WishlistService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<ApiResponse<WishlistResponse>> get(Authentication auth) {
        return ok("Wishlist retrieved", service.get(auth.getName()));
    }
    @PostMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<WishlistResponse>> add(Authentication auth, @PathVariable Long productId) {
        return ok("Product added to wishlist", service.add(auth.getName(), productId));
    }
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<WishlistResponse>> remove(Authentication auth, @PathVariable Long productId) {
        return ok("Product removed from wishlist", service.remove(auth.getName(), productId));
    }
    private ResponseEntity<ApiResponse<WishlistResponse>> ok(String message, WishlistResponse data) {
        return ResponseEntity.ok(ApiResponse.success(message, data, 200, "/api/v1/wishlist"));
    }
}
