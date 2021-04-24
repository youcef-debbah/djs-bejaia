package dz.ngnex.meta;

import dz.ngnex.bean.AccountDemandBean;
import dz.ngnex.entity.AccountDemandEntity;
import dz.ngnex.testkit.DatabaseTest;
import org.junit.Test;

import javax.ejb.EJB;

import static org.hamcrest.MatcherAssert.assertThat;

public class TestInjectionScope extends DatabaseTest {

  @EJB
  private AccountDemandBean accountDemandBean;

  @EJB
  private AccountDemandBean accountDemandBean1;

  @EJB
  private AccountDemandBean accountDemandBean2;

  @Test
  public void identityShouldBePreservedInTestMethod() {
    AccountDemandEntity demandEntity = new AccountDemandEntity();
    demandEntity.setDescription("identity test");
    AccountDemandEntity demand = accountDemandBean.send(demandEntity);
    AccountDemandEntity demand1 = accountDemandBean1.find(demand.getId());
    AccountDemandEntity demand2 = accountDemandBean2.find(demand.getId());
    assertThat("injected EJB beans are not sharing the same hibernate context", demand1 == demand2);
  }
}
