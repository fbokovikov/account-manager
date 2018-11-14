package account.matchers;

import account.model.Account;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Objects;

/**
 * @author fbokovikov
 */
public class AccountMatcher extends TypeSafeMatcher<Account> {

    private final Account account;

    public AccountMatcher(Account account) {
        this.account = account;
    }

    @Override
    protected boolean matchesSafely(Account that) {
        return Objects.equals(account.getId(), that.getId()) &&
                Objects.equals(account.getAmount().doubleValue(), that.getAmount().doubleValue());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(account.toString());
    }

    public static AccountMatcher equals(Account account) {
        return new AccountMatcher(account);
    }
}
