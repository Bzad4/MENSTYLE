package com.menstyle.controller;

import com.menstyle.dto.request.CheckoutRequest;
import com.menstyle.dto.response.*;
import com.menstyle.service.CheckoutService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class CheckoutController {
    private final CheckoutService service;

    public CheckoutController(CheckoutService service) {
        this.service = service;
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(Authentication auth,
                                                                @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Order created",
                service.checkout(auth.getName(), request), 200, "/api/v1/checkout"));
    }

}
