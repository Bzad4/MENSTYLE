package com.menstyle.repository;
import com.menstyle.entity.User;
import com.menstyle.entity.enums.UserStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import jakarta.persistence.LockModeType;
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<User> findByRoleName(String roleName, Pageable pageable);
    Page<User> findByRoleNameIn(java.util.Collection<String> roleNames, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);
    @Query("select u from User u where u.role.name = 'CUSTOMER' and (:keyword is null or lower(u.fullName) like lower(concat('%', :keyword, '%')) or lower(u.email) like lower(concat('%', :keyword, '%')))")
    Page<User> searchCustomers(@Param("keyword") String keyword, Pageable pageable);
    Page<User> findByRoleNameAndStatus(String roleName, UserStatus status, Pageable pageable);
}
