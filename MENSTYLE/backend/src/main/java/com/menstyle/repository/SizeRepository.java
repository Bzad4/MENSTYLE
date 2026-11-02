package com.menstyle.repository;
import com.menstyle.entity.Size; import com.menstyle.entity.enums.ProductStatus; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface SizeRepository extends JpaRepository<Size,Long> { List<Size> findByStatusOrderByNameAsc(ProductStatus status); }
