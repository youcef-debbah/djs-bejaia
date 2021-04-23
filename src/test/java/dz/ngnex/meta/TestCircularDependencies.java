package dz.ngnex.meta;

import dz.ngnex.testkit.DatabaseTest;
import org.junit.Test;

import javax.ejb.EJB;

public class TestCircularDependencies extends DatabaseTest {
  
  @EJB
  private FakeEJB1 fakeEJB1;

  @EJB
  private FakeEJB2 fakeEJB2;

  @Test
  public void reflectiveDependencyShould() {
    fakeEJB1.assertInjected();
    fakeEJB2.assertInjected();
  }
}
