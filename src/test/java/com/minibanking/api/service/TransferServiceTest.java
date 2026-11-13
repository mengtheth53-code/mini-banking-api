package com.minibanking.api.service;

import com.minibanking.api.dto.request.TransferRequest;
import com.minibanking.api.dto.response.TransactionResponse;
import com.minibanking.api.exception.InsufficientBalanceException;
import com.minibanking.api.exception.SelfTransferException;
import com.minibanking.api.exception.UnauthorizedAccountAccessException;
import com.minibanking.api.model.Account;
import com.minibanking.api.model.Transaction;
import com.minibanking.api.model.User;
import com.minibanking.api.model.enums.AccountStatus;
import com.minibanking.api.model.enums.AccountType;
import com.minibanking.api.model.enums.TransactionStatus;
import com.minibanking.api.model.enums.TransactionType;
import com.minibanking.api.repository.AccountRepository;
import com.minibanking.api.repository.TransactionRepository;
import com.minibanking.api.security.CustomUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransferService transferService;

    private User senderUser;
    private User receiverUser;
    private Account sourceAccount;
    private Account targetAccount;
    private CustomUserPrincipal senderPrincipal;

    @BeforeEach
    void setUp() {
        senderUser = new User(1L, "Alice Sender", "alice@bank.com", "hash", "ROLE_USER", null);
        receiverUser = new User(2L, "Bob Receiver", "bob@bank.com", "hash", "ROLE_USER", null);

        sourceAccount = new Account(1L, "ACC-1111", AccountType.CHECKING, new BigDecimal("1000.00"), AccountStatus.ACTIVE, senderUser);
        targetAccount = new Account(2L, "ACC-2222", AccountType.SAVINGS, new BigDecimal("200.00"), AccountStatus.ACTIVE, receiverUser);

        senderPrincipal = new CustomUserPrincipal(senderUser);
    }

    @Test
    @DisplayName("transfer() should debit source, credit target, and record transaction on success")
    void transfer_Success() {
        TransferRequest request = new TransferRequest("ACC-1111", "ACC-2222", new BigDecimal("300.00"), "Rent payment", false);

        when(accountRepository.findByAccountNumber("ACC-1111")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber("ACC-2222")).thenReturn(Optional.of(targetAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transferService.transfer(request, senderPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.amount()).isEqualByComparingTo("300.00");
        assertThat(response.status()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(sourceAccount.getBalance()).isEqualByComparingTo("700.00");
        assertThat(targetAccount.getBalance()).isEqualByComparingTo("500.00");

        verify(accountRepository, times(1)).save(sourceAccount);
        verify(accountRepository, times(1)).save(targetAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("transfer() should throw InsufficientBalanceException when balance is less than transfer amount")
    void transfer_InsufficientBalance_ThrowsException() {
        TransferRequest request = new TransferRequest("ACC-1111", "ACC-2222", new BigDecimal("5000.00"), "Overdraft attempt", false);

        when(accountRepository.findByAccountNumber("ACC-1111")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber("ACC-2222")).thenReturn(Optional.of(targetAccount));

        assertThatThrownBy(() -> transferService.transfer(request, senderPrincipal))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient funds");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("transfer() should throw SelfTransferException when source equals target")
    void transfer_SelfTransfer_ThrowsException() {
        TransferRequest request = new TransferRequest("ACC-1111", "ACC-1111", new BigDecimal("50.00"), "Self transfer", false);

        assertThatThrownBy(() -> transferService.transfer(request, senderPrincipal))
                .isInstanceOf(SelfTransferException.class)
                .hasMessageContaining("cannot be identical");
    }

    @Test
    @DisplayName("transfer() should throw UnauthorizedAccountAccessException when caller does not own source account")
    void transfer_UnauthorizedUser_ThrowsException() {
        User hacker = new User(99L, "Hacker", "hacker@evil.com", "hash", "ROLE_USER", null);
        CustomUserPrincipal hackerPrincipal = new CustomUserPrincipal(hacker);

        TransferRequest request = new TransferRequest("ACC-1111", "ACC-2222", new BigDecimal("100.00"), "Unauthorized theft", false);
        when(accountRepository.findByAccountNumber("ACC-1111")).thenReturn(Optional.of(sourceAccount));

        assertThatThrownBy(() -> transferService.transfer(request, hackerPrincipal))
                .isInstanceOf(UnauthorizedAccountAccessException.class);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Day 19 Rollback Proof: simulateFailure = true triggers RuntimeException after debit to verify rollback boundary")
    void transfer_SimulateFailure_ThrowsRuntimeException() {
        TransferRequest request = new TransferRequest("ACC-1111", "ACC-2222", new BigDecimal("100.00"), "Rollback test", true);

        when(accountRepository.findByAccountNumber("ACC-1111")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber("ACC-2222")).thenReturn(Optional.of(targetAccount));

        assertThatThrownBy(() -> transferService.transfer(request, senderPrincipal))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Simulated Mid-Transfer System Crash");

        // Target was never credited and transaction was never saved
        verify(transactionRepository, never()).save(any());
    }
}
