package dz.ngnex.testkit;

public enum DatabaseAction {
    NONE("none"),
    CREATE("create"),
    DROP_AND_CREATE("drop-and-create"),
    DROP("drop"),
    ;

    private static final String PROPERTY_NAME = "javax.persistence.schema-generation.database.action";
    private final String value;

    DatabaseAction(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getPropertyName() {
        return PROPERTY_NAME;
    }
}
