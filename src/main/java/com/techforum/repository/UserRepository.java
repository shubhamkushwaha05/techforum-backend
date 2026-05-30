package com.techforum.repository;

import com.techforum.entity.User;
import com.techforum.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    // Fix 2: Proper DB-level search — was loading ALL users into memory
    @Query("""
        SELECT u FROM User u
        WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY u.createdAt DESC
    """)
    Page<User> searchByKeywordContaining(@Param("keyword") String keyword, Pageable pageable);

    // Fix 2: COUNT queries — was using findAll() + Java stream for stats
    long countByIsBannedTrue();

    // Fix 2: Use @Param with RoleName enum — string literal JPQL comparison
    //        with enum field is unreliable across JPA providers
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.name = :role")
    long countByRole(@Param("role") RoleName role);
}
