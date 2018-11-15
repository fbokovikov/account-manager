package account.module;

import account.modules.AccountServiceModule;
import account.modules.DataSourceModule;
import com.google.inject.AbstractModule;

/**
 * @author fbokovikov
 */
public class UnitTestModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new DataSourceModule());
        install(new AccountServiceModule());
    }
}
