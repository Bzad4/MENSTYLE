package com.menstyle.controller;

import com.menstyle.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        Map<String, String> data = Map.of("status", "UP");
        return ResponseEntity.ok(ApiResponse.success(
                "MENSTYLE backend is running", data, 200, "/api/v1/public/health"));
    }
}
