package com.menstyle.service;

import com.menstyle.entity.*;
import com.menstyle.entity.enums.ProductStatus;
import com.menstyle.exception.ResourceNotFoundException;
import com.menstyle.exception.BadRequestException;
import com.menstyle.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCatalogServiceTest {
    @Mock ProductRepository productRepository;
    @Mock ProductImageRepository imageRepository;
    @Mock ProductVariantRepository variantRepository;
    @Mock InventoryRepository inventoryRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock BrandRepository brandRepository;
    ProductCatalogService service;

    @BeforeEach
    void setUp() {
        service = new ProductCatalogService(productRepository, imageRepository, variantRepository,
                inventoryRepository, categoryRepository, brandRepository);
    }

    @Test
    void listMapsPageAndStock() {
        Product product = product(1L, "Polo", "polo");
        when(productRepository.search(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(product), PageRequest.of(0, 12), 1));
        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl("image.jpg");
        when(imageRepository.findByProductIdInOrderByProductIdAscSortOrderAsc(any())).thenReturn(List.of(image));
        when(inventoryRepository.findByProductIds(any())).thenReturn(List.of());

        var result = service.list(null, null, null, null, null, null, null, null, 0, 12, "newest");

        assertEquals(1, result.totalElements());
        assertEquals("image.jpg", result.content().get(0).primaryImage());
        assertFalse(result.content().get(0).inStock());
    }

    @Test
    void detailRejectsMissingProduct() {
        when(productRepository.findActiveById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.detail(99L));
    }

    @Test
    void detailMapsVariantsAndAvailableStock() {
        Product product = product(1L, "Polo", "polo");
        ProductVariant variant = new ProductVariant();
        variant.setId(2L);
        variant.setSku("POLO-M-BLK");
        variant.setProduct(product);
        Size size = new Size(); size.setName("M"); variant.setSize(size);
        Color color = new Color(); color.setName("Black"); variant.setColor(color);
        variant.setPrice(BigDecimal.valueOf(100));
        when(productRepository.findActiveById(1L)).thenReturn(Optional.of(product));
        when(imageRepository.findByProductIdOrderBySortOrderAsc(1L)).thenReturn(List.of());
        when(variantRepository.findActiveByProductId(1L)).thenReturn(List.of(variant));
        Inventory inventory = new Inventory(); inventory.setId(999L); inventory.setVariant(variant); inventory.setQuantity(10); inventory.setReservedQuantity(3);
        when(inventoryRepository.findByVariantIds(List.of(2L))).thenReturn(List.of(inventory));

        var result = service.detail(1L);

        assertEquals(1, result.variants().size());
        assertEquals(7, result.variants().get(0).stockQuantity());
        assertTrue(result.variants().get(0).inStock());
    }

    @Test
    void categoriesAndBrandsReturnRepositoryResults() {
        when(categoryRepository.findByStatusOrderByNameAsc(ProductStatus.ACTIVE)).thenReturn(List.of(new Category()));
        when(brandRepository.findByStatusOrderByNameAsc(ProductStatus.ACTIVE)).thenReturn(List.of(new Brand()));
        assertEquals(1, service.categories().size());
        assertEquals(1, service.brands().size());
    }

    @Test
    void rejectsUnsupportedSort() {
        assertThrows(BadRequestException.class,
                () -> service.list(null, null, null, null, null, null, null, null, 0, 12, "random"));
    }

    @Test
    void buildsBasePriceAscendingSortWithIdTieBreaker() {
        assertSort("priceAsc", Sort.Direction.ASC, "basePrice", Sort.Direction.ASC, "id");
    }

    @Test
    void buildsBasePriceDescendingSortWithIdTieBreaker() {
        assertSort("priceDesc", Sort.Direction.DESC, "basePrice", Sort.Direction.DESC, "id");
    }

    @Test
    void buildsNameAscendingSortWithIdTieBreaker() {
        assertSort("nameAsc", Sort.Direction.ASC, "name", Sort.Direction.ASC, "id");
    }

    @Test
    void buildsNameDescendingSortWithIdTieBreaker() {
        assertSort("nameDesc", Sort.Direction.DESC, "name", Sort.Direction.DESC, "id");
    }

    @Test
    void buildsNewestSortWithIdTieBreaker() {
        assertSort("newest", Sort.Direction.DESC, "createdAt", Sort.Direction.DESC, "id");
    }

    private void assertSort(String option, Sort.Direction primaryDirection, String primaryProperty,
                            Sort.Direction tieBreakerDirection, String tieBreakerProperty) {
        when(productRepository.search(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        service.list(null, null, null, null, null, null, null, null, 0, 12, option);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).search(any(), any(), any(), any(), any(), any(), any(), any(), pageableCaptor.capture());
        List<Sort.Order> orders = pageableCaptor.getValue().getSort().stream().toList();
        assertEquals(2, orders.size());
        assertEquals(primaryProperty, orders.get(0).getProperty());
        assertEquals(primaryDirection, orders.get(0).getDirection());
        assertEquals(tieBreakerProperty, orders.get(1).getProperty());
        assertEquals(tieBreakerDirection, orders.get(1).getDirection());
    }

    private Product product(Long id, String name, String slug) {
        Product p = new Product(); p.setId(id); p.setName(name); p.setSlug(slug);
        p.setBasePrice(BigDecimal.valueOf(100)); p.setStatus(ProductStatus.ACTIVE);
        Category category = new Category(); category.setId(1L); category.setName("Category"); category.setSlug("category"); p.setCategory(category);
        Brand brand = new Brand(); brand.setId(1L); brand.setName("Brand"); brand.setSlug("brand"); p.setBrand(brand);
        return p;
    }
}
