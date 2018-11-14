package account.controller.transformer;

import account.exception.AccountApiBadRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Parse json requests to objects.
 *
 * @author fbokovikov
 */
@ThreadSafe
public class JsonRequestTransformer {

    private static final Gson GSON = new Gson();

    public <T> T parseBody(String requestBody, Class<T> tClass) {
        try {
            return GSON.fromJson(requestBody, tClass);
        } catch (JsonSyntaxException jsonException) {
            throw new AccountApiBadRequest("Bad json data");
        }
    }
}
