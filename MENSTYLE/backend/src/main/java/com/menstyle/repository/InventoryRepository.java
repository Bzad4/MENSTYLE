package com.menstyle.repository;

import com.menstyle.entity.Inventory;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.Collection;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByVariantId(Long variantId);
    @Query("select i from Inventory i where i.variant.id in :variantIds")
    List<Inventory> findByVariantIds(@Param("variantIds") Collection<Long> variantIds);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.variant.id = :variantId")
    Optional<Inventory> findByVariantIdForUpdate(@Param("variantId") Long variantId);
    @org.springframework.data.jpa.repository.Query("select i from Inventory i where i.variant.product.id in :productIds")
    List<Inventory> findByProductIds(@org.springframework.data.repository.query.Param("productIds") Collection<Long> productIds);
    @Query("select i from Inventory i join fetch i.variant v join fetch v.product p where (:keyword is null or lower(v.sku) like lower(concat('%', :keyword, '%')) or lower(p.name) like lower(concat('%', :keyword, '%')))")
    List<Inventory> searchAdmin(@Param("keyword") String keyword);
}
