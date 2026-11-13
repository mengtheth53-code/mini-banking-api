package com.minibanking.api.dto.request;

import com.minibanking.api.model.enums.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountCreateRequest(
    @NotNull(message = "Account type is required (SAVINGS or CHECKING)")
    AccountType accountType,

    @DecimalMin(value = "0.00", message = "Initial deposit cannot be negative")
    BigDecimal initialDeposit
) {}
