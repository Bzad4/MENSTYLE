package com.menstyle.controller;

import com.menstyle.dto.response.*;
import com.menstyle.service.ProductCatalogService;
import com.menstyle.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class PublicCatalogControllerTest {
    private MockMvc mockMvc;
    private ProductCatalogService service;

    @BeforeEach
    void setUp() {
        service = mock(ProductCatalogService.class);
        mockMvc = standaloneSetup(new PublicCatalogController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void productsArePublic() throws Exception {
        when(service.list(any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt(), any()))
                .thenReturn(new PageResponse<>(List.of(), 0, 12, 0, 0));
        mockMvc.perform(get("/api/v1/public/products")).andExpect(status().isOk());
    }

    @Test
    void rejectsNegativePage() throws Exception {
        mockMvc.perform(get("/api/v1/public/products?page=-1")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void rejectsOversizedPage() throws Exception {
        mockMvc.perform(get("/api/v1/public/products?pageSize=51")).andExpect(status().isBadRequest());
    }

    @Test
    void variantSizeFilterDoesNotChangePageSize() throws Exception {
        when(service.list(any(), any(), any(), any(), any(), eq("M"), any(), any(), eq(0), eq(12), any()))
                .thenReturn(new PageResponse<>(List.of(), 0, 12, 0, 0));

        mockMvc.perform(get("/api/v1/public/products?pageSize=12&sizeFilter=M"))
                .andExpect(status().isOk());
        verify(service).list(any(), any(), any(), any(), any(), eq("M"), any(), any(), eq(0), eq(12), any());
    }

    @Test
    void rejectsInvalidPriceRange() throws Exception {
        mockMvc.perform(get("/api/v1/public/products?minPrice=500&maxPrice=100")).andExpect(status().isBadRequest());
    }

    @Test
    void rejectsUnknownSort() throws Exception {
        when(service.list(any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt(), eq("random")))
                .thenThrow(new IllegalArgumentException("Unsupported sort option"));
        mockMvc.perform(get("/api/v1/public/products?sort=random")).andExpect(status().isBadRequest());
    }
}
