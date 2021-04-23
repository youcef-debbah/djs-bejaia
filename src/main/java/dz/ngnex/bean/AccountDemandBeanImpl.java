package dz.ngnex.bean;

import dz.ngnex.entity.AccountDemandEntity;
import dz.ngnex.entity.DatabaseEntity;
import dz.ngnex.util.Check;
import dz.ngnex.util.TestWithTransaction;
import org.apache.logging.log4j.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import java.util.List;

@Stateless
@TestWithTransaction
public class AccountDemandBeanImpl implements AccountDemandBean {

  @Inject
  private EntityManager em;

  @Inject
  private Logger log;

  @Override
  public AccountDemandEntity send(AccountDemandEntity demand) {
    Check.argNotNull(demand);
    demand.setCreated(System.currentTimeMillis());
    demand.setId(null);
    demand.setName(null);
    demand.setVersion(0);
    em.persist(demand);
    return demand;
  }

  @Override
  public List<AccountDemandEntity> getDemands() {
    return em.createQuery("from AccountDemandEntity", AccountDemandEntity.class)
        .getResultList();
  }

  @Override
  public void delete(AccountDemandEntity entity) {
    DatabaseEntity.requireID(entity);
    em.remove(em.getReference(AccountDemandEntity.class, entity.getId()));
  }

  @Override
  public AccountDemandEntity find(Integer demandID) {
    return em.find(AccountDemandEntity.class, demandID);
  }

  @Override
  @TestWithTransaction(traceSQL = false)
  public void clear() {
    BeanUtil.clearCache(em);
  }

  @AroundInvoke
  public Object benchmarkCalls(InvocationContext ctx) throws Exception {
    return BeanUtil.benchmarkCall(log, ctx);
  }
}
