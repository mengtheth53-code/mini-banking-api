package com.minibanking.api.dto.response;

import com.minibanking.api.model.enums.TransactionStatus;
import com.minibanking.api.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
    Long id,
    String transactionReference,
    TransactionType transactionType,
    BigDecimal amount,
    String sourceAccountNumber,
    String targetAccountNumber,
    String description,
    TransactionStatus status,
    Instant timestamp
) {}
