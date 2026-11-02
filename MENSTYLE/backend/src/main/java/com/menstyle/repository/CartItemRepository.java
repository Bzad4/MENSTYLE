package com.menstyle.repository;

import com.menstyle.entity.CartItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @Query("select i from CartItem i join fetch i.variant v join fetch v.product p join fetch v.size join fetch v.color where i.cart.id = :cartId order by i.id")
    List<CartItem> findByCartIdWithDetails(@Param("cartId") Long cartId);
    Optional<CartItem> findByCartIdAndVariantId(Long cartId, Long variantId);
    Optional<CartItem> findByIdAndCartId(Long id, Long cartId);
}
