package dz.ngnex.testkit;

import dz.ngnex.bean.BeanUtil;
import dz.ngnex.util.InjectableByTests;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

@InjectableByTests
public class EntityManagerProducer {

  @Produces
  @Singleton
  public EntityManager produce(BeanManager beanManager) {
    EntityManagerFactory entityManagerFactory = DatabaseResource.getCurrentEntityManagerFactory(beanManager);
    EntityManager em = entityManagerFactory.createEntityManager();
    BeanUtil.logAsSqlComment(em, "New context");
    return em;
  }

  public void close(@Disposes EntityManager instance) {
    instance.close();
  }
}
