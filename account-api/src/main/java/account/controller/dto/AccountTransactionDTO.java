package account.controller.dto;

import account.model.AccountTransaction;

import java.math.BigDecimal;

/**
 * @author fbokovikov
 */
public class AccountTransactionDTO {

    private final Long fromId;
    private final Long toId;
    private final BigDecimal amount;

    public AccountTransactionDTO(Long fromId, Long toId, BigDecimal amount) {
        this.fromId = fromId;
        this.toId = toId;
        this.amount = amount;
    }

    public AccountTransaction toTransaction() {
        return new AccountTransaction.Builder()
                .setFromId(fromId)
                .setToId(toId)
                .setAmount(amount)
                .build();
    }

    public Long getFromId() {
        return fromId;
    }

    public Long getToId() {
        return toId;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
