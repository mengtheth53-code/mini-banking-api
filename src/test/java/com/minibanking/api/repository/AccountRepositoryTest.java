package com.minibanking.api.repository;

import com.minibanking.api.model.Account;
import com.minibanking.api.model.User;
import com.minibanking.api.model.enums.AccountStatus;
import com.minibanking.api.model.enums.AccountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should persist Account linked to User via @ManyToOne relationship")
    void saveAccount_LinkedToUser() {
        User user = userRepository.save(new User(null, "Bunheng Vorn", "bunheng@bank.com", "USER", null));

        Account account = new Account(null, "ACC-10001", AccountType.SAVINGS, new BigDecimal("500.00"), AccountStatus.ACTIVE, user);
        Account savedAccount = accountRepository.save(account);

        assertThat(savedAccount.getId()).isNotNull();
        assertThat(savedAccount.getAccountNumber()).isEqualTo("ACC-10001");
        assertThat(savedAccount.getUser().getId()).isEqualTo(user.getId());
        assertThat(savedAccount.getBalance()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("Cascading: Saving User with addAccount() should automatically persist the Account")
    void cascadeSave_FromUserToAccount() {
        User user = new User(null, "Chanry Mao", "chanry@bank.com", "USER", null);
        Account account = new Account(null, "ACC-20002", AccountType.CHECKING, new BigDecimal("1200.50"), AccountStatus.ACTIVE, null);
        user.addAccount(account);

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        List<Account> accounts = accountRepository.findByUserId(savedUser.getId());
        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getAccountNumber()).isEqualTo("ACC-20002");
    }

    @Test
    @DisplayName("Pagination: Should retrieve paginated and sorted accounts")
    void pagination_ShouldReturnPagedResults() {
        User user = userRepository.save(new User(null, "Owner", "owner@bank.com", "USER", null));

        accountRepository.save(new Account(null, "ACC-003", AccountType.SAVINGS, new BigDecimal("300"), AccountStatus.ACTIVE, user));
        accountRepository.save(new Account(null, "ACC-001", AccountType.CHECKING, new BigDecimal("100"), AccountStatus.ACTIVE, user));
        accountRepository.save(new Account(null, "ACC-002", AccountType.SAVINGS, new BigDecimal("200"), AccountStatus.ACTIVE, user));

        // Request Page 0 with PageSize 2, sorted by accountNumber ASC
        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by("accountNumber").ascending());
        Page<Account> page = accountRepository.findByUserId(user.getId(), pageRequest);

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).getAccountNumber()).isEqualTo("ACC-001");
        assertThat(page.getContent().get(1).getAccountNumber()).isEqualTo("ACC-002");
    }
}
