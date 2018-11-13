package account.modules;

import com.google.inject.AbstractModule;

/**
 * @author fbokovikov
 */
public class MainModule extends AbstractModule {

    protected void configure() {
        install(new DataSourceModule());
        install(new AccountServiceModule());
    }
}
