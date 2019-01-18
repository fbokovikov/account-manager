package account.service.db;

import account.exception.AccountApiBadRequest;
import account.model.Account;
import account.model.AccountTransaction;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
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
     *     <li>Order accounts by id (prevent locking)</li>
     *     <li>Lock first account</li>
     *     <li>Lock second account</li>
     *     <li>Subtract amount from first account</li>
     *     <li>Add amount to second account</li>
     *     <li>Commit transaction</li>
     * </ol>
     */
    public void transaction(AccountTransaction transaction) {
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            //sort accounts to prevent locking
            long[] accounts = new long[] {transaction.getFromId(), transaction.getToId()};
            Arrays.sort(accounts);

            try {
                Account account1 = lockAccount(connection, accounts[0]);
                Account account2 = lockAccount(connection, accounts[1]);

                Account from = account1.getId() == transaction.getFromId() ? account1 : account2;
                Account to = account1 == from ? account2 : account1;

                var fromAmount = from.getAmount();
                var fromFinalAmount = fromAmount.subtract(transaction.getAmount());
                if (fromFinalAmount.compareTo(BigDecimal.ZERO) < 0) {
                    throw new AccountApiBadRequest("Not enough amount for transfer");
                }

                updateAccountAmount(connection, from.getId(), fromFinalAmount);
                updateAccountAmount(connection, to.getId(), to.getAmount().add(transaction.getAmount()));

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("SQL Exception", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL Exception", e);
        }

    }

    private static Account lockAccount(Connection connection, long accountId) throws SQLException {
        try (var lockStatement = connection.prepareStatement(LOCK_ACCOUNT)){
            lockStatement.setLong(1, accountId);
            try (var resultSet = lockStatement.executeQuery()) {
                return ACCOUNT_EXTRACTOR.extract(resultSet);
            }
        }
    }

    private static void updateAccountAmount(Connection connection, long accountId, BigDecimal amount) throws SQLException {
        try (var updateStatement = connection.prepareStatement(UPDATE_AMOUNT)) {
            updateStatement.setBigDecimal(1, amount);
            updateStatement.setLong(2, accountId);
            updateStatement.executeUpdate();
        }
    }
}
