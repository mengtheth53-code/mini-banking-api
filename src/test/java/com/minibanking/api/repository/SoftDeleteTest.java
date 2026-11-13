package com.minibanking.api.repository;

import com.minibanking.api.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SoftDeleteTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Day 07 Soft Delete: deleteById() must set is_deleted = true without removing row from database")
    void softDelete_ShouldMarkInactive_WithoutRemovingFromDatabase() {
        // 1. Create and save a user
        User user = new User(null, "John Audited", "john.audit@bank.com", "password", "ROLE_USER", null);
        User saved = userRepository.saveAndFlush(user);
        Long userId = saved.getId();

        // 2. Perform delete
        userRepository.deleteById(userId);
        userRepository.flush();
        entityManager.clear();

        // 3. Normal Spring Data JPA query ignores deleted user due to @SQLRestriction("is_deleted = false")
        Optional<User> queried = userRepository.findById(userId);
        assertThat(queried).isEmpty();

        // 4. Native SQL query verifies row still exists in database with is_deleted = true
        Object isDeletedValue = entityManager.createNativeQuery(
                "SELECT is_deleted FROM users WHERE id = :id")
                .setParameter("id", userId)
                .getSingleResult();

        assertThat(isDeletedValue).isEqualTo(true);
    }
}
