package dz.ngnex.entity;

import dz.ngnex.bean.AccountDemandBean;
import dz.ngnex.bean.IntegrityException;
import dz.ngnex.bean.PrincipalBean;
import dz.ngnex.testkit.DatabaseTest;
import org.junit.Test;

import javax.ejb.EJB;
import javax.persistence.OptimisticLockException;

public class PrincipalBeanTest extends DatabaseTest {

  @EJB
  private PrincipalBean principalBean;

  @EJB
  private AccountDemandBean accountDemandBean;

  @Test(expected = OptimisticLockException.class)
  public void staleUpdateOnAdminShouldFail() {
    AdminEntity admin = EntityTestUtils.createSuperAdmin("staleUpdateOnAdmin", principalBean);
    principalBean.clear();

    admin.setPassword("updated_password");
    principalBean.updateAdminInfo(admin);

    admin.setPassword("new_updated_password");
    principalBean.updateAdminInfo(admin);
  }

  @Test(expected = OptimisticLockException.class)
  public void staleUpdateOnSportAssociationShouldFail() {
    BasicAssociationEntity association = EntityTestUtils.createSportAssociation("staleUpdateOnSportAssociation", null, principalBean);
    EntityTestUtils.updateAssociationTwoTimes(association, principalBean);
  }

  @Test(expected = OptimisticLockException.class)
  public void staleUpdateOnYouthAssociationShouldFail() {
    BasicAssociationEntity association = EntityTestUtils.createYouthAssociation("staleUpdateOnYouthAssociation", null, principalBean);
    EntityTestUtils.updateAssociationTwoTimes(association, principalBean);
  }

  @Test
  public void createdSportAssociationShouldBePersisted() throws IntegrityException {
    SportAssociationEntity association = EntityTestUtils.createSportAssociation("createdSportAssociation", null, principalBean);
    EntityTestUtils.assertNewAssoCanBeLoaded(association, principalBean);
  }

  @Test
  public void createdYouthAssociationShouldBePersisted() throws IntegrityException {
    YouthAssociationEntity association = EntityTestUtils.createYouthAssociation("createdYouthAssociation", null, principalBean);
    EntityTestUtils.assertNewAssoCanBeLoaded(association, principalBean);
  }

  @Test
  public void adminsShouldBeLoadedAndDeleted() {
    EntityTestUtils.assertEntityPersistedLoadedAndDeleted(
        () -> EntityTestUtils.createSuperAdmin("admin_delete_test", principalBean),
        () -> principalBean.clear(),
        () -> principalBean.getAllAdmins(),
        entity -> principalBean.deleteAdmin(entity.getId()));
  }

  @Test
  public void sportAssociationsShouldBeLoadedAndDeleted() {
    EntityTestUtils.assertEntityPersistedLoadedAndDeleted(
        () -> EntityTestUtils.createSportAssociation("sport_delete_test", null, principalBean),
        () -> principalBean.clear(),
        () -> principalBean.getAllSportAssociations(),
        entity -> principalBean.deleteAssociation(entity.getReference()));
  }

  @Test
  public void youthAssociationsShouldBeLoadedAndDeleted() {
    EntityTestUtils.assertEntityPersistedLoadedAndDeleted(
        () -> EntityTestUtils.createYouthAssociation("youth_delete_test", null, principalBean),
        () -> principalBean.clear(),
        () -> principalBean.getAllYouthAssociations(),
        entity -> principalBean.deleteAssociation(entity.getReference()));
  }

  @Test
  public void allAssociationsShouldBeLoadedAndDeleted() {
    EntityTestUtils.assertEntityPersistedLoadedAndDeleted(
        () -> EntityTestUtils.createSportAssociation("basic_delete_test", null, principalBean),
        () -> principalBean.clear(),
        () -> principalBean.getAllAssociations(),
        entity -> principalBean.deleteAssociation(entity.getReference()));
  }

  @Test(expected = IntegrityException.class)
  public void creatingAdminWithDuplicatedNameShouldFail() throws IntegrityException {
    String name = "duplicated_admin_name";
    principalBean.createAdmin(name, "test_password0", AccessType.SUPER_ADMIN.getSecurityRole());
    principalBean.createAdmin(name, "test_password1", AccessType.SUPER_ADMIN.getSecurityRole());
  }

  @Test(expected = IntegrityException.class)
  public void creatingSportAssoWithDuplicatedNameShouldFail() throws IntegrityException {
    String name = "duplicated_sport_asso_name";
    principalBean.createSportAssociation(name, "test_password0", "test_desc0", null, null);
    principalBean.createSportAssociation(name, "test_password1", "test_desc1", null, null);
  }

  @Test(expected = IntegrityException.class)
  public void creatingYouthAssoWithDuplicatedNameShouldFail() throws IntegrityException {
    String name = "duplicated_youth_asso_name";
    principalBean.createYouthAssociation(name, "test_password0", "test_desc0", null, null);
    principalBean.createYouthAssociation(name, "test_password1", "test_desc1", null, null);
  }

  @Test
  public void createdSportAssociationFromDemandShouldBePersisted() throws IntegrityException {
    AccountDemandEntity demand = EntityTestUtils.createAccountDemand("test_sport_asso_demand", accountDemandBean);
    SportAssociationEntity association = EntityTestUtils.createSportAssociation("createdSportAssociationFromDemand", demand.getId(), principalBean);
    EntityTestUtils.assertSavedAssociationContainsDemandInfo(association, demand, principalBean);
  }

  @Test
  public void createdYouthAssociationFromDemandShouldBePersisted() throws IntegrityException {
    AccountDemandEntity demand = EntityTestUtils.createAccountDemand("test_youth_asso_demand", accountDemandBean);
    YouthAssociationEntity association = EntityTestUtils.createYouthAssociation("createdYouthAssociationFromDemand", demand.getId(), principalBean);
    EntityTestUtils.assertSavedAssociationContainsDemandInfo(association, demand, principalBean);
  }
}
