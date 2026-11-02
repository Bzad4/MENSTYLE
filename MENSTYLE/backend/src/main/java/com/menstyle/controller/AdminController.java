package com.menstyle.controller;

import com.menstyle.dto.request.ChangePasswordRequest;
import com.menstyle.dto.request.UpdateAdminProfileRequest;
import com.menstyle.dto.response.ApiResponse;
import com.menstyle.dto.response.UserResponse;
import com.menstyle.service.AdminAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final AdminAccountService adminAccountService;

    public AdminController(AdminAccountService adminAccountService) {
        this.adminAccountService = adminAccountService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Admin profile", adminAccountService.getProfile(authentication),
                200, "/api/v1/admin/me"));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            Authentication authentication, @Valid @RequestBody UpdateAdminProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Admin profile updated",
                adminAccountService.updateProfile(authentication, request), 200, "/api/v1/admin/me"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        adminAccountService.changePassword(authentication, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null,
                200, "/api/v1/admin/change-password"));
    }

    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<Map<String, String>>> ping() {
        return ResponseEntity.ok(ApiResponse.success("Admin access granted", Map.of("status", "OK"), 200,
                "/api/v1/admin/ping"));
    }
}
