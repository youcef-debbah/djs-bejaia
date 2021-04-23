package dz.ngnex.entity;

import dz.ngnex.bean.AccountDemandBean;
import dz.ngnex.testkit.DatabaseTest;
import org.junit.Test;

import javax.ejb.EJB;
import javax.transaction.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AccountDemandBeanTest extends DatabaseTest {

  @EJB
  private AccountDemandBean accountDemandBean;

  @Test(expected = IllegalArgumentException.class)
  public void sendingNullDemandShouldFail() {
    accountDemandBean.send(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void deletingNullDemandShouldFail() {
    accountDemandBean.delete(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void findingDemandWithNullKeyShouldFail() {
    accountDemandBean.find(null);
  }

  @Test
  public void demandsShouldBeLoadedAndDeleted() {
    EntityTestUtils.assertEntityPersistedLoadedAndDeleted(() -> EntityTestUtils.createAccountDemand("delete_test", accountDemandBean),
        () -> accountDemandBean.clear(),
        () -> accountDemandBean.getDemands(),
        entity -> accountDemandBean.delete(entity));
  }

  @Test
  public void findingSingleDemand() {
    AccountDemandEntity demand = EntityTestUtils.createAccountDemand("find_test", accountDemandBean);
    accountDemandBean.clear();

    AccountDemandEntity retriedDemand = accountDemandBean.find(demand.getId());

    assertThat(retriedDemand.getId(), notNullValue());
    assertThat(retriedDemand.getId(), equalTo(demand.getId()));
  }
}
