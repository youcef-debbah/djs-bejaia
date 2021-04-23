package dz.ngnex.meta;

import dz.ngnex.util.TestWithTransaction;
import org.apache.logging.log4j.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Objects;

@Stateless
@TestWithTransaction
public class FakeEJB1Impl implements FakeEJB1 {

  @Inject
  private Logger log;

  @Inject
  private EntityManager em;

  @EJB
  private FakeEJB1 fakeEJB1;

  @EJB
  private FakeEJB2 fakeEJB2;

  @Override
  public void assertInjected() {
    Objects.requireNonNull(log);
    Objects.requireNonNull(em);
    Objects.requireNonNull(fakeEJB1);
    Objects.requireNonNull(fakeEJB2);
  }
}
