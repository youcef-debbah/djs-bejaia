package dz.ngnex.testkit;

public abstract class TestConfig {
    public static final DatabaseInitialization DATABASE_INITIALIZATION = DatabaseInitialization.NONE;
    public static final Database DATABASE = Database.MYSQL;
    public static final boolean LOG_BEAN_CALLS = false;
    public static final boolean LOG_NEW_ENTITIES_CONTEXT = false;

    public static final int SELECT_LIMIT = 10000;
    public static final int BATCH_SIZE = 10;

    public static final int WARMUPS = 300;
    public static final int BENCHMARKS = 300;
}
