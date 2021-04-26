package dz.ngnex.bean;

import dz.ngnex.control.LocaleManager;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.*;
import dz.ngnex.util.Check;
import dz.ngnex.util.TestWithTransaction;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Stateless
@TestWithTransaction
public class DefaultPrincipalBeanImpl implements PrincipalBean {

  @Inject
  private Logger log;

  @Inject
  private Meta meta;

  @Inject
  private LocaleManager localeManager;

  @Inject
  private EntityManager em;

  @Override
  public List<AdminEntity> getAllAdmins() {
    return em.createQuery("from AdminEntity", AdminEntity.class)
        .getResultList();
  }

  @Override
  public List<BasicAssociationEntity> getAllAssociations() {
    return em.createQuery("select a from BasicAssociationEntity a", BasicAssociationEntity.class)
        .getResultList();
  }

  @Override
  public BasicAssociationEntity getSingleAssociation(Integer id) {
    if (id == null)
      return null;
    else {
      List<BasicAssociationEntity> result = em.createQuery("select a from BasicAssociationEntity a where a.id = :id", BasicAssociationEntity.class)
          .setParameter("id", id)
          .getResultList();
      return result.isEmpty() ? null : result.get(0);
    }
  }

  @Override
  public List<BasicAssociationEntity> getAllSportAssociations() {
    return em.createQuery("select a from SportAssociationEntity a", BasicAssociationEntity.class)
        .getResultList();
  }

  @Override
  public List<BasicAssociationEntity> getAllYouthAssociations() {
    return em.createQuery("select a from YouthAssociationEntity a", BasicAssociationEntity.class)
        .getResultList();
  }

  @Override
  public BasicPrincipalEntity findLoggedInPrincipalByName(String name) {
    BasicPrincipalEntity principal = em.createQuery("select p from BasicPrincipalEntity p where p.name = :name", BasicPrincipalEntity.class)
        .setParameter("name", Check.argNotNull(name))
        .setMaxResults(1)
        .getSingleResult();
    principal.setLastLogin(System.currentTimeMillis());
    return principal;
  }

  @Override
  public BasicPrincipalEntity findPrincipalByName(String name) {
    Check.argNotNull(name);
    List<BasicPrincipalEntity> result = em.createQuery("select p from BasicPrincipalEntity p where p.name = :name", BasicPrincipalEntity.class)
        .setParameter("name", name)
        .setMaxResults(1)
        .getResultList();

    return result.isEmpty() ? null : result.get(0);
  }

  @Override
  public AdminEntity findAdmin(Integer id) {
    return em.find(AdminEntity.class, id);
  }

  @Override
  public <T extends BasicAssociationEntity> @Nullable T findSelectedAssociation(EntityReference<T> principalReference) {
    return (principalReference != null) ? loadAssociation(principalReference.getType(), principalReference.getId()) : null;
  }

  @Nullable
  private <T extends BasicAssociationEntity> T loadAssociation(@NotNull Class<T> type, @NotNull Integer id) {
    EntityGraph<?> associationGraph = em.getEntityGraph("loadSelectedAssociation");
    T association = em.find(type, id, Hints.fetchGraph(associationGraph));
    if (association != null) {
      Hibernate.initialize(association.getVirtualSections());
      Map<Integer, ContractInstanceEntity> currentContracts = association.getActiveContractInstances().get(findCurrentSeasonID());
      if (currentContracts != null)
        for (ContractInstanceEntity currentContract : currentContracts.values()) {
          Hibernate.initialize(currentContract.getContractTemplate().getProperties());
          Hibernate.initialize(currentContract.getContractTemplate().getActivities());
        }
    }
    return association;
  }

  @Override
  public Integer findCurrentSeasonID() {
    List<Integer> currentSeasonID = em.createQuery("select s.id from SeasonEntity s order by s.index desc", Integer.class)
        .setMaxResults(1)
        .getResultList();

    if (currentSeasonID.isEmpty())
      return null;
    else
      return currentSeasonID.get(0);
  }

  @Nullable
  @Override
  public <T extends BasicPrincipalEntity> T findPrincipal(EntityReference<T> principalReference) {
    return em.find(principalReference.getType(), principalReference.getId());
  }

  @NotNull
  @Override
  public <T extends BasicPrincipalEntity> T getPrincipal(EntityReference<T> principalReference) throws IntegrityException {
    return DatabaseEntity.requirePersisted(em.find(principalReference.getType(), principalReference.getId()));
  }

  @Override
  public BasicAssociationEntity updateAssociationInfo(BasicAssociationEntity updatedAssociation) {
    updatedAssociation.setLastUpdate(System.currentTimeMillis());
    return em.merge(DatabaseEntity.requireID(updatedAssociation));
  }

  @Override
  public AdminEntity updateAdminInfo(AdminEntity updatedAdmin) {
    return em.merge(DatabaseEntity.requireID(updatedAdmin));
  }

  @Override
  public AdminEntity createAdmin(String name, String password, String role) throws IntegrityException {
    Check.argNotNull(name, password, role);
    ensurePrincipalNameNotUsed(name);

    AdminEntity principal = new AdminEntity(name, password, role);
    principal.setCreationDate(getDateOfToday());

    em.persist(principal);
    return principal;
  }

  @NotNull
  private String getDateOfToday() {
    LocalDate today = LocalDate.now(localeManager.getAdminZone());
    return today.getDayOfMonth() + "-" + today.getMonthValue() + "-" + today.getYear();
  }

  @Override
  public SportAssociationEntity createSportAssociation(String name, String password, String description,
                                                       Integer demandID) throws IntegrityException {
    Check.argNotNull(name, password, description);
    ensurePrincipalNameNotUsed(name);
    SportAssociationEntity association = new SportAssociationEntity(name, password, description);
    return persistAssociation(demandID, association);
  }

  @Override
  public YouthAssociationEntity createYouthAssociation(String name, String password, String description,
                                                       Integer demandID) throws IntegrityException {
    Check.argNotNull(name, password, description);
    ensurePrincipalNameNotUsed(name);
    YouthAssociationEntity association = new YouthAssociationEntity(name, password, description);
    return persistAssociation(demandID, association);
  }

  private <T extends BasicAssociationEntity> T persistAssociation(Integer demandID, T association) {
    association.setCreationDate(getDateOfToday());

    if (demandID != null) {
      AccountDemandEntity demand = em.find(AccountDemandEntity.class, demandID);
      if (demand != null) {
        demand.setName(association.getName());
        association.setDescription(demand.getDescription());
        association.setAdresse(demand.getAdresse());
        association.setAgrement(demand.getAgrement());
        association.setPhone(demand.getPhone());
        association.setPresident(demand.getPresident());
      }
    }

    em.persist(association);
    return association;
  }

  private void ensurePrincipalNameNotUsed(String name) throws IntegrityException {
    if (countPrincipalNameUsage(name) > 0)
      throw new IntegrityException("principal name already used: " + name, "identifierUsed");
  }

  private int countPrincipalNameUsage(String name) {
    return em.createQuery("select association.id from BasicPrincipalEntity association where association.name = :name", Integer.class)
        .setMaxResults(1)
        .setParameter("name", name)
        .getResultList().size();
  }

  @Override
  public void deleteAdmin(Integer adminID) {
    em.remove(em.getReference(AdminEntity.class, adminID));
  }

  @Override
  public void deleteAssociation(EntityReference<? extends BasicAssociationEntity> associationRef) {
    BasicAssociationEntity association = em.find(associationRef.getType(), associationRef.getId());
    if (association != null) {
      String name = association.getName();
      em.createQuery("delete from MessageEntity m where m.senderName = :senderName")
          .setParameter("senderName", name)
          .executeUpdate();
      em.createQuery("delete from AdminMessageEntity m where m.receiverName = :receiverName")
          .setParameter("receiverName", name)
          .executeUpdate();
      em.createQuery("delete from AttachmentInfoEntity f where f.uploader = :uploader")
          .setParameter("uploader", name)
          .executeUpdate();
      em.remove(association);
    }
  }

  @Override
  public <T extends AbstractAssociationEntity> void recalcContractDownloadState(EntityReference<? extends BasicAssociationEntity> associationRef) {
    Check.argNotNull(associationRef);
    em.createQuery("update " + associationRef.getType().getSimpleName() + " asso set "
        + "asso.contractsCount = (select count(c.id) from ContractInstanceEntity c where c.association.id = :associationID and c.state = :activeState), "
        + "asso.downloadedContractsCount = (select count(c.id) from ContractInstanceEntity c where c.association.id = :associationID and c.state = :activeState and c.retrait > 0) "
        + "where asso.id = :associationID")
        .setParameter("activeState", ContractInstanceState.ACTIVE)
        .setParameter("associationID", associationRef.getId())
        .executeUpdate();
  }

  @Override
  public void archiveContractInstance(Integer contractId, EntityReference<? extends BasicAssociationEntity> associationRef) {
    ContractInstanceEntity contractInstance = em.find(ContractInstanceEntity.class, contractId);
    if (contractInstance != null) {
      contractInstance.setState(ContractInstanceState.ARCHIVED);
      recalcContractDownloadState(associationRef);
    }
  }

  @Override
  public void unarchiveContractInstance(Integer contractId, EntityReference<? extends BasicAssociationEntity> associationRef) {
    ContractInstanceEntity contractInstance = em.find(ContractInstanceEntity.class, contractId);
    if (contractInstance != null) {
      contractInstance.setState(ContractInstanceState.ACTIVE);
      recalcContractDownloadState(associationRef);
    }
  }

  @Override
  public BasicPrincipalEntity findPrincipal(Integer id) {
    return em.find(BasicPrincipalEntity.class, id);
  }

  @Override
  public void deleteContractInstance(Integer contractID) {
    ContractInstanceEntity contract = em.find(ContractInstanceEntity.class, Check.argNotNull(contractID));
    deleteContractInstance(contractID, contract.getAssociation().getReference());
  }

  @Override
  public void deleteContractInstance(Integer contractInstanceID,
                                     EntityReference<? extends BasicAssociationEntity> associationRef) {
    Check.argNotNull(contractInstanceID, associationRef);
    em.createQuery("delete from PropertyValueEntity e where e.contract.id = :contractID")
        .setParameter("contractID", contractInstanceID)
        .executeUpdate();

    em.createQuery("delete from BudgetEntity e where e.contract.id = :contractID")
        .setParameter("contractID", contractInstanceID)
        .executeUpdate();

    em.createQuery("delete from GlobalBudgetEntity e where e.contract.id = :contractID")
        .setParameter("contractID", contractInstanceID)
        .executeUpdate();

    em.createQuery("delete from ContractInstanceEntity e where e.id = :contractID")
        .setParameter("contractID", contractInstanceID)
        .executeUpdate();
    recalcContractDownloadState(associationRef);
  }

  @Override
  public void deleteTemplate(Integer contractTemplateID) {
    deleteContractInstances(contractTemplateID);

    em.createQuery("delete from PropertyEntity e where e.contract.id = :contractTemplateID")
        .setParameter("contractTemplateID", contractTemplateID)
        .executeUpdate();

    em.createQuery("delete from ActivityEntity e where e.contract.id = :contractTemplateID")
        .setParameter("contractTemplateID", contractTemplateID)
        .executeUpdate();

    em.createQuery("delete from ContractTemplateEntity e where e.id = :contractTemplateID")
        .setParameter("contractTemplateID", contractTemplateID)
        .executeUpdate();
  }

  @Override
  public Integer addContractInstance(Integer contractTemplateId,
                                     EntityReference<? extends BasicAssociationEntity> associationRef) {
    Check.argNotNull(contractTemplateId, associationRef);
    ContractInstanceEntity newContractInstance = new ContractInstanceEntity();
    newContractInstance.setContractTemplate(em.getReference(ContractTemplateEntity.class, contractTemplateId));
    newContractInstance.setAssignmentDate(System.currentTimeMillis());
    newContractInstance.persist(em, em.getReference(associationRef.getType(), associationRef.getId()));
    recalcContractDownloadState(associationRef);
    return newContractInstance.getId();
  }

  private void deleteContractInstances(Integer templateID) {
    Check.argNotNull(templateID);

    List<Integer> contractInstanceIDs = em.createQuery("select c.id from ContractInstanceEntity c where c.contractTemplate.id = :templateID", Integer.class)
        .setParameter("templateID", templateID)
        .getResultList();

    if (!contractInstanceIDs.isEmpty()) {
      em.createQuery("delete from PropertyValueEntity e where e.contract.id in :contractInstanceIDs")
          .setParameter("contractInstanceIDs", contractInstanceIDs)
          .executeUpdate();

      em.createQuery("delete from BudgetEntity e where e.contract.id in :contractInstanceIDs")
          .setParameter("contractInstanceIDs", contractInstanceIDs)
          .executeUpdate();

      em.createQuery("delete from GlobalBudgetEntity e where e.contract.id in :contractInstanceIDs")
          .setParameter("contractInstanceIDs", contractInstanceIDs)
          .executeUpdate();

      em.createQuery("delete from ContractInstanceEntity c where c.id in :contractInstanceIDs")
          .setParameter("contractInstanceIDs", contractInstanceIDs)
          .executeUpdate();
    }
  }

  @Override
  public List<ContractInstanceEntity> getAllContracts(EntityReference<? extends BasicAssociationEntity> associationRef) {
    Check.argNotNull(associationRef);
    EntityGraph<?> contractGraph = em.getEntityGraph("loadBudgetGraph");
    return em.createQuery("select c from ContractInstanceEntity c where c.association.id = :associationID", ContractInstanceEntity.class)
        .setHint(Hints.FETCH_GRAPH, contractGraph)
        .setParameter("associationID", associationRef.getId())
        .getResultList();
  }

  @Override
  @TestWithTransaction(traceSQL = false)
  public void clear() {
    BeanUtil.clearCache(em);
  }

  @Override
  public BasicAssociationEntity findAssociationForContractDownload(Integer accountID) {
    BasicAssociationEntity association = em.find(BasicAssociationEntity.class, Check.argNotNull(accountID));
    if (association != null)
      Hibernate.initialize(association.getVirtualSections());
    return association;
  }

  @Override
  public BasicAssociationEntity findAssociationForContractView(Integer accountID) {
    return em.find(BasicAssociationEntity.class, Check.argNotNull(accountID));
  }

  @AroundInvoke
  public Object benchmarkCalls(InvocationContext ctx) throws Exception {
    return BeanUtil.benchmarkCall(log, ctx);
  }
}
