package com.menstyle.controller;

import com.menstyle.dto.request.*;
import com.menstyle.dto.response.*;
import com.menstyle.service.AdminCatalogService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.menstyle.service.MediaStorageService;
import com.menstyle.repository.SizeRepository;
import com.menstyle.repository.ColorRepository;
import com.menstyle.entity.enums.ProductStatus;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminCatalogController {
    private final AdminCatalogService service;
    private final MediaStorageService mediaStorage;
    private final SizeRepository sizes;
    private final ColorRepository colors;
    public AdminCatalogController(AdminCatalogService service, MediaStorageService mediaStorage, SizeRepository sizes, ColorRepository colors) { this.service = service; this.mediaStorage = mediaStorage; this.sizes = sizes; this.colors = colors; }

    @GetMapping("/sizes")
    public ResponseEntity<ApiResponse<List<OptionResponse>>> sizes() {
        return ok("Sizes retrieved", sizes.findByStatusOrderByNameAsc(ProductStatus.ACTIVE).stream().map(s -> new OptionResponse(s.getId(), s.getName())).toList(), "/api/v1/admin/sizes");
    }
    @GetMapping("/colors")
    public ResponseEntity<ApiResponse<List<OptionResponse>>> colors() {
        return ok("Colors retrieved", colors.findByStatusOrderByNameAsc(ProductStatus.ACTIVE).stream().map(c -> new OptionResponse(c.getId(), c.getName())).toList(), "/api/v1/admin/colors");
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<PageResponse<AdminProductResponse>>> products(
            @RequestParam(required = false) String keyword, @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId, @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        return ok("Products retrieved", service.products(keyword, categoryId, brandId, status, page, size, sort), "/api/v1/admin/products");
    }
    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<AdminProductResponse>> product(@PathVariable Long id) {
        return ok("Product retrieved", service.product(id), "/api/v1/admin/products/" + id);
    }
    @PostMapping("/products")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminProductResponse>> createProduct(@Valid @RequestBody AdminProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Product created", service.createProduct(request), 201, "/api/v1/admin/products"));
    }
    @PutMapping("/products/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminProductResponse>> updateProduct(@PathVariable Long id, @Valid @RequestBody AdminProductRequest request) {
        return ok("Product updated", service.updateProduct(id, request), "/api/v1/admin/products/" + id);
    }
    @PatchMapping("/products/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminProductResponse>> productStatus(@PathVariable Long id, @RequestParam String value) {
        return ok("Product status updated", service.setProductStatus(id, value), "/api/v1/admin/products/" + id + "/status");
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<PageResponse<AdminCategoryResponse>>> categories(@RequestParam(required = false) String keyword,
                                                                                         @RequestParam(defaultValue = "0") int page,
                                                                                         @RequestParam(defaultValue = "50") int size) {
        return ok("Categories retrieved", service.categories(keyword, page, size), "/api/v1/admin/categories");
    }
    @PostMapping("/categories") @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminCategoryResponse>> createCategory(@Valid @RequestBody AdminCategoryRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Category created", service.createCategory(r), 201, "/api/v1/admin/categories"));
    }
    @PutMapping("/categories/{id}") @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminCategoryResponse>> updateCategory(@PathVariable Long id, @Valid @RequestBody AdminCategoryRequest r) {
        return ok("Category updated", service.updateCategory(id, r), "/api/v1/admin/categories/" + id);
    }
    @PatchMapping("/categories/{id}/status") @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminCategoryResponse>> categoryStatus(@PathVariable Long id, @RequestParam String value) {
        return ok("Category status updated", service.setCategoryStatus(id, value), "/api/v1/admin/categories/" + id + "/status");
    }

    @GetMapping("/brands")
    public ResponseEntity<ApiResponse<PageResponse<AdminBrandResponse>>> brands(@RequestParam(required = false) String keyword,
                                                                                  @RequestParam(defaultValue = "0") int page,
                                                                                  @RequestParam(defaultValue = "50") int size) {
        return ok("Brands retrieved", service.brands(keyword, page, size), "/api/v1/admin/brands");
    }
    @PostMapping("/brands") @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminBrandResponse>> createBrand(@Valid @RequestBody AdminBrandRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Brand created", service.createBrand(r), 201, "/api/v1/admin/brands"));
    }
    @PutMapping("/brands/{id}") @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminBrandResponse>> updateBrand(@PathVariable Long id, @Valid @RequestBody AdminBrandRequest r) {
        return ok("Brand updated", service.updateBrand(id, r), "/api/v1/admin/brands/" + id);
    }
    @PatchMapping("/brands/{id}/status") @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminBrandResponse>> brandStatus(@PathVariable Long id, @RequestParam String value) {
        return ok("Brand status updated", service.setBrandStatus(id, value), "/api/v1/admin/brands/" + id + "/status");
    }

    @PostMapping("/products/{productId}/variants") @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminVariantResponse>> createVariant(@PathVariable Long productId, @Valid @RequestBody AdminVariantRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Variant created", service.createVariant(productId, r), 201, "/api/v1/admin/products/" + productId + "/variants"));
    }
    @PutMapping("/products/{productId}/variants/{variantId}") @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminVariantResponse>> updateVariant(@PathVariable Long productId, @PathVariable Long variantId, @Valid @RequestBody AdminVariantRequest r) {
        return ok("Variant updated", service.updateVariant(productId, variantId, r), "/api/v1/admin/products/" + productId + "/variants/" + variantId);
    }
    @PatchMapping("/products/{productId}/variants/{variantId}/status") @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminVariantResponse>> variantStatus(@PathVariable Long productId, @PathVariable Long variantId, @RequestParam String value) {
        return ok("Variant status updated", service.setVariantStatus(productId, variantId, value), "/api/v1/admin/products/" + productId + "/variants/" + variantId + "/status");
    }
    @PostMapping("/products/{productId}/images") @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> addImage(@PathVariable Long productId, @RequestParam String imageUrl,
                                                         @RequestParam(defaultValue = "false") boolean primary,
                                                         @RequestParam(defaultValue = "0") int sortOrder) {
        return ok("Image added", service.addImage(productId, imageUrl, primary, sortOrder), "/api/v1/admin/products/" + productId + "/images");
    }
    @PostMapping(value = "/products/{productId}/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> uploadImage(@PathVariable Long productId, @RequestPart("file") MultipartFile file,
                                                             @RequestParam(defaultValue = "false") boolean primary,
                                                             @RequestParam(defaultValue = "0") int sortOrder) {
        String url = mediaStorage.storeImage(file);
        return ok("Image uploaded", service.addImage(productId, url, primary, sortOrder), "/api/v1/admin/products/" + productId + "/images/upload");
    }
    @DeleteMapping("/products/{productId}/images/{imageId}") @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable Long productId, @PathVariable Long imageId) {
        service.deleteImage(productId, imageId);
        return ResponseEntity.ok(ApiResponse.success("Image deleted", null, 200, "/api/v1/admin/products/" + productId + "/images/" + imageId));
    }
    @PatchMapping("/products/{productId}/images/{imageId}") @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateImage(@PathVariable Long productId, @PathVariable Long imageId,
                                                            @RequestParam(defaultValue = "false") boolean primary,
                                                            @RequestParam(defaultValue = "0") int sortOrder) {
        return ok("Image updated", service.updateImage(productId, imageId, primary, sortOrder),
                "/api/v1/admin/products/" + productId + "/images/" + imageId);
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(String message, T data, String path) {
        return ResponseEntity.ok(ApiResponse.success(message, data, 200, path));
    }
}
