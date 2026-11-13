package com.minibanking.api.model;

import com.minibanking.api.model.enums.TransactionStatus;
import com.minibanking.api.model.enums.TransactionType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable Financial Ledger Entry.
 * 
 * Banking Rule: Financial transactions are append-only.
 * They are NEVER deleted or modified, maintaining an unalterable audit trail.
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_tx_ref", columnList = "transaction_reference", unique = true),
    @Index(name = "idx_tx_source_acc", columnList = "source_account_number"),
    @Index(name = "idx_tx_target_acc", columnList = "target_account_number"),
    @Index(name = "idx_tx_timestamp", columnList = "timestamp")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_reference", nullable = false, unique = true, length = 64)
    private String transactionReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "source_account_number", length = 20)
    private String sourceAccountNumber;

    @Column(name = "target_account_number", length = 20)
    private String targetAccountNumber;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;

    public Transaction() {
    }

    public Transaction(Long id, String transactionReference, TransactionType transactionType,
                       BigDecimal amount, String sourceAccountNumber, String targetAccountNumber,
                       String description, TransactionStatus status) {
        this.id = id;
        this.transactionReference = transactionReference;
        this.transactionType = transactionType;
        this.amount = amount;
        this.sourceAccountNumber = sourceAccountNumber;
        this.targetAccountNumber = targetAccountNumber;
        this.description = description;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public void setSourceAccountNumber(String sourceAccountNumber) {
        this.sourceAccountNumber = sourceAccountNumber;
    }

    public String getTargetAccountNumber() {
        return targetAccountNumber;
    }

    public void setTargetAccountNumber(String targetAccountNumber) {
        this.targetAccountNumber = targetAccountNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
