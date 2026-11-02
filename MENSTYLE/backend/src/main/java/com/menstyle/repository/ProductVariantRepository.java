package com.menstyle.repository;

import com.menstyle.entity.ProductVariant;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface ProductVariantRepository extends JpaRepository<ProductVariant,Long> {
    Optional<ProductVariant> findBySku(String sku);
    @Query("select v from ProductVariant v join fetch v.product p join fetch v.size join fetch v.color where v.id = :id and v.status = com.menstyle.entity.enums.ProductStatus.ACTIVE and p.status = com.menstyle.entity.enums.ProductStatus.ACTIVE")
    Optional<ProductVariant> findActiveById(@Param("id") Long id);
    @Query("select v from ProductVariant v join fetch v.size join fetch v.color where v.product.id = :productId and v.status = com.menstyle.entity.enums.ProductStatus.ACTIVE order by v.id")
    List<ProductVariant> findActiveByProductId(@Param("productId") Long productId);
    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id);
    boolean existsByProductIdAndSizeIdAndColorId(Long productId, Long sizeId, Long colorId);
    boolean existsByProductIdAndSizeIdAndColorIdAndIdNot(Long productId, Long sizeId, Long colorId, Long id);
    List<ProductVariant> findByProductIdOrderById(Long productId);
}
