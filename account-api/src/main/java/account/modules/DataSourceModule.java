package account.modules;

import com.google.inject.AbstractModule;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

/**
 * H2 Guice DataSource Module.
 *
 * @author fbokovikov
 */
public class DataSourceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DataSource.class).toInstance(h2DataSource());
    }

    private DataSource h2DataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        //use DB_CLOSE_DELAY=-1 to keep content after closing last connection to db
        ds.setURL("jdbc:h2:mem:account-db;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("sa");
        return ds;
    }
}
