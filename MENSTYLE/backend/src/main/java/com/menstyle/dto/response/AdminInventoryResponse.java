package com.menstyle.dto.response;

public record AdminInventoryResponse(Long inventoryId, Long variantId, String productName,
        String sku, int quantity, int reservedQuantity, int availableQuantity, int reorderLevel) {}
