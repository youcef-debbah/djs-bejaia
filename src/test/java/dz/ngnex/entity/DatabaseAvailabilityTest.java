package dz.ngnex.entity;

import dz.ngnex.testkit.DatabaseTest;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class DatabaseAvailabilityTest extends DatabaseTest {

  @Test
  public void testDatabaseConnection() {
    Object result = em.createNativeQuery("select 'database result'").getSingleResult();
    assertThat(result, equalTo("database result"));
  }
}
