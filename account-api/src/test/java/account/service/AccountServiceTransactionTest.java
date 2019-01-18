package account.service;

import account.db.DatabasePopulator;
import account.exception.AccountApiBadRequest;
import account.matchers.AccountMatcher;
import account.model.Account;
import account.model.AccountTransaction;
import account.module.UnitTestModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Unit-tests on {@link AccountService#transaction(AccountTransaction)}.
 *
 * @author fbokovikov
 */
public class AccountServiceTransactionTest {

    private static final Injector INJECTOR = Guice.createInjector(new UnitTestModule());

    private AccountService accountService;

    @BeforeAll
    static void initDb() {
        INJECTOR.getInstance(DatabasePopulator.class).populateDbTables();
        AccountService accountService = INJECTOR.getInstance(AccountService.class);
        accountService.createAccount(new BigDecimal("10"));
        accountService.createAccount(new BigDecimal("20"));
        accountService.createAccount(new BigDecimal("30"));
    }

    @BeforeEach
    void initService() {
        accountService = INJECTOR.getInstance(AccountService.class);
    }

    @ParameterizedTest
    @MethodSource("badCases")
    @DisplayName("Transaction can not be completed")
    void transactionImpossible(AccountTransaction transaction, String errorMessage) {
        AccountApiBadRequest exception = Assertions.assertThrows(
                AccountApiBadRequest.class,
                () -> accountService.transaction(transaction)
        );
        Assertions.assertEquals(
                errorMessage,
                exception.getMessage()
        );
    }

    static Stream<Arguments> badCases() {
        return Stream.of(
                Arguments.of(
                        new AccountTransaction.Builder()
                                .setFromId(10L)
                                .setToId(1L)
                                .setAmount(BigDecimal.ONE)
                                .build(),
                        "Account not found"
                ),
                Arguments.of(
                        new AccountTransaction.Builder()
                                .setFromId(1L)
                                .setToId(10L)
                                .setAmount(BigDecimal.ONE)
                                .build(),
                        "Account not found"
                ),
                Arguments.of(
                        new AccountTransaction.Builder()
                                .setFromId(10L)
                                .setToId(10L)
                                .setAmount(BigDecimal.ONE)
                                .build(),
                        "Accounts should be different for transaction"
                ),
                Arguments.of(
                        new AccountTransaction.Builder()
                                .setFromId(10L)
                                .setToId(10L)
                                .setAmount(BigDecimal.ONE)
                                .build(),
                        "Accounts should be different for transaction"
                ),
                Arguments.of(
                        new AccountTransaction.Builder()
                                .setFromId(1L)
                                .setToId(2L)
                                .setAmount(BigDecimal.ONE.negate())
                                .build(),
                        "Transaction amount should be positive"
                ),
                Arguments.of(
                        new AccountTransaction.Builder()
                                .setFromId(1L)
                                .setToId(2L)
                                .setAmount(BigDecimal.ZERO)
                                .build(),
                        "Transaction amount should be positive"
                ),
                Arguments.of(
                        new AccountTransaction.Builder()
                                .setFromId(1L)
                                .setToId(2L)
                                .setAmount(new BigDecimal("10.5"))
                                .build(),
                        "Not enough amount for transfer"
                )
        );
    }

    @Test
    @DisplayName("Parallel successful transactions")
    void transactions() {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<CompletableFuture<Void>> transactions = new ArrayList<>(3);
        transactions.add(CompletableFuture.runAsync(
                () -> accountService.transaction(
                        new AccountTransaction.Builder()
                                .setFromId(1L)
                                .setToId(2L)
                                .setAmount(new BigDecimal("3"))
                                .build()),
                executorService));
        transactions.add(CompletableFuture.runAsync(
                () -> accountService.transaction(
                        new AccountTransaction.Builder()
                                .setFromId(2L)
                                .setToId(1L)
                                .setAmount(new BigDecimal("4"))
                                .build()),
                executorService));
        transactions.add(CompletableFuture.runAsync(
                () -> accountService.transaction(
                        new AccountTransaction.Builder()
                                .setFromId(1L)
                                .setToId(3L)
                                .setAmount(new BigDecimal("2"))
                                .build()),
                executorService));
        transactions.add(CompletableFuture.runAsync(
                () -> accountService.transaction(
                        new AccountTransaction.Builder()
                                .setFromId(2L)
                                .setToId(3L)
                                .setAmount(new BigDecimal("4"))
                                .build()),
                executorService));
        transactions.forEach(c -> c.join());
        Account expectedFirst = new Account.Builder()
                .setId(1L)
                .setAmount(new BigDecimal(9))
                .build();
        Account expectedSecond = new Account.Builder()
                .setId(2L)
                .setAmount(new BigDecimal(15))
                .build();
        Account expectedThird = new Account.Builder()
                .setId(3L)
                .setAmount(new BigDecimal(36))
                .build();
        MatcherAssert.assertThat(
                accountService.getAccount(1L).get(),
                AccountMatcher.equals(expectedFirst)
        );
        MatcherAssert.assertThat(
                accountService.getAccount(2L).get(),
                AccountMatcher.equals(expectedSecond)
        );
        MatcherAssert.assertThat(
                accountService.getAccount(3L).get(),
                AccountMatcher.equals(expectedThird)
        );
    }
}
