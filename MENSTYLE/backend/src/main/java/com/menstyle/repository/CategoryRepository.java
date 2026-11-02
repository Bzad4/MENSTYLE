package com.menstyle.repository;
import com.menstyle.entity.Category; import com.menstyle.entity.enums.ProductStatus; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*; import org.springframework.data.domain.Page;
public interface CategoryRepository extends JpaRepository<Category,Long> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByStatusOrderByNameAsc(ProductStatus status);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);
    @org.springframework.data.jpa.repository.Query("select c from Category c where (:keyword is null or lower(c.name) like lower(concat('%', :keyword, '%')) or lower(c.slug) like lower(concat('%', :keyword, '%')))")
    Page<Category> adminSearch(@org.springframework.data.repository.query.Param("keyword") String keyword, org.springframework.data.domain.Pageable pageable);
}
