package account.modules;

import account.service.AccountService;
import account.service.H2AccountService;
import com.google.inject.AbstractModule;

/**
 * @author fbokovikov
 */
public class AccountServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AccountService.class).to(H2AccountService.class);
    }
}
