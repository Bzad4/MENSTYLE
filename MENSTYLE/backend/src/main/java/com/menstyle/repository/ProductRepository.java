package com.menstyle.repository;
import com.menstyle.entity.Product; import com.menstyle.entity.enums.ProductStatus; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;

public interface ProductRepository extends JpaRepository<Product,Long> {
    Optional<Product> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);
    @Query(value = """
            select distinct p.*
            from products p
            join categories c on c.id = p.category_id
            join brands b on b.id = p.brand_id
            where (
                :keyword is null
                or p.name collate Latin1_General_100_CI_AI like
                    concat('%', cast(:keyword as nvarchar(4000)), '%')
                or p.slug collate Latin1_General_100_CI_AI like
                    concat('%', cast(:keyword as nvarchar(4000)), '%')
                or exists (
                    select 1 from product_variants v
                    where v.product_id = p.id
                      and v.sku collate Latin1_General_100_CI_AI like
                          concat('%', cast(:keyword as nvarchar(4000)), '%')
                )
            )
            and (:categoryId is null or c.id = :categoryId)
            and (:brandId is null or b.id = :brandId)
            and (:status is null or p.status = :status)
            """,
            countQuery = """
            select count(distinct p.id)
            from products p
            join categories c on c.id = p.category_id
            join brands b on b.id = p.brand_id
            where (
                :keyword is null
                or p.name collate Latin1_General_100_CI_AI like
                    concat('%', cast(:keyword as nvarchar(4000)), '%')
                or p.slug collate Latin1_General_100_CI_AI like
                    concat('%', cast(:keyword as nvarchar(4000)), '%')
                or exists (
                    select 1 from product_variants v
                    where v.product_id = p.id
                      and v.sku collate Latin1_General_100_CI_AI like
                          concat('%', cast(:keyword as nvarchar(4000)), '%')
                )
            )
            and (:categoryId is null or c.id = :categoryId)
            and (:brandId is null or b.id = :brandId)
            and (:status is null or p.status = :status)
            """, nativeQuery = true)
    Page<Product> adminSearch(@Param("keyword") String keyword, @Param("categoryId") Long categoryId,
                              @Param("brandId") Long brandId, @Param("status") String status, Pageable pageable);

    @Query("""
        select distinct p from Product p
        join fetch p.category c join fetch p.brand b
        where p.status = com.menstyle.entity.enums.ProductStatus.ACTIVE
          and (:keyword is null or lower(p.name) like lower(concat('%', :keyword, '%'))
               or lower(p.slug) like lower(concat('%', :keyword, '%'))
               or lower(b.name) like lower(concat('%', :keyword, '%')))
          and (:categoryId is null or c.id = :categoryId)
          and (:brandId is null or b.id = :brandId)
          and (:minPrice is null or coalesce(p.salePrice, p.basePrice) >= :minPrice)
          and (:maxPrice is null or coalesce(p.salePrice, p.basePrice) <= :maxPrice)
          and (:size is null or exists (select 1 from ProductVariant v join v.size s
              where v.product = p and v.status = com.menstyle.entity.enums.ProductStatus.ACTIVE and lower(s.name) = lower(:size)))
          and (:color is null or exists (select 1 from ProductVariant v join v.color col
              where v.product = p and v.status = com.menstyle.entity.enums.ProductStatus.ACTIVE and lower(col.name) = lower(:color)))
          and (:inStock is null or :inStock = false or exists (select 1 from Inventory i
              where i.variant.product = p and i.quantity > i.reservedQuantity))
        """)
    Page<Product> search(@Param("keyword") String keyword, @Param("categoryId") Long categoryId,
                         @Param("brandId") Long brandId, @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice, @Param("size") String size,
                         @Param("color") String color, @Param("inStock") Boolean inStock, Pageable pageable);

    @Query("select distinct p from Product p join fetch p.category join fetch p.brand where p.id = :id and p.status = com.menstyle.entity.enums.ProductStatus.ACTIVE")
    Optional<Product> findActiveById(@Param("id") Long id);

    @Query("select distinct p from Product p join fetch p.category join fetch p.brand where p.slug = :slug and p.status = com.menstyle.entity.enums.ProductStatus.ACTIVE")
    Optional<Product> findActiveBySlug(@Param("slug") String slug);
}
