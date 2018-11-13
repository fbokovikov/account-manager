package account.service;

import account.model.Account;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Service for manage accounts (create, get, update amount).
 *
 * @author fbokovikov
 */
public interface AccountService {

    /**
     * Create new account with initial amount.
     *
     * @param amount initial amount on account
     * @return
     */
    Account createAccount(BigDecimal amount);

    /**
     * Get account info.
     *
     * @param accountId account unique id
     * @return found account
     */
    Optional<Account> getAccount(long accountId);

    /**
     * Deposit {@code amount} on account.
     *
     * @param accountId account unique id
     * @param amount    amount to deposit on account
     * @return updated account
     */
    Account deposit(long accountId, BigDecimal amount);

    /**
     * Withdraw {@code amount} from account.
     *
     * @param accountId account unique id
     * @param amount    amount to withdraw from account
     * @return          updated account
     */
    Account withdraw(long accountId, BigDecimal amount);
}
