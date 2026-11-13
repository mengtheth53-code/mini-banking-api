package com.minibanking.api.service;

import com.minibanking.api.dto.request.AccountCreateRequest;
import com.minibanking.api.dto.request.DepositRequest;
import com.minibanking.api.dto.request.WithdrawRequest;
import com.minibanking.api.dto.response.AccountResponse;
import com.minibanking.api.dto.response.TransactionResponse;
import com.minibanking.api.exception.AccountFrozenException;
import com.minibanking.api.exception.InsufficientBalanceException;
import com.minibanking.api.exception.ResourceNotFoundException;
import com.minibanking.api.exception.UnauthorizedAccountAccessException;
import com.minibanking.api.model.Account;
import com.minibanking.api.model.Transaction;
import com.minibanking.api.model.User;
import com.minibanking.api.model.enums.AccountStatus;
import com.minibanking.api.model.enums.TransactionStatus;
import com.minibanking.api.model.enums.TransactionType;
import com.minibanking.api.repository.AccountRepository;
import com.minibanking.api.repository.TransactionRepository;
import com.minibanking.api.repository.UserRepository;
import com.minibanking.api.security.CustomUserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final SecureRandom random = new SecureRandom();

    public AccountService(AccountRepository accountRepository, UserRepository userRepository,
                          TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String accountNumber = generateUniqueAccountNumber();
        BigDecimal initialBalance = (request.initialDeposit() != null && request.initialDeposit().compareTo(BigDecimal.ZERO) > 0)
                ? request.initialDeposit()
                : BigDecimal.ZERO;

        Account account = new Account(null, accountNumber, request.accountType(), initialBalance, AccountStatus.ACTIVE, user);
        Account saved = accountRepository.save(account);

        // Record initial deposit transaction if positive
        if (initialBalance.compareTo(BigDecimal.ZERO) > 0) {
            Transaction initialTx = new Transaction(
                    null,
                    "DEP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    TransactionType.DEPOSIT,
                    initialBalance,
                    null,
                    saved.getAccountNumber(),
                    "Initial account opening deposit",
                    TransactionStatus.SUCCESS
            );
            transactionRepository.save(initialTx);
        }

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(String accountNumber, CustomUserPrincipal principal) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));

        // Ownership Guard: verify caller owns account (or is ADMIN)
        verifyOwnership(account, principal);

        return mapToResponse(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public TransactionResponse deposit(DepositRequest request) {
        Account account = accountRepository.findByAccountNumber(request.accountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.accountNumber()));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountFrozenException("Cannot deposit to account: status is " + account.getStatus());
        }

        account.setBalance(account.getBalance().add(request.amount()));
        accountRepository.save(account);

        Transaction tx = new Transaction(
                null,
                "DEP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                TransactionType.DEPOSIT,
                request.amount(),
                null,
                account.getAccountNumber(),
                request.description() != null ? request.description() : "Cash Deposit",
                TransactionStatus.SUCCESS
        );
        Transaction savedTx = transactionRepository.save(tx);

        return mapToTxResponse(savedTx);
    }

    @Transactional
    public TransactionResponse withdraw(WithdrawRequest request, CustomUserPrincipal principal) {
        Account account = accountRepository.findByAccountNumber(request.accountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.accountNumber()));

        // Ownership Guard
        verifyOwnership(account, principal);

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountFrozenException("Account is not active: " + account.getStatus());
        }

        if (account.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException("Insufficient funds. Available balance: $" + account.getBalance());
        }

        account.setBalance(account.getBalance().subtract(request.amount()));
        accountRepository.save(account);

        Transaction tx = new Transaction(
                null,
                "WTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                TransactionType.WITHDRAWAL,
                request.amount(),
                account.getAccountNumber(),
                null,
                request.description() != null ? request.description() : "ATM Withdrawal",
                TransactionStatus.SUCCESS
        );
        Transaction savedTx = transactionRepository.save(tx);

        return mapToTxResponse(savedTx);
    }

    private void verifyOwnership(Account account, CustomUserPrincipal principal) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !account.getUser().getId().equals(principal.getId())) {
            throw new UnauthorizedAccountAccessException("You do not have permission to access account: " + account.getAccountNumber());
        }
    }

    private String generateUniqueAccountNumber() {
        String accNum;
        do {
            long number = 1000000000L + (long)(random.nextDouble() * 8999999999L);
            accNum = String.valueOf(number);
        } while (accountRepository.existsByAccountNumber(accNum));
        return accNum;
    }

    private AccountResponse mapToResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getStatus(),
                account.getUser().getEmail(),
                account.getCreatedAt()
        );
    }

    private TransactionResponse mapToTxResponse(Transaction tx) {
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
