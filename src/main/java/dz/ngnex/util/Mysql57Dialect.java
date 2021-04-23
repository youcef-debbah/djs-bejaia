package dz.ngnex.util;

import org.hibernate.dialect.MySQL57Dialect;

public class Mysql57Dialect extends MySQL57Dialect {
  @Override
  public String getTableTypeString() {
    return " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ";
  }
}
