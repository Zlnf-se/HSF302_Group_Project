package com.recruit.recruitmentapplication.repository;

import com.recruit.recruitmentapplication.entity.User;
import com.recruit.recruitmentapplication.entity.User.AccountStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByRole_Name(String roleName);

    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.username = :username")
    Optional<User> findByUsernameWithRole(@Param("username") String username);

    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmailWithRole(@Param("email") String email);

    @Query("SELECT u FROM User u JOIN FETCH u.role ORDER BY u.id")
    List<User> findAllWithRole();

    @Query("""
            SELECT u FROM User u JOIN FETCH u.role
            WHERE (:search IS NULL
                   OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:roleName IS NULL OR u.role.name = :roleName)
              AND (:accountStatus IS NULL OR u.accountStatus = :accountStatus)
            ORDER BY u.id
            """)
    List<User> searchUsers(@Param("search") String search,
                           @Param("roleName") String roleName,
                           @Param("accountStatus") AccountStatus accountStatus);

    long countByRole_NameAndAccountStatus(String roleName, AccountStatus accountStatus);
}
