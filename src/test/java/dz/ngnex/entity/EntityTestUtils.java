package dz.ngnex.entity;

import dz.ngnex.bean.AccountDemandBean;
import dz.ngnex.bean.IntegrityException;
import dz.ngnex.bean.PrincipalBean;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class EntityTestUtils {

  private EntityTestUtils() throws InstantiationException {
    throw new InstantiationException();
  }

  public static AdminEntity createSuperAdmin(String name, PrincipalBean principalBean) {
    try {
      return principalBean.createAdmin(name, "test_password", AccessType.SUPER_ADMIN.getSecurityRole());
    } catch (IntegrityException e) {
      throw new RuntimeException("could not create admin", e);
    }
  }

  public static void updateAssociationTwoTimes(BasicAssociationEntity association, PrincipalBean principalBean) {
    principalBean.clear();

    association.setAdresse("updated_address");
    principalBean.updateAssociationInfo(association);

    association.setAdresse("new_updated_address");
    principalBean.updateAssociationInfo(association);
  }

  public static SportAssociationEntity createSportAssociation(String name, Integer demandID, PrincipalBean principalBean) {
    try {
      return principalBean.createSportAssociation(name, "test_password", "test_desc", demandID);
    } catch (IntegrityException e) {
      throw new RuntimeException("could not create sport association", e);
    }
  }

  public static void assertNewAssoCanBeLoaded(BasicAssociationEntity saved, PrincipalBean principalBean) throws IntegrityException {
    principalBean.clear();
    BasicAssociationEntity loaded = principalBean.getPrincipal(saved.getReference());
    assertThat(loaded.getName(), equalTo(saved.getName()));
    assertThat(loaded.getPassword(), equalTo(saved.getPassword()));
    assertThat(loaded.getDescription(), equalTo(saved.getDescription()));
    assertThat(loaded.getAgrement(), isEmptyString());
    assertThat(loaded.getAdresse(), isEmptyString());
    assertThat(loaded.getPresident(), isEmptyString());
    assertThat(loaded.getPhone(), isEmptyString());
  }

  public static YouthAssociationEntity createYouthAssociation(String name, Integer demandID, PrincipalBean principalBean) {
    try {
      return principalBean.createYouthAssociation(name, "test_password", "test_desc", demandID);
    } catch (IntegrityException e) {
      throw new RuntimeException("could not create youth association", e);
    }
  }

  public static AccountDemandEntity createAccountDemand(String description, AccountDemandBean accountDemandBean) {
    AccountDemandEntity demand = new AccountDemandEntity()
        .setDescription(description)
        .setAdresse("demand address")
        .setPresident("demand president")
        .setPhone("demand phone")
        .setAgrement("demand agreement number");

    return accountDemandBean.send(demand);
  }

  public static void assertSavedAssociationContainsDemandInfo(BasicAssociationEntity saved, AccountDemandEntity demand, PrincipalBean principalBean) throws IntegrityException {
    principalBean.clear();
    BasicAssociationEntity loaded = principalBean.getPrincipal(saved.getReference());
    assertThat(loaded.getName(), equalTo(saved.getName()));
    assertThat(loaded.getPassword(), equalTo(saved.getPassword()));
    assertThat(loaded.getDescription(), equalTo(demand.getDescription()));
    assertThat(loaded.getAgrement(), equalTo(demand.getAgrement()));
    assertThat(loaded.getAdresse(), equalTo(demand.getAdresse()));
    assertThat(loaded.getPresident(), equalTo(demand.getPresident()));
    assertThat(loaded.getPhone(), equalTo(demand.getPhone()));
  }

  public static <T extends DatabaseEntity> void assertEntityPersistedLoadedAndDeleted(Supplier<T> persistedEntitySupplier,
                                                                                      Runnable contextCleaner,
                                                                                      Supplier<Collection<T>> allEntitiesLoader,
                                                                                      Consumer<T> deleter) {
    T entity = persistedEntitySupplier.get();
    assertThat(entity.getId(), notNullValue());

    contextCleaner.run();
    Collection<T> entities = allEntitiesLoader.get();
    int entitiesCount = entities.size();
    assertThat(entitiesCount, greaterThan(0));

    contextCleaner.run();
    deleter.accept(entity);
    assertThat(allEntitiesLoader.get(), hasSize(entitiesCount - 1));
  }

  public static int countActiveContractInstances(EntityManager em, Integer associationID, Integer seasonID) {
    return em.createQuery("select count(c) from ContractInstanceEntity c where c.association.id = :associationID and c.state = :state and c.contractTemplate.season.id = :seasonID", Long.class)
        .setParameter("associationID", associationID)
        .setParameter("state", ContractInstanceState.ACTIVE)
        .setParameter("seasonID", seasonID)
        .getSingleResult()
        .intValue();
  }
}
