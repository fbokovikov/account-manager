package account.service.db;

import account.exception.AccountApiBadRequest;
import account.model.Account;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Extract Account from ResultSet.
 *
 * @author fbokovikov
 */
class AccountResultSetExtractor {

    Account extract(ResultSet rs) {
        try {
            if (rs.next()) {
                var amount = rs.getBigDecimal("amount");
                var id = rs.getLong("id");
                return new Account.Builder()
                        .setId(id)
                        .setAmount(amount)
                        .build();
            } else {
                throw new AccountApiBadRequest("Account not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
