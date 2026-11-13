package com.minibanking.api.controller;

import com.minibanking.api.dto.request.AccountCreateRequest;
import com.minibanking.api.dto.request.DepositRequest;
import com.minibanking.api.dto.request.WithdrawRequest;
import com.minibanking.api.dto.response.AccountResponse;
import com.minibanking.api.dto.response.TransactionResponse;
import com.minibanking.api.security.CustomUserPrincipal;
import com.minibanking.api.service.AccountService;
import com.minibanking.api.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts & Operations", description = "Bank account management, deposits, withdrawals, and statements")
@SecurityRequirement(name = "Bearer Authentication")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    public AccountController(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @PostMapping
    @Operation(summary = "Open a new bank account (SAVINGS or CHECKING)")
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountCreateRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        AccountResponse response = accountService.createAccount(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get all accounts owned by the currently authenticated user")
    public ResponseEntity<List<AccountResponse>> getMyAccounts(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<AccountResponse> accounts = accountService.getMyAccounts(principal.getId());
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get account details and current balance (Ownership protected)")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable String accountNumber,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        AccountResponse account = accountService.getAccount(accountNumber, principal);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposit funds into a bank account")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody DepositRequest request) {
        TransactionResponse response = accountService.deposit(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw funds from an account (Ownership & balance protected)")
    public ResponseEntity<TransactionResponse> withdraw(
            @Valid @RequestBody WithdrawRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        TransactionResponse response = accountService.withdraw(request, principal);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountNumber}/transactions")
    @Operation(summary = "Get paginated account statement / transaction history")
    public ResponseEntity<Page<TransactionResponse>> getAccountStatement(
            @PathVariable String accountNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @PageableDefault(size = 10, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Page<TransactionResponse> statement = transactionService.getAccountStatement(
                accountNumber, startDate, endDate, pageable, principal
        );
        return ResponseEntity.ok(statement);
    }
}
