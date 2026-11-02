package com.menstyle.repository;

import com.menstyle.entity.WishlistItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    @Query("select i from WishlistItem i join fetch i.product p join fetch p.category join fetch p.brand where i.wishlist.id = :wishlistId order by i.id")
    List<WishlistItem> findByWishlistIdWithDetails(@Param("wishlistId") Long wishlistId);
    Optional<WishlistItem> findByWishlistIdAndProductId(Long wishlistId, Long productId);
}
