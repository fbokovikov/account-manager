package account.model;

import javax.annotation.concurrent.Immutable;

/**
 * Returns when api request processing finished with exception.
 *
 * @author fbokovikov
 */
@Immutable
public class ResponseError {

    private final String message;

    public ResponseError(String message) {
        this.message = message;
    }
}
