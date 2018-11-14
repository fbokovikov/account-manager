package account.controller.dto;

import account.exception.AccountApiBadRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

/**
 * Unit tests for {@link AccountTransactionValidator}.
 *
 * @author fbokovikov
 */
public class AccountTransactionValidatorTest {

    @ParameterizedTest
    @MethodSource("invalidArgs")
    @DisplayName("Invalid transaction objects")
    void notValid(AccountTransactionDTO transactionDTO) {
        Assertions.assertThrows(
                AccountApiBadRequest.class,
                () -> AccountTransactionValidator.validate(transactionDTO)
        );
    }

    static Stream<Arguments> invalidArgs() {
        return Stream.of(
                Arguments.of(new AccountTransactionDTO(10L, 10L, BigDecimal.ONE)),
                Arguments.of(new AccountTransactionDTO(10L, 15L, BigDecimal.ZERO)),
                Arguments.of(new AccountTransactionDTO(10L, 15L, BigDecimal.ONE.negate())),
                Arguments.of(new AccountTransactionDTO(-10L, 15L, BigDecimal.ONE)),
                Arguments.of(new AccountTransactionDTO(10L, -15L, BigDecimal.ONE)),
                Arguments.of(new AccountTransactionDTO(-10L, -15L, BigDecimal.ZERO))
        );
    }

    @ParameterizedTest
    @MethodSource("validArgs")
    @DisplayName("Valid transaction objects")
    void valid(AccountTransactionDTO transactionDTO) {
        Assertions.assertDoesNotThrow(
                () -> AccountTransactionValidator.validate(transactionDTO)
        );
    }

    static Stream<Arguments> validArgs() {
        return Stream.of(
                Arguments.of(new AccountTransactionDTO(10L, 15L, BigDecimal.ONE))
        );
    }
}
