package account.service.db;

import account.exception.AccountApiBadRequest;
import account.model.Account;
import account.model.AccountTransaction;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * Database layer for working with accounts.
 *
 * @author fbokovikov
 */
public class AccountDao {

    private static final String CREATE_ACCOUNT = "" +
            "INSERT INTO account(amount) VALUES(?)";

    private static final String GET_ACCOUNT = "" +
            "SELECT id, amount FROM account WHERE id = ?";

    private static final String UPDATE_AMOUNT = "" +
            "UPDATE account SET amount = ? WHERE id = ?";

    private static final String LOCK_ACCOUNT = "" +
            GET_ACCOUNT + " FOR UPDATE";

    private static final AccountResultSetExtractor ACCOUNT_EXTRACTOR = new AccountResultSetExtractor();

    private final DataSource dataSource;

    @Inject
    public AccountDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Account createAccount(BigDecimal amount) {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(CREATE_ACCOUNT, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setBigDecimal(1, amount);
            statement.executeUpdate();
            try (var generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    var accountId = generatedKeys.getLong(1);
                    return new Account.Builder()
                            .setAmount(amount)
                            .setId(accountId)
                            .build();
                } else {
                    throw new AccountApiBadRequest("Can not get generated id key");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL Exception", e);
        }
    }

    public Optional<Account> getAccount(long accountId) {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(GET_ACCOUNT)
        ) {
            statement.setLong(1, accountId);
            try (var rs = statement.executeQuery()) {
                if (rs.next()) {
                    Account account = new Account.Builder()
                            .setId(rs.getLong("id"))
                            .setAmount(rs.getBigDecimal("amount"))
                            .build();
                    return Optional.of(account);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL Exception", e);
        }
    }

    /**
     * Lock account record and add {@code amount} to account
     *
     * @param accountId  unit account id
     * @param amountDiff value to be added on account
     */
    public Account updateAmount(long accountId, BigDecimal amountDiff) {
        try (var connection = dataSource.getConnection();
             var lockStatement = connection.prepareStatement(LOCK_ACCOUNT);
             var updateStatement = connection.prepareStatement(UPDATE_AMOUNT)
        ) {
            connection.setAutoCommit(false);
            lockStatement.setLong(1, accountId);
            try (var rs = lockStatement.executeQuery()) {
                if (rs.next()) {
                    var amountBefore = rs.getBigDecimal("amount");
                    var amountAfter = amountBefore.add(amountDiff);
                    if (amountAfter.compareTo(BigDecimal.ZERO) < 0) {
                        throw new AccountApiBadRequest("Not enough amount for transfer");
                    }

                    updateStatement.setBigDecimal(1, amountAfter);
                    updateStatement.setLong(2, accountId);
                    updateStatement.executeUpdate();

                    connection.commit();

                    return new Account.Builder()
                            .setId(accountId)
                            .setAmount(amountAfter)
                            .build();
                } else {
                    throw new AccountApiBadRequest("Account not found");
                }
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("SQL Exception", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL Exception", e);
        }
    }

    /**
     * Make transaction between two accounts.
     *
     * <ol>
     *     <li>Lock from account</li>
     *     <li>Lock to account</li>
     *     <li>Subtract amount from first account</li>
     *     <li>Add amount to second account</li>
     *     <li>Commit transaction</li>
     * </ol>
     */
    public void transaction(AccountTransaction transaction) {
        try (var connection = dataSource.getConnection();
             var lockStatement = connection.prepareStatement(LOCK_ACCOUNT);
             var updateStatement = connection.prepareStatement(UPDATE_AMOUNT)
        ) {
            connection.setAutoCommit(false);
            lockStatement.setLong(1, transaction.getFromId());
            try (var rsFrom = lockStatement.executeQuery()) {
                Account from = ACCOUNT_EXTRACTOR.extract(rsFrom);
                lockStatement.setLong(1, transaction.getToId());
                try (var rsTo = lockStatement.executeQuery()) {
                    Account to = ACCOUNT_EXTRACTOR.extract(rsTo);

                    var fromAmount = from.getAmount();
                    var fromFinalAmount = fromAmount.subtract(transaction.getAmount());
                    if (fromFinalAmount.compareTo(BigDecimal.ZERO) < 0) {
                        throw new AccountApiBadRequest("Not enough amount for transfer");
                    }

                    updateStatement.setBigDecimal(1, fromFinalAmount);
                    updateStatement.setLong(2, from.getId());

                    updateStatement.executeUpdate();

                    var toAmount = to.getAmount();
                    var toFinalAmount = toAmount.add(transaction.getAmount());

                    updateStatement.setBigDecimal(1, toFinalAmount);
                    updateStatement.setLong(2, to.getId());

                    updateStatement.executeUpdate();

                    connection.commit();
                }
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("SQL Exception", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL Exception", e);
        }

    }
}
