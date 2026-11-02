package com.menstyle.controller;

import com.menstyle.dto.response.*;
import com.menstyle.service.ProductCatalogService;
import com.menstyle.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
public class PublicCatalogController {
    private final ProductCatalogService catalogService;

    public PublicCatalogController(ProductCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<PageResponse<ProductListItemResponse>>> products(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) String keyword, @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId, @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice, @RequestParam(required = false) String sizeFilter,
            @RequestParam(required = false) String color, @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "newest") String sort) {
        if (page < 0 || pageSize < 1 || pageSize > 50) throw new BadRequestException("Invalid pagination");
        if (minPrice != null && minPrice.signum() < 0) throw new BadRequestException("minPrice must be non-negative");
        if (maxPrice != null && maxPrice.signum() < 0) throw new BadRequestException("maxPrice must be non-negative");
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) throw new BadRequestException("minPrice must not exceed maxPrice");
        PageResponse<ProductListItemResponse> data = catalogService.list(keyword, categoryId, brandId, minPrice, maxPrice,
                sizeFilter, color, inStock, page, pageSize, sort);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved", data, 200, "/api/v1/public/products"));
    }

    @GetMapping("/products/{id:\\d+}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> product(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Product retrieved", catalogService.detail(id), 200,
                "/api/v1/public/products/" + id));
    }

    @GetMapping("/products/slug/{slug}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> productBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success("Product retrieved", catalogService.detailBySlug(slug), 200,
                "/api/v1/public/products/slug/" + slug));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategorySummaryResponse>>> categories() {
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved", catalogService.categories(), 200,
                "/api/v1/public/categories"));
    }

    @GetMapping("/brands")
    public ResponseEntity<ApiResponse<List<BrandSummaryResponse>>> brands() {
        return ResponseEntity.ok(ApiResponse.success("Brands retrieved", catalogService.brands(), 200,
                "/api/v1/public/brands"));
    }
}
