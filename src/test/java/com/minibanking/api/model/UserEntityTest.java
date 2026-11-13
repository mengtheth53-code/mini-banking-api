package com.minibanking.api.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("JPA should persist User entity, generate ID, and automatically populate createdAt via @PrePersist")
    void persistUser_Success() {
        User user = new User();
        user.setFullName("Kosal Meas");
        user.setEmail("kosal.meas@bank.com");
        user.setPassword("hashedPassword123");
        user.setRole("ROLE_USER");

        User persisted = entityManager.persistAndFlush(user);

        assertThat(persisted.getId()).isNotNull();
        assertThat(persisted.getId()).isPositive();
        assertThat(persisted.getFullName()).isEqualTo("Kosal Meas");
        assertThat(persisted.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("JPA should enforce unique constraint on email column")
    void persistUser_DuplicateEmail_ThrowsException() {
        User user1 = new User(null, "User One", "unique@bank.com", "pass123", "ROLE_USER", null);
        entityManager.persistAndFlush(user1);

        User user2 = new User(null, "User Two", "unique@bank.com", "pass123", "ROLE_USER", null);

        assertThatThrownBy(() -> entityManager.persistAndFlush(user2))
                .isInstanceOf(Exception.class);
    }
}
