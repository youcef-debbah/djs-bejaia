package dz.ngnex.testkit;

import bitronix.tm.resource.jdbc.PoolingDataSource;
import org.junit.rules.ExternalResource;

import javax.enterprise.inject.spi.BeanManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Properties;

public final class DatabaseResource extends ExternalResource {

    public static final String TEST_DATASOURCE_JNDI = "jdbc/TestDS";
    public static final String TEST_PERSISTENCE_UNITE = "test_djs";


    private static volatile EntityManagerFactory entityManagerFactory;
    private static volatile boolean resourceEnabled;
    private static final Object MUTEX = new Object();

    @Override
    protected void before() {
        resourceEnabled = true;
    }

    public static EntityManagerFactory getCurrentEntityManagerFactory(BeanManager beanManager) {
        if (!resourceEnabled)
            throw new IllegalStateException("The rule 'DatabaseResource' is not added as a @ClassRule");

        if (entityManagerFactory == null)
            synchronized (MUTEX) {
                if (entityManagerFactory == null)
                    entityManagerFactory = newEntityManagerFactory(beanManager);
            }

        return entityManagerFactory;
    }

    private static EntityManagerFactory newEntityManagerFactory(BeanManager beanManager) {

        // set c3p0 log to warning to avoid the annoying c3p0 logs
        System.setProperty("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
        System.setProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "WARNING");

        Properties properties = initDataSource();

        properties.put("javax.persistence.bean.manager", beanManager);

        properties.put(TestConfig.DATABASE_INIT.getPropertyName(), TestConfig.DATABASE_INIT.getValue());
        properties.put("javax.persistence.schema-generation.create-source", "metadata-then-script");
        properties.put("javax.persistence.schema-generation.drop-source", "script");

        properties.put("hibernate.globally_quoted_identifiers", "true");
        properties.put("hibernate.globally_quoted_identifiers_skip_column_definitions", "true");

        properties.put("hibernate.jdbc.batch_size", "30");

        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.format_sql", "false");
        properties.put("hibernate.use_sql_comments", "true");

        return Persistence.createEntityManagerFactory(TEST_PERSISTENCE_UNITE, properties);
    }

    private static Properties initDataSource() {
        final Properties properties = new Properties();
        final PoolingDataSource ds = new PoolingDataSource();

        TestConfig.DATABASE.setup(properties, ds);

        // Configure the pool
        // https://github.com/bitronix/btm/wiki/JDBC-pools-configuration
        ds.setAllowLocalTransactions(true); // default is false and good for production; true allows Hibernate to execute DDLs
        ds.setMinPoolSize(1); // default is 0
        ds.setMaxPoolSize(Runtime.getRuntime().availableProcessors());
        ds.setShareTransactionConnections(true); // defaults is false only for backwards compatibility
        ds.setEnableJdbc4ConnectionTest(true);

        // Make the data source available to Hibernate via JNDI
        ds.setUniqueName(TEST_DATASOURCE_JNDI);
        ds.init();

        return properties;
    }
}