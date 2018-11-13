package account.controller.transformer;

import com.google.gson.Gson;
import spark.ResponseTransformer;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Response transformer for json view.
 *
 * @author fbokovikov
 */
@ThreadSafe
public class JsonResponseTransformer implements ResponseTransformer {

    /**
     * Thread-safe json converter.
     */
    private static final Gson GSON = new Gson();

    @Override
    public String render(Object model) {
        return GSON.toJson(model);
    }
}
