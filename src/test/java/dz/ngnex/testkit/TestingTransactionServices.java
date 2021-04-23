package dz.ngnex.testkit;

import bitronix.tm.TransactionManagerServices;
import dz.ngnex.util.InjectableByTests;
import org.jboss.weld.transaction.spi.TransactionServices;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

@InjectableByTests
public class TestingTransactionServices implements TransactionServices {

  @Override
  public void registerSynchronization(Synchronization synchronizedObserver) {
    TransactionManagerServices.getTransactionSynchronizationRegistry()
        .registerInterposedSynchronization(synchronizedObserver);
  }

  @Override
  public boolean isTransactionActive() {
    try {
      return getUserTransaction().getStatus() == Status.STATUS_ACTIVE;
    } catch (SystemException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  @Produces
  @Dependent
  public UserTransaction getUserTransaction() {
    return TransactionManagerServices.getTransactionManager();
  }

  @Override
  public void cleanup() {

  }
}