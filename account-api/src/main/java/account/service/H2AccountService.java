package account.service;

import account.exception.AccountApiBadRequest;
import account.model.Account;
import account.model.AccountTransaction;
import account.service.db.AccountDao;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * H2 Database implementation for {@link AccountService}.
 *
 * @author fbokovikov
 */
@ParametersAreNonnullByDefault
public class H2AccountService implements AccountService {

    private final AccountDao accountDao;

    @Inject
    public H2AccountService(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public Account createAccount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountApiBadRequest("Can not create account with negative amount");
        }
        return accountDao.createAccount(amount);
    }

    @Override
    public Optional<Account> getAccount(long accountId) {
        return accountDao.getAccount(accountId);
    }

    @Override
    public Account deposit(long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountApiBadRequest("Expecting amount greater than 0 for deposit");
        }
        return accountDao.updateAmount(accountId, amount);
    }

    @Override
    public Account withdraw(long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) >= 0) {
            throw new AccountApiBadRequest("Expecting amount less than 0 for withdrawal");
        }
        return accountDao.updateAmount(accountId, amount);
    }

    @Override
    public void transaction(AccountTransaction transaction) {
        if (transaction.getFromId() == transaction.getToId()) {
            throw new AccountApiBadRequest("Accounts should be different for transaction");
        }
        if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountApiBadRequest("Transaction amount should be positive");
        }
        accountDao.transaction(transaction);
    }
}
