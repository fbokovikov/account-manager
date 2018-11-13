package account.model;

import com.google.common.base.MoreObjects;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Bank account info.
 *
 * @author fbokovikov
 */
@Immutable
@ParametersAreNonnullByDefault
public class Account {

    private final long id;

    /**
     * Amount in pieces.
     */
    private final BigDecimal amount;

    public Account(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "Account id should not be null!");
        this.amount = MoreObjects.firstNonNull(builder.amount, BigDecimal.ZERO);
    }

    public long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public static class Builder {
        private Long id;
        private BigDecimal amount;

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Account build() {
            return new Account(this);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("amount", amount)
                .toString();
    }
}
