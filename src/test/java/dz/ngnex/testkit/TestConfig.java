package dz.ngnex.testkit;

public interface TestConfig {
    DatabaseAction DATABASE_INIT = DatabaseAction.DROP_AND_CREATE;
    Database DATABASE = Database.H2_IN_FILE;
    boolean LOG_BEAN_CALLS = true;
    boolean LOG_NEW_ENTITIES_CONTEXT = false;

    int SELECT_LIMIT = 1000;
    int BATCH_SIZE = SELECT_LIMIT;

    int WARMUPS = 0;
    int BENCHMARKS = 1;
}
