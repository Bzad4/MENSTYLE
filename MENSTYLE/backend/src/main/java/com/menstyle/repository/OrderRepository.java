package com.menstyle.repository;
import com.menstyle.entity.Order;
import com.menstyle.entity.enums.OrderStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
public interface OrderRepository extends JpaRepository<Order,Long> {
    Optional<Order> findByOrderCode(String orderCode);
    Optional<Order> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);
    Optional<Order> findByIdAndUserId(Long id, Long userId);
    Page<Order> findByUserId(Long userId, Pageable pageable);
    Page<Order> findByUserIdAndOrderStatus(Long userId, OrderStatus status, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :orderId and o.user.id = :userId")
    Optional<Order> findByIdAndUserIdForUpdate(@Param("orderId") Long orderId, @Param("userId") Long userId);
    Page<Order> findAllByOrderStatus(OrderStatus status, Pageable pageable);
    @Query("select o from Order o where (:keyword is null or lower(o.orderCode) like lower(concat('%', :keyword, '%'))) and (:status is null or o.orderStatus = :status)")
    Page<Order> searchAdmin(@Param("keyword") String keyword, @Param("status") OrderStatus status, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :orderId")
    Optional<Order> findByIdForUpdate(@Param("orderId") Long orderId);
}
