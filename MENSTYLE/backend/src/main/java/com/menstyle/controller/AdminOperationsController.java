package com.menstyle.controller;

import com.menstyle.dto.request.*;
import com.menstyle.dto.response.*;
import com.menstyle.entity.enums.*;
import com.menstyle.service.AdminOperationsService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminOperationsController {
    private final AdminOperationsService service;
    public AdminOperationsController(AdminOperationsService service) { this.service = service; }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<PageResponse<AdminOrderResponse>>> orders(
            @RequestParam(required = false) String keyword, @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ok("Orders retrieved", service.orders(keyword, status, page, size), "/api/v1/admin/orders");
    }
    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<AdminOrderResponse>> order(@PathVariable Long id) {
        return ok("Order retrieved", service.order(id), "/api/v1/admin/orders/" + id);
    }
    @PatchMapping("/orders/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','STAFF')")
    public ResponseEntity<ApiResponse<AdminOrderResponse>> status(@PathVariable Long id, @RequestParam OrderStatus value,
            @Valid @RequestBody AdminOrderActionRequest request, Authentication authentication) {
        return ok("Order status updated", service.transition(id, value, request.note(), authentication.getName()), "/api/v1/admin/orders/" + id + "/status");
    }

    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<PageResponse<AdminCustomerResponse>>> customers(
            @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ok("Customers retrieved", service.customers(keyword, page, size), "/api/v1/admin/customers");
    }
    @PatchMapping("/customers/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminCustomerResponse>> customerStatus(@PathVariable Long id, @RequestParam UserStatus value) {
        return ok("Customer status updated", service.customerStatus(id, value), "/api/v1/admin/customers/" + id + "/status");
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AdminUserResponse>>> users(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ok("Staff retrieved", service.staff(page, size), "/api/v1/admin/users");
    }
    @PostMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminUserResponse>> createUser(@Valid @RequestBody AdminUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Staff created", service.createStaff(request), 201, "/api/v1/admin/users"));
    }
    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminUserResponse>> userStatus(@PathVariable Long id, @RequestParam UserStatus value, Authentication authentication) {
        return ok("User status updated", service.staffStatus(id, value, authentication.getName()), "/api/v1/admin/users/" + id + "/status");
    }

    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<List<AdminInventoryResponse>>> inventory(@RequestParam(required = false) String keyword) {
        return ok("Inventory retrieved", service.inventory(keyword), "/api/v1/admin/inventory");
    }
    @PutMapping("/inventory/{variantId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminInventoryResponse>> adjustInventory(@PathVariable Long variantId,
            @Valid @RequestBody InventoryAdjustmentRequest request, Authentication authentication) {
        return ok("Inventory adjusted", service.adjustInventory(variantId, request, authentication.getName()), "/api/v1/admin/inventory/" + variantId);
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(String message, T data, String path) {
        return ResponseEntity.ok(ApiResponse.success(message, data, 200, path));
    }
}
