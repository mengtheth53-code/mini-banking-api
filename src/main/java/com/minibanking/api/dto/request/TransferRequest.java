package com.minibanking.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
    @NotBlank(message = "Source account number is required")
    String sourceAccountNumber,

    @NotBlank(message = "Target account number is required")
    String targetAccountNumber,

    @NotNull(message = "Transfer amount is required")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than zero")
    BigDecimal amount,

    String description,

    /**
     * For Day 19 Rollback demonstration:
     * When set to true, the service will inject a forced RuntimeException right after debiting
     * the sender to prove @Transactional rolls back the entire transaction!
     */
    boolean simulateFailure
) {}
