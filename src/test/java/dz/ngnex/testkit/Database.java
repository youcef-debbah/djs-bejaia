package dz.ngnex.testkit;

import bitronix.tm.resource.jdbc.PoolingDataSource;

import java.util.Properties;

public enum Database {
  H2_IN_FILE {
    @Override
    public void setup(Properties properties, PoolingDataSource ds) {
      ds.setClassName("org.h2.jdbcx.JdbcDataSource");
      ds.getDriverProperties().setProperty("url", "jdbc:h2:./h2/test_db;TRACE_LEVEL_FIle=2;MODE=MySQL;DATABASE_TO_LOWER=TRUE");
      properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
      addH2Hooks(properties);
    }
  },
  H2_IN_MEMORY {
    @Override
    public void setup(Properties properties, PoolingDataSource ds) {
      ds.setClassName("org.h2.jdbcx.JdbcDataSource");
      ds.getDriverProperties().setProperty("url", "jdbc:h2:mem:test_db;TRACE_LEVEL_FIle=2;MODE=MySQL;DATABASE_TO_LOWER=TRUE");
      properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
      addH2Hooks(properties);
    }
  },
  MYSQL {
    @Override
    public void setup(Properties properties, PoolingDataSource ds) {
      ds.setClassName("com.mysql.cj.jdbc.MysqlXADataSource");
      ds.getDriverProperties().setProperty("user", "root");
      ds.getDriverProperties().setProperty("password", "E16hxuMmVHUzRfne8XfH");
      ds.getDriverProperties().setProperty("url", "jdbc:mysql://localhost:3306/djs_test_db?useSSL=false&useUnicode=yes&characterEncoding=UTF-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&pooling=false&autoReconnect=true&zeroDateTimeBehavior=convertToNull&useLocalSessionState=true&pinGlobalTxToPhysicalConnection=true");
      properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL57Dialect");
      addMysqlHooks(properties);
    }
  };

  public static void addMysqlHooks(Properties properties) {
    properties.put("javax.persistence.schema-generation.create-script-source", "mysql_hooks/create.sql");
    properties.put("javax.persistence.sql-load-script-source", "mysql_hooks/data.sql");
    properties.put("javax.persistence.schema-generation.drop-script-source", "mysql_hooks/drop.sql");
  }

  public static void addH2Hooks(Properties properties) {
    properties.put("javax.persistence.schema-generation.create-script-source", "h2_hooks/create.sql");
    properties.put("javax.persistence.sql-load-script-source", "h2_hooks/data.sql");
    properties.put("javax.persistence.schema-generation.drop-script-source", "h2_hooks/drop.sql");
  }

  public abstract void setup(Properties properties, PoolingDataSource ds);

  public static Database defaultDatabase() {
    return H2_IN_MEMORY;
  }
}
