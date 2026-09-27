package com.minibanking.api.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * JPA Entity mapped to the PostgreSQL "users" table.
 * 
 * - @Entity: Marks this class as a persistent database entity managed by Hibernate.
 * - @Table: Specifies the physical database table name ("users").
 * - @Id: Declares the Primary Key.
 * - @GeneratedValue: Uses PostgreSQL BIGSERIAL / IDENTITY auto-incrementing strategy.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "role", nullable = false, length = 20)
    private String role; // "USER", "ADMIN"

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Default no-arg constructor required by JPA specification
     * for Hibernate reflection and entity instantiation.
     */
    public User() {
    }

    public User(Long id, String fullName, String email, String role, Instant createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    /**
     * JPA Entity Lifecycle Callback:
     * Automatically sets createdAt right before Hibernate issues the SQL INSERT.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
