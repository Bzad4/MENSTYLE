package com.menstyle.service;

import com.menstyle.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCatalogServiceTest {
    @Mock ProductRepository products;
    @Mock CategoryRepository categories;
    @Mock BrandRepository brands;
    @Mock ProductVariantRepository variants;
    @Mock ProductImageRepository images;
    @Mock InventoryRepository inventories;
    @Mock SizeRepository sizes;
    @Mock ColorRepository colors;

    @Test
    void preservesVietnameseKeywordForRepositoryBinding() {
        AdminCatalogService service = new AdminCatalogService(products, categories, brands, variants, images,
                inventories, sizes, colors);
        when(products.adminSearch(any(), any(), any(), any(), any())).thenReturn(Page.empty());

        service.products("Áo Sơ Mi Oxford", null, null, "ACTIVE", 0, 12, "newest");

        verify(products).adminSearch(eq("Áo Sơ Mi Oxford"), isNull(), isNull(), eq("ACTIVE"), any());
    }

    @Test
    void preservesPartialAccentedKeyword() {
        AdminCatalogService service = new AdminCatalogService(products, categories, brands, variants, images,
                inventories, sizes, colors);
        when(products.adminSearch(any(), any(), any(), any(), any())).thenReturn(Page.empty());

        service.products("Quần Tây", null, null, null, 0, 12, "newest");

        verify(products).adminSearch(eq("Quần Tây"), isNull(), isNull(), isNull(), any());
    }
}
