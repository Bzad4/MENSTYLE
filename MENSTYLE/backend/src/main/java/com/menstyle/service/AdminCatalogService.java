package com.menstyle.service;

import com.menstyle.dto.request.*;
import com.menstyle.dto.response.*;
import com.menstyle.entity.*;
import com.menstyle.entity.enums.ProductStatus;
import com.menstyle.exception.BadRequestException;
import com.menstyle.exception.ResourceNotFoundException;
import com.menstyle.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminCatalogService {
    private final ProductRepository products;
    private final CategoryRepository categories;
    private final BrandRepository brands;
    private final ProductVariantRepository variants;
    private final ProductImageRepository images;
    private final InventoryRepository inventories;
    private final SizeRepository sizes;
    private final ColorRepository colors;

    public AdminCatalogService(ProductRepository products, CategoryRepository categories, BrandRepository brands,
                               ProductVariantRepository variants, ProductImageRepository images,
                               InventoryRepository inventories, SizeRepository sizes, ColorRepository colors) {
        this.products = products; this.categories = categories; this.brands = brands; this.variants = variants;
        this.images = images; this.inventories = inventories; this.sizes = sizes; this.colors = colors;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminProductResponse> products(String keyword, Long categoryId, Long brandId, String status,
                                                        int page, int size, String sort) {
        ProductStatus productStatus = status == null || status.isBlank() ? null : parseStatus(status);
        if (page < 0 || size < 1 || size > 50) throw new BadRequestException("Invalid pagination");
        Page<Product> result = products.adminSearch(normalize(keyword), categoryId, brandId,
                productStatus == null ? null : productStatus.name(),
                PageRequest.of(page, size, productSort(sort)));
        return PageResponse.from(result.map(this::productResponse));
    }

    @Transactional(readOnly = true)
    public AdminProductResponse product(Long id) { return productResponse(findProduct(id)); }

    @Transactional
    public AdminProductResponse createProduct(AdminProductRequest request) {
        validateProduct(request, null);
        Product product = new Product();
        applyProduct(product, request);
        return productResponse(products.save(product));
    }

    @Transactional
    public AdminProductResponse updateProduct(Long id, AdminProductRequest request) {
        validateProduct(request, id);
        Product product = findProduct(id);
        applyProduct(product, request);
        return productResponse(products.save(product));
    }

    @Transactional
    public AdminProductResponse setProductStatus(Long id, String status) {
        Product product = findProduct(id);
        product.setStatus(parseStatus(status));
        return productResponse(products.save(product));
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminCategoryResponse> categories(String keyword, int page, int size) {
        validatePage(page, size);
        return PageResponse.from(categories.adminSearch(normalize(keyword),
                PageRequest.of(page, size, Sort.by("name"))).map(AdminCategoryResponse::from));
    }

    @Transactional
    public AdminCategoryResponse createCategory(AdminCategoryRequest request) {
        validateCategory(request, null);
        Category category = new Category(); applyCategory(category, request);
        return AdminCategoryResponse.from(categories.save(category));
    }

    @Transactional
    public AdminCategoryResponse updateCategory(Long id, AdminCategoryRequest request) {
        validateCategory(request, id);
        Category category = categories.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        applyCategory(category, request);
        return AdminCategoryResponse.from(categories.save(category));
    }

    @Transactional
    public AdminCategoryResponse setCategoryStatus(Long id, String status) {
        Category category = categories.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        category.setStatus(parseStatus(status));
        return AdminCategoryResponse.from(categories.save(category));
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminBrandResponse> brands(String keyword, int page, int size) {
        validatePage(page, size);
        return PageResponse.from(brands.adminSearch(normalize(keyword),
                PageRequest.of(page, size, Sort.by("name"))).map(AdminBrandResponse::from));
    }

    @Transactional
    public AdminBrandResponse createBrand(AdminBrandRequest request) {
        validateBrand(request, null);
        Brand brand = new Brand(); applyBrand(brand, request);
        return AdminBrandResponse.from(brands.save(brand));
    }

    @Transactional
    public AdminBrandResponse updateBrand(Long id, AdminBrandRequest request) {
        validateBrand(request, id);
        Brand brand = brands.findById(id).orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
        applyBrand(brand, request);
        return AdminBrandResponse.from(brands.save(brand));
    }

    @Transactional
    public AdminBrandResponse setBrandStatus(Long id, String status) {
        Brand brand = brands.findById(id).orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
        brand.setStatus(parseStatus(status));
        return AdminBrandResponse.from(brands.save(brand));
    }

    @Transactional
    public AdminVariantResponse createVariant(Long productId, AdminVariantRequest request) {
        Product product = findProduct(productId);
        validateVariant(productId, request, null);
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        applyVariant(variant, request);
        ProductVariant saved = variants.save(variant);
        Inventory inventory = new Inventory();
        inventory.setVariant(saved);
        inventory.setQuantity(request.initialStock() == null ? 0 : request.initialStock());
        inventory.setReservedQuantity(0);
        inventory.setReorderLevel(0);
        return AdminVariantResponse.from(saved, inventories.save(inventory));
    }

    @Transactional
    public AdminVariantResponse updateVariant(Long productId, Long variantId, AdminVariantRequest request) {
        findProduct(productId);
        validateVariant(productId, request, variantId);
        ProductVariant variant = variants.findById(variantId)
                .filter(v -> v.getProduct().getId().equals(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));
        applyVariant(variant, request);
        return AdminVariantResponse.from(variants.save(variant), inventories.findByVariantId(variantId).orElse(null));
    }

    @Transactional
    public AdminVariantResponse setVariantStatus(Long productId, Long variantId, String status) {
        findProduct(productId);
        ProductVariant variant = variants.findById(variantId)
                .filter(v -> v.getProduct().getId().equals(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));
        variant.setStatus(parseStatus(status));
        return AdminVariantResponse.from(variants.save(variant), inventories.findByVariantId(variantId).orElse(null));
    }

    @Transactional
    public String addImage(Long productId, String imageUrl, boolean primary, int sortOrder) {
        if (imageUrl == null || imageUrl.isBlank() || imageUrl.length() > 500) throw new BadRequestException("Invalid image URL");
        Product product = findProduct(productId);
        if (primary) images.findByProductIdOrderBySortOrderAscIdAsc(productId).forEach(i -> i.setPrimary(false));
        ProductImage image = new ProductImage();
        image.setProduct(product); image.setImageUrl(imageUrl.trim()); image.setPrimary(primary); image.setSortOrder(Math.max(sortOrder, 0));
        return images.save(image).getImageUrl();
    }

    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        findProduct(productId);
        ProductImage image = images.findById(imageId).filter(i -> i.getProduct().getId().equals(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));
        images.delete(image);
    }

    @Transactional
    public String updateImage(Long productId, Long imageId, boolean primary, int sortOrder) {
        findProduct(productId);
        ProductImage image = images.findById(imageId).filter(i -> i.getProduct().getId().equals(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));
        if (primary) images.findByProductIdOrderBySortOrderAscIdAsc(productId).forEach(i -> i.setPrimary(false));
        image.setPrimary(primary);
        image.setSortOrder(Math.max(sortOrder, 0));
        return images.save(image).getImageUrl();
    }

    private Product findProduct(Long id) { return products.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found")); }
    private AdminProductResponse productResponse(Product p) {
        List<AdminVariantResponse> variantResponses = variants.findByProductIdOrderById(p.getId()).stream()
                .map(v -> AdminVariantResponse.from(v, inventories.findByVariantId(v.getId()).orElse(null))).toList();
        return AdminProductResponse.from(p, images.findByProductIdOrderBySortOrderAscIdAsc(p.getId()).stream().map(ProductImage::getImageUrl).toList(), variantResponses);
    }
    private void validateProduct(AdminProductRequest r, Long id) {
        if ((id == null ? products.existsBySlug(r.slug()) : products.existsBySlugAndIdNot(r.slug(), id))) throw new BadRequestException("Slug is already in use");
        if (r.salePrice() != null && r.salePrice().compareTo(r.basePrice()) > 0) throw new BadRequestException("Sale price must not exceed base price");
        categories.findById(r.categoryId()).orElseThrow(() -> new BadRequestException("Category not found"));
        brands.findById(r.brandId()).orElseThrow(() -> new BadRequestException("Brand not found"));
    }
    private void applyProduct(Product p, AdminProductRequest r) {
        p.setName(r.name().trim()); p.setSlug(r.slug().trim()); p.setDescription(r.description()); p.setMaterial(r.material());
        p.setBasePrice(r.basePrice()); p.setSalePrice(r.salePrice()); p.setStatus(parseStatus(r.status())); p.setFeatured(r.featured());
        p.setCategory(categories.findById(r.categoryId()).orElseThrow(() -> new BadRequestException("Category not found")));
        p.setBrand(brands.findById(r.brandId()).orElseThrow(() -> new BadRequestException("Brand not found")));
    }
    private void validateCategory(AdminCategoryRequest r, Long id) {
        String slug = r.slug().trim();
        if ((id == null ? categories.existsBySlug(slug) : categories.existsBySlugAndIdNot(slug, id))) throw new BadRequestException("Category slug is already in use");
        if (r.parentId() != null && (id != null && id.equals(r.parentId()))) throw new BadRequestException("Category cannot be its own parent");
    }
    private void applyCategory(Category c, AdminCategoryRequest r) {
        c.setName(r.name().trim()); c.setSlug(r.slug().trim()); c.setDescription(r.description()); c.setImageUrl(r.imageUrl()); c.setStatus(parseStatus(r.status()));
        c.setParent(r.parentId() == null ? null : categories.findById(r.parentId()).orElseThrow(() -> new BadRequestException("Parent category not found")));
    }
    private void validateBrand(AdminBrandRequest r, Long id) {
        String name = r.name().trim();
        String slug = r.slug().trim();
        if ((id == null ? brands.existsByName(name) : brands.existsByNameAndIdNot(name, id)) ||
                (id == null ? brands.existsBySlug(slug) : brands.existsBySlugAndIdNot(slug, id))) throw new BadRequestException("Brand name or slug is already in use");
    }
    private void applyBrand(Brand b, AdminBrandRequest r) { b.setName(r.name().trim()); b.setSlug(r.slug().trim()); b.setLogoUrl(r.logoUrl()); b.setDescription(r.description()); b.setStatus(parseStatus(r.status())); }
    private void validateVariant(Long productId, AdminVariantRequest r, Long id) {
        String sku = r.sku().trim();
        if ((id == null ? variants.existsBySku(sku) : variants.existsBySkuAndIdNot(sku, id)) ||
                (id == null ? variants.existsByProductIdAndSizeIdAndColorId(productId, r.sizeId(), r.colorId()) : variants.existsByProductIdAndSizeIdAndColorIdAndIdNot(productId, r.sizeId(), r.colorId(), id)))
            throw new BadRequestException("SKU or size/color combination is already in use");
        sizes.findById(r.sizeId()).orElseThrow(() -> new BadRequestException("Size not found"));
        colors.findById(r.colorId()).orElseThrow(() -> new BadRequestException("Color not found"));
    }
    private void applyVariant(ProductVariant v, AdminVariantRequest r) {
        v.setSize(sizes.findById(r.sizeId()).orElseThrow(() -> new BadRequestException("Size not found")));
        v.setColor(colors.findById(r.colorId()).orElseThrow(() -> new BadRequestException("Color not found")));
        v.setSku(r.sku().trim()); v.setPrice(r.price()); v.setStatus(parseStatus(r.status()));
    }
    private ProductStatus parseStatus(String value) { try { return ProductStatus.valueOf(value.toUpperCase(Locale.ROOT)); } catch (Exception e) { throw new BadRequestException("Invalid status"); } }
    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 50) throw new BadRequestException("Invalid pagination");
    }
    private Sort productSort(String sort) {
        return switch (sort == null ? "newest" : sort) {
            case "nameAsc" -> Sort.by("name").ascending().and(Sort.by("id").ascending());
            case "nameDesc" -> Sort.by("name").descending().and(Sort.by("id").descending());
            case "priceAsc" -> Sort.by("base_price").ascending().and(Sort.by("id").ascending());
            case "priceDesc" -> Sort.by("base_price").descending().and(Sort.by("id").descending());
            default -> Sort.by("created_at").descending().and(Sort.by("id").descending());
        };
    }
}
