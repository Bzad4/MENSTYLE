package com.menstyle.repository;
import com.menstyle.entity.Color; import com.menstyle.entity.enums.ProductStatus; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface ColorRepository extends JpaRepository<Color,Long> { List<Color> findByStatusOrderByNameAsc(ProductStatus status); }
