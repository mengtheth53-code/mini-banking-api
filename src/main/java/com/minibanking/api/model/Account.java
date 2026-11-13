package com.minibanking.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minibanking.api.model.enums.AccountStatus;
import com.minibanking.api.model.enums.AccountType;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * JPA Entity representing a Customer Bank Account.
 * 
 * Soft Delete Pattern:
 * - When deleted, sets is_deleted = true.
 * - Queries automatically exclude closed/deleted accounts.
 */
@Entity
@Table(name = "accounts")
@SQLDelete(sql = "UPDATE accounts SET is_deleted = true, status = 'CLOSED' WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    // Banking Rule: Always use BigDecimal for money to avoid floating point inaccuracies!
    @Column(name = "balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Account() {
    }

    public Account(Long id, String accountNumber, AccountType accountType, BigDecimal balance, AccountStatus status, User user) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
        this.status = status != null ? status : AccountStatus.ACTIVE;
        this.user = user;
        this.isDeleted = false;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }
        if (this.status == null) {
            this.status = AccountStatus.ACTIVE;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
