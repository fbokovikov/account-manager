package account.controller;


import account.controller.dto.AccountTransactionDTO;
import account.controller.dto.AccountTransactionValidator;
import account.controller.transformer.JsonRequestTransformer;
import account.controller.transformer.JsonResponseTransformer;
import account.exception.AccountApiBadRequest;
import account.model.AccountTransaction;
import account.model.ResponseError;
import account.service.AccountService;
import spark.Request;
import spark.Spark;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;

/**
 * Main application with RESTful API
 *
 * @author fbokovikov
 */
public class AccountController implements SparkController {

    private static final String APPLICATION_JSON = "application/json";

    private static final JsonRequestTransformer REQUEST_TRANSFORMER = new JsonRequestTransformer();
    private static final JsonResponseTransformer RESPONSE_TRANSFORMER = new JsonResponseTransformer();

    private final AccountService accountService;

    @Inject
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void init() {
        Spark.post(
                "/accounts",
                (request, response) ->  {
                    BigDecimal amount = extractAmount(request);

                    response.type(APPLICATION_JSON);
                    return accountService.createAccount(amount);
                },
                RESPONSE_TRANSFORMER
        );

        Spark.get(
                "/accounts/:accountId",
                (request, response) -> {
                    long accountId = Long.parseLong(request.params("accountId"));

                    response.type(APPLICATION_JSON);
                    return accountService.getAccount(accountId)
                            .orElseThrow(() -> new AccountApiBadRequest("Account not found"));
                },
                RESPONSE_TRANSFORMER
        );

        Spark.put(
                "/accounts/:accountId/deposits",
                (request, response) -> {
                    BigDecimal amount = extractAmount(request);
                    long accountId = Long.parseLong(request.params("accountId"));

                    response.type(APPLICATION_JSON);
                    return accountService.deposit(accountId, amount);
                },
                RESPONSE_TRANSFORMER
        );

        Spark.put(
                "/accounts/:accountId/withdrawals",
                (request, response) -> {
                    BigDecimal amount = extractAmount(request);
                    long accountId = Long.parseLong(request.params("accountId"));

                    response.type(APPLICATION_JSON);
                    return accountService.withdraw(accountId, amount);
                },
                RESPONSE_TRANSFORMER
        );

        Spark.post(
                "/accounts/transactions",
                (request, response) -> {
                    String body = request.body();
                    AccountTransactionDTO accountTransactionDto =
                            REQUEST_TRANSFORMER.parseBody(body, AccountTransactionDTO.class);
                    AccountTransactionValidator.validate(accountTransactionDto);
                    accountService.transaction(accountTransactionDto.toTransaction());
                    return accountTransactionDto;
                },
                RESPONSE_TRANSFORMER
        );

        Spark.exception(
                AccountApiBadRequest.class,
                (exception, request, response) -> {
                    response.status(HttpServletResponse.SC_BAD_REQUEST);
                    response.type(APPLICATION_JSON);
                    ResponseError error = new ResponseError(exception.getMessage());
                    response.body(RESPONSE_TRANSFORMER.render(error));
                }
        );
    }

    private static BigDecimal extractAmount(Request request) {
        String amountStrValue = request.queryMap("amount").value();
        if (amountStrValue == null) {
            throw new AccountApiBadRequest("Amount is not present in request");
        }
        BigDecimal amount = new BigDecimal(amountStrValue);
        return amount;
    }
}
