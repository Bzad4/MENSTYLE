package com.menstyle.repository;
import com.menstyle.entity.Brand; import com.menstyle.entity.enums.ProductStatus; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import org.springframework.data.domain.Page;
public interface BrandRepository extends JpaRepository<Brand,Long> {
    Optional<Brand> findBySlug(String slug);
    List<Brand> findByStatusOrderByNameAsc(ProductStatus status);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);
    @org.springframework.data.jpa.repository.Query("select b from Brand b where (:keyword is null or lower(b.name) like lower(concat('%', :keyword, '%')) or lower(b.slug) like lower(concat('%', :keyword, '%')))")
    Page<Brand> adminSearch(@org.springframework.data.repository.query.Param("keyword") String keyword, org.springframework.data.domain.Pageable pageable);
}
