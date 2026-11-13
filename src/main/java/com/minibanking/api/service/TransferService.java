package com.minibanking.api.service;

import com.minibanking.api.dto.request.TransferRequest;
import com.minibanking.api.dto.response.TransactionResponse;
import com.minibanking.api.exception.*;
import com.minibanking.api.model.Account;
import com.minibanking.api.model.Transaction;
import com.minibanking.api.model.enums.AccountStatus;
import com.minibanking.api.model.enums.TransactionStatus;
import com.minibanking.api.model.enums.TransactionType;
import com.minibanking.api.repository.AccountRepository;
import com.minibanking.api.repository.TransactionRepository;
import com.minibanking.api.security.CustomUserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransferService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Executes an Atomic Money Transfer between two Bank Accounts.
     * 
     * ACID Properties & @Transactional:
     * - Atomicity: Debit sender and credit receiver execute as one indivisible unit.
     * - Rollback: If an exception occurs at any point before commit, the database
     *   rolls back all modifications. Zero money is lost or created!
     */
    @Transactional(rollbackFor = Exception.class)
    public TransactionResponse transfer(TransferRequest request, CustomUserPrincipal principal) {
        log.info("Initiating transfer of ${} from {} to {}", request.amount(),
                request.sourceAccountNumber(), request.targetAccountNumber());

        // Rule 1: Prevent transferring to the same account
        if (request.sourceAccountNumber().equalsIgnoreCase(request.targetAccountNumber())) {
            throw new SelfTransferException("Source and target account numbers cannot be identical");
        }

        // Rule 2: Verify Source Account
        Account source = accountRepository.findByAccountNumber(request.sourceAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found: " + request.sourceAccountNumber()));

        // Ownership Guard: verify caller owns source account (or is ADMIN)
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !source.getUser().getId().equals(principal.getId())) {
            throw new UnauthorizedAccountAccessException("You can only transfer funds from your own account");
        }

        if (source.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountFrozenException("Source account is not active: " + source.getStatus());
        }

        // Rule 3: Verify Target Account
        Account target = accountRepository.findByAccountNumber(request.targetAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Target account not found: " + request.targetAccountNumber()));

        if (target.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountFrozenException("Target account is not active: " + target.getStatus());
        }

        // Rule 4: Verify Sufficient Balance
        if (source.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException("Insufficient funds. Available: $" + source.getBalance() +
                    ", requested: $" + request.amount());
        }

        // --- STEP 1: DEBIT SENDER ---
        source.setBalance(source.getBalance().subtract(request.amount()));
        accountRepository.save(source);
        log.info("Debited ${} from source account {}. New balance: ${}", request.amount(),
                source.getAccountNumber(), source.getBalance());

        // --- DAY 19 ROLLBACK DEMONSTRATION INJECTION ---
        if (request.simulateFailure()) {
            log.error("SIMULATED FAILURE TRIGGERED! Throwing RuntimeException to prove @Transactional rollback...");
            throw new RuntimeException("Simulated Mid-Transfer System Crash! Proving @Transactional Rollback: money is restored.");
        }

        // --- STEP 2: CREDIT RECEIVER ---
        target.setBalance(target.getBalance().add(request.amount()));
        accountRepository.save(target);
        log.info("Credited ${} to target account {}. New balance: ${}", request.amount(),
                target.getAccountNumber(), target.getBalance());

        // --- STEP 3: RECORD AUDIT LEDGER TRANSACTION ---
        String reference = "TRF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Transaction transaction = new Transaction(
                null,
                reference,
                TransactionType.TRANSFER,
                request.amount(),
                source.getAccountNumber(),
                target.getAccountNumber(),
                request.description() != null ? request.description() : "Fund Transfer",
                TransactionStatus.SUCCESS
        );
        Transaction savedTx = transactionRepository.save(transaction);

        log.info("Fund transfer completed successfully. Reference: {}", reference);
        return new TransactionResponse(
                savedTx.getId(),
                savedTx.getTransactionReference(),
                savedTx.getTransactionType(),
                savedTx.getAmount(),
                savedTx.getSourceAccountNumber(),
                savedTx.getTargetAccountNumber(),
                savedTx.getDescription(),
                savedTx.getStatus(),
                savedTx.getTimestamp()
        );
    }
}
