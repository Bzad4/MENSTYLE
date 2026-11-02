package com.menstyle.service;

import com.menstyle.dto.response.*;
import com.menstyle.entity.*;
import com.menstyle.entity.enums.ProductStatus;
import com.menstyle.exception.ResourceNotFoundException;
import com.menstyle.exception.BadRequestException;
import com.menstyle.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductCatalogService {
    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryRepository inventoryRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    public ProductCatalogService(ProductRepository productRepository, ProductImageRepository imageRepository,
                                 ProductVariantRepository variantRepository, InventoryRepository inventoryRepository,
                                 CategoryRepository categoryRepository, BrandRepository brandRepository) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.variantRepository = variantRepository;
        this.inventoryRepository = inventoryRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductListItemResponse> list(String keyword, Long categoryId, Long brandId,
                                                       BigDecimal minPrice, BigDecimal maxPrice, String size,
                                                       String color, Boolean inStock, int page, int pageSize,
                                                       String sort) {
        if (page < 0 || pageSize < 1 || pageSize > 50) throw new BadRequestException("Invalid pagination");
        if (minPrice != null && minPrice.signum() < 0 || maxPrice != null && maxPrice.signum() < 0
                || minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0)
            throw new BadRequestException("Invalid price range");
        Pageable pageable;
        try {
            pageable = PageRequest.of(page, pageSize, sortFor(sort));
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException(exception.getMessage());
        }
        Page<Product> products = productRepository.search(normalize(keyword), categoryId, brandId, minPrice, maxPrice,
                normalize(size), normalize(color), inStock, pageable);
        List<Long> ids = products.getContent().stream().map(Product::getId).toList();
        Map<Long, String> images = imageRepository.findByProductIdInOrderByProductIdAscSortOrderAsc(ids).stream()
                .collect(Collectors.toMap(i -> i.getProduct().getId(), ProductImage::getImageUrl, (a, b) -> a));
        Map<Long, Boolean> stocks = inventoryRepository.findByProductIds(ids).stream()
                .collect(Collectors.groupingBy(i -> i.getVariant().getProduct().getId(),
                        Collectors.collectingAndThen(Collectors.toList(),
                                list -> list.stream().anyMatch(i -> i.getQuantity() > i.getReservedQuantity()))));
        return PageResponse.from(products.map(p -> ProductListItemResponse.from(p, images.get(p.getId()),
                Boolean.TRUE.equals(stocks.get(p.getId())))));
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse detail(Long id) {
        return toDetail(productRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found")));
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse detailBySlug(String slug) {
        return toDetail(productRepository.findActiveBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found")));
    }

    @Transactional(readOnly = true)
    public List<CategorySummaryResponse> categories() {
        return categoryRepository.findByStatusOrderByNameAsc(ProductStatus.ACTIVE).stream()
                .map(CategorySummaryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<BrandSummaryResponse> brands() {
        return brandRepository.findByStatusOrderByNameAsc(ProductStatus.ACTIVE).stream()
                .map(BrandSummaryResponse::from).toList();
    }

    private ProductDetailResponse toDetail(Product product) {
        List<String> images = imageRepository.findByProductIdOrderBySortOrderAsc(product.getId()).stream()
                .map(ProductImage::getImageUrl).toList();
        List<ProductVariant> variants = variantRepository.findActiveByProductId(product.getId());
        Map<Long, Inventory> inventories = inventoryRepository.findByVariantIds(
                variants.stream().map(ProductVariant::getId).toList()).stream()
                .collect(Collectors.toMap(i -> i.getVariant().getId(), Function.identity()));
        return ProductDetailResponse.from(product, images, variants.stream()
                .map(v -> ProductVariantResponse.from(v, inventories.get(v.getId()))).toList());
    }

    private Sort sortFor(String option) {
        return switch (option == null ? "newest" : option) {
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt")
                    .and(Sort.by(Sort.Direction.DESC, "id"));
            case "priceAsc" -> Sort.by(Sort.Direction.ASC, "basePrice")
                    .and(Sort.by(Sort.Direction.ASC, "id"));
            case "priceDesc" -> Sort.by(Sort.Direction.DESC, "basePrice")
                    .and(Sort.by(Sort.Direction.DESC, "id"));
            case "nameAsc" -> Sort.by(Sort.Direction.ASC, "name")
                    .and(Sort.by(Sort.Direction.ASC, "id"));
            case "nameDesc" -> Sort.by(Sort.Direction.DESC, "name")
                    .and(Sort.by(Sort.Direction.DESC, "id"));
            default -> throw new IllegalArgumentException("Unsupported sort option");
        };
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
