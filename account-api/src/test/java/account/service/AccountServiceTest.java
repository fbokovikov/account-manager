package account.service;

import account.db.DatabasePopulator;
import account.exception.AccountApiBadRequest;
import account.matchers.AccountMatcher;
import account.model.Account;
import account.module.UnitTestModule;
import account.modules.AccountServiceModule;
import account.modules.DataSourceModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Unit test for {@link AccountService}.
 *
 * @author fbokovikov
 */
public class AccountServiceTest {

    private static final Injector INJECTOR = Guice.createInjector(new UnitTestModule());

    private AccountService accountService;

    @BeforeAll
    static void initDb() {
        INJECTOR.getInstance(DatabasePopulator.class).populateDbTables();
        AccountService accountService = INJECTOR.getInstance(AccountService.class);
        accountService.createAccount(new BigDecimal("15.10"));
        accountService.createAccount(new BigDecimal("44.33"));
        accountService.createAccount(new BigDecimal("46.00"));
    }

    @BeforeEach
    void initService() {
        accountService = INJECTOR.getInstance(AccountService.class);
    }

    @Test
    @DisplayName("Account not found")
    void accountNotFound() {
        Optional<Account> account = accountService.getAccount(10L);
        Assertions.assertFalse(
                account.isPresent()
        );
    }

    @Test
    @DisplayName("Account is found")
    void accountFound() {
        Optional<Account> account = accountService.getAccount(2L);
        MatcherAssert.assertThat(
                account.get(),
                new AccountMatcher(
                        new Account.Builder()
                                .setId(2L)
                                .setAmount(new BigDecimal(44.33))
                                .build()
                )
        );
    }

    @Test
    @DisplayName("Account creating")
    void createAccount() {
        Account account = accountService.createAccount(new BigDecimal(10.125));
        MatcherAssert.assertThat(
                account,
                new AccountMatcher(
                        new Account.Builder()
                                .setId(4L)
                                .setAmount(new BigDecimal(10.125))
                                .build()
                )
        );
        Assertions.assertTrue(
                accountService.getAccount(4L).isPresent()
        );
    }

    @Test
    @DisplayName("Creating with negative amount")
    void createWithNegativeAmount() {
        AccountApiBadRequest accountApiBadRequest = Assertions.assertThrows(
                AccountApiBadRequest.class,
                () -> accountService.createAccount(BigDecimal.ONE.negate())
        );
        Assertions.assertEquals(
                "Can not create account with negative amount",
                accountApiBadRequest.getMessage()
        );
    }

    @Test
    @DisplayName("Deposit negative amount")
    void depositNegativeAmount() {
        AccountApiBadRequest accountApiBadRequest = Assertions.assertThrows(
                AccountApiBadRequest.class,
                () -> accountService.deposit(1L, BigDecimal.ONE.negate())
        );
        Assertions.assertEquals(
                "Expecting amount greater than 0 for deposit",
                accountApiBadRequest.getMessage()
        );
    }

    @Test
    @DisplayName("Deposit account not found")
    void accountNotFoundForDeposit() {
        AccountApiBadRequest accountApiBadRequest = Assertions.assertThrows(
                AccountApiBadRequest.class,
                () -> accountService.deposit(100L, BigDecimal.ONE)
        );
        Assertions.assertEquals(
                "Account not found",
                accountApiBadRequest.getMessage()
        );
    }

    @Test
    @DisplayName("Successful deposit")
    void successfulDeposit() {
        Account account = accountService.deposit(1L, new BigDecimal(100.50));

        Account expected = new Account.Builder()
                .setId(1L)
                .setAmount(new BigDecimal(115.60))
                .build();
        MatcherAssert.assertThat(
                account,
                AccountMatcher.equals(expected)
        );
        Optional<Account> foundAccount = accountService.getAccount(1L);
        MatcherAssert.assertThat(
                foundAccount.get(),
                AccountMatcher.equals(expected)
        );
    }

    @Test
    @DisplayName("Withdrawal positive amount")
    void withdrawalPositiveAmount() {
        AccountApiBadRequest accountApiBadRequest = Assertions.assertThrows(
                AccountApiBadRequest.class,
                () -> accountService.withdraw(1L, BigDecimal.ONE)
        );
        Assertions.assertEquals(
                "Expecting amount less than 0 for withdrawal",
                accountApiBadRequest.getMessage()
        );
    }

    @Test
    @DisplayName("Withdrawal account not found")
    void accountNotFoundForWithdrawal() {
        AccountApiBadRequest accountApiBadRequest = Assertions.assertThrows(
                AccountApiBadRequest.class,
                () -> accountService.withdraw(100L, BigDecimal.ONE.negate())
        );
        Assertions.assertEquals(
                "Account not found",
                accountApiBadRequest.getMessage()
        );
    }

    @Test
    @DisplayName("Withdrawal too big amount")
    void withdrawalTooBigAmount() {
        AccountApiBadRequest accountApiBadRequest = Assertions.assertThrows(
                AccountApiBadRequest.class,
                () -> accountService.withdraw(3, new BigDecimal("100").negate())
        );
        Assertions.assertEquals(
                "Not enough amount for transfer",
                accountApiBadRequest.getMessage()
        );
    }

    @Test
    @DisplayName("Successful withrawal")
    void successfulWithdrawal() {
        Account account = accountService.withdraw(3L, new BigDecimal(20).negate());

        Account expected = new Account.Builder()
                .setId(3L)
                .setAmount(new BigDecimal(26))
                .build();
        MatcherAssert.assertThat(
                account,
                AccountMatcher.equals(expected)
        );
        Optional<Account> foundAccount = accountService.getAccount(3L);
        MatcherAssert.assertThat(
                foundAccount.get(),
                AccountMatcher.equals(expected)
        );
    }
}
