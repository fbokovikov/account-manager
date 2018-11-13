package account.db;

import org.h2.tools.RunScript;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

/**
 * Populate H2 sequences and tables.
 *
 * @author fbokovikov
 */
public class DatabasePopulator {

    private static final Collection<String> SCRIPTS = Set.of(
            "ACCOUNT.sql"
    );

    private final DataSource dataSource;

    @Inject
    public DatabasePopulator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void populateDbTables() {
        try (var connection = dataSource.getConnection()
        ) {
            for (var script : SCRIPTS) {
                var scriptInputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(script);
                var reader = new InputStreamReader(scriptInputStream);
                RunScript.execute(connection, reader);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("SQL Error while populating H2 tables", e);
        }
    }
}
