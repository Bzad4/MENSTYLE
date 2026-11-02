package com.menstyle.controller;

import com.menstyle.dto.request.CancelOrderRequest;
import com.menstyle.dto.response.*;
import com.menstyle.entity.enums.OrderStatus;
import com.menstyle.service.OrderManagementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderManagementService service;

    public OrderController(OrderManagementService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryResponse>>> list(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved",
                service.list(authentication.getName(), status, page, size), 200, "/api/v1/orders"));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> detail(
            Authentication authentication, @PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success("Order retrieved",
                service.detail(authentication.getName(), orderId), 200, "/api/v1/orders/" + orderId));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> cancel(
            Authentication authentication, @PathVariable Long orderId,
            @Valid @RequestBody CancelOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Order cancelled",
                service.cancel(authentication.getName(), orderId, request), 200,
                "/api/v1/orders/" + orderId + "/cancel"));
    }
}
