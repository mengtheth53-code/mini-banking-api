package com.minibanking.api.repository;

import com.minibanking.api.model.User;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory thread-safe implementation of UserRepository.
 * Annotated with @Repository so Spring registers it as a Bean.
 */
@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public InMemoryUserRepository() {
        // Seed initial admin user
        save(new User(null, "System Admin", "admin@minibank.com", "ADMIN", Instant.now()));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return storage.values().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        return storage.values().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
            if (user.getCreatedAt() == null) {
                user.setCreatedAt(Instant.now());
            }
        }
        storage.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }
}
