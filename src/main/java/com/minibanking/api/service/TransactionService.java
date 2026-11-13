package com.minibanking.api.service;

import com.minibanking.api.dto.response.TransactionResponse;
import com.minibanking.api.exception.ResourceNotFoundException;
import com.minibanking.api.exception.UnauthorizedAccountAccessException;
import com.minibanking.api.model.Account;
import com.minibanking.api.model.Transaction;
import com.minibanking.api.repository.AccountRepository;
import com.minibanking.api.repository.TransactionRepository;
import com.minibanking.api.security.CustomUserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAccountStatement(
            String accountNumber,
            Instant startDate,
            Instant endDate,
            Pageable pageable,
            CustomUserPrincipal principal
    ) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));

        // Ownership Guard
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !account.getUser().getId().equals(principal.getId())) {
            throw new UnauthorizedAccountAccessException("You can only view statements for your own accounts");
        }

        Page<Transaction> txPage = transactionRepository.findAccountStatement(accountNumber, startDate, endDate, pageable);
        return txPage.map(this::mapToResponse);
    }

    private TransactionResponse mapToResponse(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getTransactionReference(),
                tx.getTransactionType(),
                tx.getAmount(),
                tx.getSourceAccountNumber(),
                tx.getTargetAccountNumber(),
                tx.getDescription(),
                tx.getStatus(),
                tx.getTimestamp()
        );
    }
}
