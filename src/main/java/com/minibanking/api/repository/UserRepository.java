package com.minibanking.api.repository;

import com.minibanking.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for User entity.
 * 
 * By extending JpaRepository<User, Long>, Spring Data JPA automatically provides:
 * - save(), saveAll()
 * - findById(), existsById()
 * - findAll(), count()
 * - deleteById(), delete(), deleteAll()
 * 
 * NO implementation class is required! Spring Data dynamically generates 
 * a runtime proxy bean that translates interface methods into SQL queries.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Derived Query Method: translates to -> SELECT * FROM users WHERE LOWER(email) = LOWER(?1)
    Optional<User> findByEmailIgnoreCase(String email);

    // Derived Query Method: translates to -> SELECT * FROM users WHERE email = ?1
    Optional<User> findByEmail(String email);

    // Derived Query Method: translates to -> SELECT COUNT(*) > 0 FROM users WHERE email = ?1
    boolean existsByEmail(String email);

    // Derived Query Method: translates to -> SELECT * FROM users WHERE role = ?1
    List<User> findByRole(String role);
}
