package account.controller.dto;

import account.exception.AccountApiBadRequest;

import java.math.BigDecimal;

/**
 * Transaction request validator.
 *
 * @author fbokovikov
 */
public final class AccountTransactionValidator {

    private AccountTransactionValidator() {
        throw new UnsupportedOperationException();
    }

    /**
     * @throws account.exception.AccountApiBadRequest if request violates common sense.
     */
    public static void validate(AccountTransactionDTO transactionDTO) {
        validateId(transactionDTO.getFromId());
        validateId(transactionDTO.getToId());
        validateAmount(transactionDTO.getAmount());
    }

    private static void validateId(Long accountId) {
        if (accountId == null) {
            throw new AccountApiBadRequest("Account id is not present");
        }
        if (accountId <= 0) {
            throw new AccountApiBadRequest("Account id should be positive");
        }
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new AccountApiBadRequest("Amount is not present in request");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountApiBadRequest("Transaction amount should be positive");
        }
    }
}
