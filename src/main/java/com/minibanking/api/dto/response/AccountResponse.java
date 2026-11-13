package com.minibanking.api.dto.response;

import com.minibanking.api.model.enums.AccountStatus;
import com.minibanking.api.model.enums.AccountType;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(
    Long id,
    String accountNumber,
    AccountType accountType,
    BigDecimal balance,
    AccountStatus status,
    String ownerEmail,
    Instant createdAt
) {}
