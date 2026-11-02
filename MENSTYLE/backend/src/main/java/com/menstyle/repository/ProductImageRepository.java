package com.menstyle.repository;

import com.menstyle.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Collection;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId);
    List<ProductImage> findByProductIdOrderBySortOrderAscIdAsc(Long productId);
    List<ProductImage> findByProductIdInOrderByProductIdAscSortOrderAsc(Collection<Long> productIds);
    long countByProductId(Long productId);
}
