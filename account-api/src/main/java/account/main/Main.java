package account.main;

import account.controller.AccountController;
import account.db.DatabasePopulator;
import account.modules.MainModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Main class to start account-api.
 *
 * @author fbokovikov
 */
public class Main {

    /**
     * Populate H2 tables and start HTTP api.
     */
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new MainModule());
        DatabasePopulator databasePopulator = injector.getInstance(DatabasePopulator.class);
        databasePopulator.populateDbTables();
        AccountController accountController = injector.getInstance(AccountController.class);
        accountController.init();
    }
}
