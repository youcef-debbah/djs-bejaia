package dz.ngnex.testkit;

import org.junit.ClassRule;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

public class DatabaseTest extends InjectableTest {
  @ClassRule
  public static DatabaseResource database = new DatabaseResource();

  @Inject
  protected EntityManager em;

  @Inject
  protected UserTransaction tx;
}
