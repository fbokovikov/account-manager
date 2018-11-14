package account.model;

import com.google.common.base.MoreObjects;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Transaction between two accounts.
 *
 * @author fbokovikov
 */
@Immutable
public class AccountTransaction {

    /**
     * Account to withdraw amount.
     */
    private final long fromId;

    /**
     * Account to deposit amount.
     */
    private final long toId;

    private final BigDecimal amount;

    public AccountTransaction(Builder builder) {
        this.fromId = Objects.requireNonNull(builder.fromId);
        this.toId = Objects.requireNonNull(builder.toId);
        this.amount = Objects.requireNonNull(builder.amount);
    }

    public long getFromId() {
        return fromId;
    }

    public long getToId() {
        return toId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public static class Builder {
        private Long fromId;
        private Long toId;
        private BigDecimal amount;

        public Builder setFromId(long fromId) {
            this.fromId = fromId;
            return this;
        }

        public Builder setToId(long toId) {
            this.toId = toId;
            return this;
        }

        public Builder setAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public AccountTransaction build() {
            return new AccountTransaction(this);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("fromId", fromId)
                .add("toId", toId)
                .add("amount", amount)
                .toString();
    }
}
