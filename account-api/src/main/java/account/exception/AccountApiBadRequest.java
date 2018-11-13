package account.exception;

/**
 * Exception indicates bad request (400) to account-api.
 *
 * @author fbokovikov
 */
public class AccountApiBadRequest extends RuntimeException {

    public AccountApiBadRequest(String message) {
        super(message);
    }

    public AccountApiBadRequest(String message, Throwable cause) {
        super(message, cause);
    }
}
