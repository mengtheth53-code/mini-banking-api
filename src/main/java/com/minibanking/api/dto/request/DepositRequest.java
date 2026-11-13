package com.minibanking.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DepositRequest(
    @NotBlank(message = "Account number is required")
    String accountNumber,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Deposit amount must be at least $1.00")
    BigDecimal amount,

    String description
) {}
