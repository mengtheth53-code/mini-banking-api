package com.minibanking.api.repository;

import com.minibanking.api.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("save() should persist User and generate a primary key ID")
    void save_ShouldPersistUser() {
        User user = new User(null, "Rithy Chea", "rithy@bank.com", "pass123", "ROLE_USER", null);

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isPositive();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findByEmail() should find user by exact email")
    void findByEmail_ShouldReturnUser_WhenExists() {
        User user = new User(null, "Dara Sok", "dara@bank.com", "pass123", "ROLE_USER", null);
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("dara@bank.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Dara Sok");
    }

    @Test
    @DisplayName("findByEmailIgnoreCase() should find user regardless of uppercase/lowercase")
    void findByEmailIgnoreCase_ShouldReturnUser() {
        User user = new User(null, "Sophea Kim", "sophea@bank.com", "pass123", "ROLE_USER", null);
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmailIgnoreCase("SOPHEA@BANK.COM");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("sophea@bank.com");
    }

    @Test
    @DisplayName("existsByEmail() should return true when email exists, false otherwise")
    void existsByEmail_ShouldVerifyPresence() {
        User user = new User(null, "Bopha Pich", "bopha@bank.com", "pass123", "ROLE_USER", null);
        userRepository.save(user);

        assertThat(userRepository.existsByEmail("bopha@bank.com")).isTrue();
        assertThat(userRepository.existsByEmail("unknown@bank.com")).isFalse();
    }

    @Test
    @DisplayName("findByRole() should return all users matching given role")
    void findByRole_ShouldReturnMatchingUsers() {
        userRepository.save(new User(null, "Admin One", "admin1@bank.com", "pass123", "ROLE_ADMIN", null));
        userRepository.save(new User(null, "User One", "user1@bank.com", "pass123", "ROLE_USER", null));
        userRepository.save(new User(null, "User Two", "user2@bank.com", "pass123", "ROLE_USER", null));

        List<User> admins = userRepository.findByRole("ROLE_ADMIN");
        List<User> standardUsers = userRepository.findByRole("ROLE_USER");

        assertThat(admins).hasSize(1);
        assertThat(standardUsers).hasSize(2);
    }

    @Test
    @DisplayName("deleteById() should soft-delete user from database queries")
    void deleteById_ShouldRemoveUserFromQueries() {
        User user = userRepository.save(new User(null, "Temporary", "temp@bank.com", "pass123", "ROLE_USER", null));
        Long userId = user.getId();

        userRepository.deleteById(userId);

        assertThat(userRepository.findById(userId)).isEmpty();
    }
}
