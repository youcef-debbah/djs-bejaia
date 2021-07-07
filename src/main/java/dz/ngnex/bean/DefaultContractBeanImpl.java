package dz.ngnex.bean;

import dz.ngnex.entity.*;
import dz.ngnex.util.Check;
import dz.ngnex.util.TemplatedContent;
import dz.ngnex.util.TestWithTransaction;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;

import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import java.util.*;

import static dz.ngnex.util.HtmlCleaner.secureHtml;

@Stateless
@TestWithTransaction
public class DefaultContractBeanImpl implements ContractBean {

  private static final TemplateDetails EMPTY_TEMPLATE = new TemplateDetails(-1, "<contrat-vide>", null, null, null);

  @Inject
  private Logger log;

  @Inject
  private EntityManager em;

  @Override
  public ContractTemplateEntity updateTemplate(ContractTemplateEntity updatedEntity) {
    return updateTemplate(updatedEntity, null);
  }

  @Override
  public ContractTemplateEntity updateTemplate(ContractTemplateEntity updatedEntity, String updater) {
    DatabaseEntity.requireID(updatedEntity);
    updatedEntity.setTemplate(secureHtml(updatedEntity.getTemplate()));
    updatedEntity.setLastUpdate(System.currentTimeMillis());
    updatedEntity.setLastUpdater(updater);
    return em.merge(updatedEntity);
  }

  @Override
  public PropertyEntity updateProperty(PropertyEntity updatedProperty) {
    DatabaseEntity.requireID(updatedProperty);
    updatedProperty.setPrototype(TemplatedContent.newLinesToBrElements(updatedProperty.getPrototype()));
    return em.merge(updatedProperty);
  }

  @Override
  public ActivityEntity updateActivity(ActivityEntity updatedActivity) {
    DatabaseEntity.requireID(updatedActivity);
    updatedActivity.setSectionPrototype(TemplatedContent.newLinesToBrElements(updatedActivity.getSectionPrototype()));
    return em.merge(updatedActivity);
  }

  @Override
  public PropertyEntity addProperty(Integer contractTemplateID, String newPropertyName, InputType newPropertyType) throws IntegrityException {
    Check.argNotNull(contractTemplateID, newPropertyName, newPropertyType);
    DatabaseEntity.checkIdentifierSyntax(newPropertyName);

    ContractTemplateEntity contract = em.getReference(ContractTemplateEntity.class, contractTemplateID);
    checkDuplicatedPropertyName(newPropertyName, contract);

    PropertyEntity newProperty = new PropertyEntity(newPropertyType);
    newProperty.setName(newPropertyName);
    newProperty.setType(newPropertyType);
    newProperty.setIndex(maxPropertyIndex(contract) + 1);

    return newProperty.persist(em, contract);
  }

  private int maxPropertyIndex(ContractTemplateEntity contract) {
    if (Hibernate.isInitialized(contract) && Hibernate.isInitialized(contract.getProperties())) {
      int maxIndex = -1;

      for (PropertyEntity property : contract.getProperties())
        if (property.getIndex() > maxIndex)
          maxIndex = property.getIndex();

      return maxIndex;
    } else {
      Integer maxIndex = em.createQuery("select max(p.index) from PropertyEntity p where p.contract.id = :contractID", Integer.class)
          .setParameter("contractID", contract.getId())
          .getSingleResult();

      return maxIndex == null ? -1 : maxIndex;
    }
  }

  private void checkDuplicatedPropertyName(String name, ContractTemplateEntity contract) throws IntegrityException {
    if (Hibernate.isInitialized(contract) && Hibernate.isInitialized(contract.getProperties())) {
      for (PropertyEntity property : contract.getProperties())
        if (name.equals(property.getName()))
          throw IntegrityException.newIdentifierUsedException(name, PropertyEntity.class);
    } else {
      Long similarNamesCount = em.createQuery("select count(p.id) from PropertyEntity p where p.contract.id = :contractID and p.name = :name", Long.class)
          .setParameter("name", name)
          .setParameter("contractID", contract.getId())
          .getSingleResult();

      if (similarNamesCount > 0)
        throw IntegrityException.newIdentifierUsedException(name, PropertyEntity.class);
    }
  }

  @Override
  public ActivityEntity addActivity(Integer contractTemplateID, String newActivityName) throws IntegrityException {
    Check.argNotNull(contractTemplateID, newActivityName);
    DatabaseEntity.checkIdentifierSyntax(newActivityName);

    ContractTemplateEntity contract = em.getReference(ContractTemplateEntity.class, contractTemplateID);
    checkDuplicatedActivityName(newActivityName, contract);

    ActivityEntity newActivity = new ActivityEntity();
    newActivity.setName(newActivityName);
    newActivity.setLabel(newActivityName);

    return newActivity.persist(em, contract);
  }

  private void checkDuplicatedActivityName(String name, ContractTemplateEntity contract) throws IntegrityException {
    if (Hibernate.isInitialized(contract) && Hibernate.isInitialized(contract.getActivities())) {
      for (ActivityEntity activity : contract.getActivities())
        if (name.equals(activity.getName()))
          throw IntegrityException.newIdentifierUsedException(name, ActivityEntity.class);
    } else {
      Long similarNamesCount = em.createQuery("select count(a.id) from ActivityEntity a where a.contract.id = :contractID and a.name = :name", Long.class)
          .setParameter("name", name)
          .setParameter("contractID", contract.getId())
          .getSingleResult();

      if (similarNamesCount > 0)
        throw IntegrityException.newIdentifierUsedException(name, ActivityEntity.class);
    }
  }

  @Override
  public ContractTemplateEntity addNewTemplate(String newTemplateName, Integer sourceTemplateID) throws IntegrityException {
    return addNewTemplate(newTemplateName, sourceTemplateID, null);
  }

  @Override
  public ContractTemplateEntity renameTemplate(ContractTemplateEntity templateEntity, String newTemplateName, String updater) throws IntegrityException {
    DatabaseEntity.requireID(templateEntity);
    checkTemplateName(newTemplateName);
    templateEntity.setLastUpdate(System.currentTimeMillis());
    templateEntity.setName(newTemplateName);
    templateEntity.setLastUpdater(updater);
    return em.merge(templateEntity);
  }

  @Override
  public ContractTemplateEntity addNewTemplate(String newTemplateName, Integer sourceTemplateID, String updater) throws IntegrityException {
    checkTemplateName(newTemplateName);

    SeasonEntity currentSeason = em.getReference(SeasonEntity.class, getCurrentSeasonID());

    ContractTemplateEntity newTemplate = new ContractTemplateEntity();
    newTemplate.setName(newTemplateName);
    newTemplate.setSeason(currentSeason);

    long creationDate = System.currentTimeMillis();
    newTemplate.setCreationDate(creationDate);
    newTemplate.setLastUpdate(creationDate);
    newTemplate.setLastUpdater(updater);

    em.persist(newTemplate);

    if (sourceTemplateID != null && !sourceTemplateID.equals(EMPTY_TEMPLATE.getId())) {
      ContractTemplateEntity sourceTemplate = em.find(ContractTemplateEntity.class, sourceTemplateID);
      if (sourceTemplate != null) {
        newTemplate.setTemplate(sourceTemplate.getTemplate());
        copyProperties(newTemplate, sourceTemplate);
        copyActivities(newTemplate, sourceTemplate);
      }
    }
    return newTemplate;
  }

  private void checkTemplateName(String newTemplateName) throws IntegrityException {
    Check.argNotNull(newTemplateName);
    DatabaseEntity.checkIdentifierSyntax(newTemplateName);
    checkDuplicatedTemplateName(newTemplateName);
  }

  private void checkDuplicatedTemplateName(String newTemplateName) throws IntegrityException {
    Long alreadyExist = em.createQuery("select count(t.id) from ContractTemplateEntity t where t.name = :name", Long.class)
        .setParameter("name", newTemplateName)
        .getSingleResult();

    if (alreadyExist > 0)
      throw IntegrityException.newIdentifierUsedException(newTemplateName, ContractTemplateEntity.class);
  }

  private void copyProperties(ContractTemplateEntity newTemplate, ContractTemplateEntity sourceTemplate) {
    List<PropertyEntity> properties = new ArrayList<>(sourceTemplate.getProperties());
    Collections.sort(properties);
    for (PropertyEntity sourceProperty : properties) {
      PropertyEntity newProperty = new PropertyEntity();
      newProperty.setType(sourceProperty.getType());
      newProperty.setAccess(sourceProperty.getAccess());
      newProperty.setName(sourceProperty.getName());
      newProperty.setLabel(sourceProperty.getLabel());
      newProperty.setRequired(sourceProperty.getRequired());
      newProperty.setDefaultValue(sourceProperty.getDefaultValue());
      newProperty.setDefaultHeader(sourceProperty.getDefaultHeader());
      newProperty.setPrototype(sourceProperty.getPrototype());
      newProperty.setIndex(sourceProperty.getIndex());
      newProperty.persist(em, newTemplate);
    }
  }

  private void copyActivities(ContractTemplateEntity newTemplate, ContractTemplateEntity sourceTemplate) throws IntegrityException {
    List<ActivityEntity> activities = new ArrayList<>(sourceTemplate.getActivities());
    Collections.sort(activities);
    for (ActivityEntity sourceActivity : activities) {
      ActivityEntity newActivity = new ActivityEntity();
      newActivity.setName(sourceActivity.getName());
      newActivity.setSectionPrototype(sourceActivity.getSectionPrototype());
      newActivity.setHeader(sourceActivity.getHeader());
      newActivity.setLabel(sourceActivity.getLabel());
      newActivity.persist(em, newTemplate);
    }
  }

  @Override
  public SeasonEntity getCurrentSeason() {
    List<SeasonEntity> seasons = em.createQuery("select s from SeasonEntity s order by s.index desc", SeasonEntity.class)
        .setMaxResults(1)
        .getResultList();

    return seasons.isEmpty() ? null : seasons.get(0);
  }

  @Override
  public Integer getCurrentSeasonID() throws IntegrityException {
    List<Integer> currentSeasonID = em.createQuery("select s.id from SeasonEntity s order by s.index desc", Integer.class)
        .setMaxResults(1)
        .getResultList();

    if (currentSeasonID.isEmpty())
      throw new IntegrityException("current season is null", "noCurrentSeason");
    else
      return currentSeasonID.get(0);
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

  @Override
  public TemplateDetails getEmptyTemplateDetails() {
    return EMPTY_TEMPLATE;
  }

  @Override
  public List<TemplateDetails> getSources() {
    List<TemplateDetails> templatesDetails = getTemplatesDetails();
    List<TemplateDetails> result = new ArrayList<>(templatesDetails.size() + 1);
    result.add(EMPTY_TEMPLATE);
    result.addAll(templatesDetails);
    return result;
  }

  @Override
  public List<ContractTemplateEntity> getTemplates() {
    return em.createQuery("select t from ContractTemplateEntity t order by t.lastUpdate desc, t.name asc", ContractTemplateEntity.class)
        .getResultList();
  }

  @Override
  public List<ContractTemplateEntity> getTemplates(Integer seasonID) {
    if (seasonID == null)
      return getTemplates();
    else
      return em.createQuery("select t from ContractTemplateEntity t where t.season.id = :seasonID order by t.lastUpdate desc, t.name asc", ContractTemplateEntity.class)
          .setParameter("seasonID", seasonID)
          .getResultList();
  }

  @Override
  public ContractTemplateEntity getTemplate(Integer id) {
    ContractTemplateEntity entity = em.find(ContractTemplateEntity.class, Check.argNotNull(id));
    if (entity == null)
      throw new EJBException("contract template not found by id: " + id);
    return entity;
  }

  @Override
  public ContractTemplateEntity getSelectedTemplate(Integer id) {
    EntityGraph<?> templateGraph = em.getEntityGraph("loadSelectedTemplate");
    ContractTemplateEntity entity = em.find(ContractTemplateEntity.class, Check.argNotNull(id), Hints.fetchGraph(templateGraph));
    if (entity == null)
      throw new EJBException("contract template not found by id: " + id);
    return entity;
  }

  @Override
  public PropertyEntity getProperty(Integer id) {
    PropertyEntity entity = em.find(PropertyEntity.class, id);
    if (entity == null)
      throw new EJBException("contract property not found by id: " + id);
    return entity;
  }

  @Override
  public void deleteProperty(PropertyEntity property) {
    em.createQuery("delete from PropertyValueEntity v where v.property = :property")
        .setParameter("property", property)
        .executeUpdate();

    em.createQuery("delete from PropertyEntity p where p = :property")
        .setParameter("property", property)
        .executeUpdate();
  }

  @Override
  public void deleteActivity(ActivityEntity activity) {
    em.createQuery("delete from BudgetEntity b where b.activity = :activity")
        .setParameter("activity", activity)
        .executeUpdate();

    em.createQuery("delete from GlobalBudgetEntity b where b.activity = :activity")
        .setParameter("activity", activity)
        .executeUpdate();

    em.createQuery("delete from ActivityEntity a where a = :activity")
        .setParameter("activity", activity)
        .executeUpdate();
  }

  private List<ContractInstanceEntity> getTemplateInstances(Integer templateID) {
    return em.createQuery("select instance from ContractInstanceEntity instance where instance.contractTemplate.id = :templateID", ContractInstanceEntity.class)
        .setParameter("templateID", templateID)
        .getResultList();
  }

  @Override
  public long countContractInstances(Integer templateID) {
    return em.createQuery("select count(instance.id) from ContractInstanceEntity instance where instance.contractTemplate.id = :templateID", Long.class)
        .setParameter("templateID", templateID)
        .getSingleResult();
  }

  @Override
  public ContractTemplateEntity movePropertyUp(Integer templateID, Integer propertyID) {
    ContractTemplateEntity template = getSelectedTemplate(templateID);
    Set<PropertyEntity> rawProperties = template.getProperties();

    if (rawProperties.size() > 1) {
      PropertyEntity targetProperty = DatabaseEntity.get(propertyID, rawProperties);
      if (targetProperty != null) {
        NavigableSet<PropertyEntity> orderedProperties = new TreeSet<>(rawProperties);
        if (targetProperty != orderedProperties.first())
          moveIndexTowardFirst(targetProperty, orderedProperties);
      }
    }

    return template;
  }

  @Override
  public ContractTemplateEntity movePropertyDown(Integer templateID, Integer propertyID) {
    ContractTemplateEntity template = getSelectedTemplate(templateID);
    Set<PropertyEntity> rawProperties = template.getProperties();

    if (rawProperties.size() > 1) {
      PropertyEntity targetProperty = DatabaseEntity.get(propertyID, rawProperties);
      if (targetProperty != null) {
        NavigableSet<PropertyEntity> orderedProperties = new TreeSet<>(rawProperties);
        if (targetProperty != orderedProperties.last())
          moveIndexTowardLast(targetProperty, orderedProperties);
      }
    }

    return template;
  }

  private void moveIndexTowardFirst(PropertyEntity target, NavigableSet<PropertyEntity> elements) {
    PropertyEntity beforeCurrent = Objects.requireNonNull(elements.pollFirst());
    while (!elements.isEmpty()) {
      PropertyEntity current = Objects.requireNonNull(elements.pollFirst());
      if (current == target) {
        swapIndex(beforeCurrent, current);
        return;
      }

      beforeCurrent = current;
    }
  }

  private void moveIndexTowardLast(PropertyEntity target, NavigableSet<PropertyEntity> elements) {
    PropertyEntity beforeCurrent = Objects.requireNonNull(elements.pollLast());
    while (!elements.isEmpty()) {
      PropertyEntity current = Objects.requireNonNull(elements.pollLast());
      if (current == target) {
        swapIndex(beforeCurrent, current);
        return;
      }

      beforeCurrent = current;
    }
  }

  private void swapIndex(PropertyEntity property1, PropertyEntity property2) {
    Integer temp = property1.getIndex();
    property1.setIndex(property2.getIndex());
    property2.setIndex(temp);
  }

  @Override
  public void updateValues(Integer contractInstanceID,
                           List<PropertyValueEntity> potentialValues,
                           List<GlobalBudgetEntity> potentialBudgets) {
    Objects.requireNonNull(potentialValues);
    Objects.requireNonNull(potentialBudgets);
    ContractInstanceEntity contract = em.find(ContractInstanceEntity.class, contractInstanceID);

    contract.setLastUpdate(System.currentTimeMillis());
    potentialValues.removeIf(value -> value.getProperty().getType() == InputType.NOTE);

    updateAssociationProperties(contract, potentialValues);
    updateAssociationBudgets(contract, potentialBudgets);
  }

  private void updateAssociationProperties(ContractInstanceEntity contractInstance,
                                           Collection<PropertyValueEntity> potentialValues) {
    Collection<PropertyValueEntity> persistedValues = contractInstance.getPropertyValues();
    if (persistedValues.isEmpty())
      for (PropertyValueEntity potentialValue : potentialValues)
        addNewValue(contractInstance, potentialValue);
    else {
      Set<PropertyValueEntity> toDelete = new HashSet<>();
      for (PropertyValueEntity potentialValue : potentialValues) {
        PropertyValueEntity persistedValue = PropertyValueEntity.extractValue(potentialValue.getProperty(), persistedValues);
        if (persistedValue != null) {
          if (potentialValue.isNull())
            toDelete.add(persistedValue);
          else
            persistedValue.copyValue(potentialValue);
        } else
          addNewValue(contractInstance, potentialValue);
      }
      PropertyValueEntity.removeAll(em, contractInstance, toDelete);
    }
  }

  private void updateAssociationBudgets(ContractInstanceEntity contractInstance,
                                        Collection<GlobalBudgetEntity> potentialBudgets) {
    Collection<GlobalBudgetEntity> persistedBudgets = contractInstance.getGlobalBudgets();
    if (persistedBudgets.isEmpty())
      for (GlobalBudgetEntity potentialBudget : potentialBudgets)
        addNewGlobalBudget(contractInstance, potentialBudget);
    else {
      Set<GlobalBudgetEntity> toDelete = new HashSet<>();
      for (GlobalBudgetEntity potentialBudget : potentialBudgets) {
        GlobalBudgetEntity persistedBudget = BudgetDatabaseEntity.extractBudget(potentialBudget, persistedBudgets);
        if (persistedBudget != null) {
          if (potentialBudget.isNull())
            toDelete.add(persistedBudget);
          else
            persistedBudget.copyBudget(potentialBudget);
        } else
          addNewGlobalBudget(contractInstance, potentialBudget);
      }
      GlobalBudgetEntity.removeAll(em, contractInstance, toDelete);
    }
  }

  @Override
  public List<PropertyValueEntity> getPropertyValues(Integer contract) {
    return em.createQuery("select v from PropertyValueEntity v where v.contract.id = :contract", PropertyValueEntity.class)
        .setParameter("contract", contract)
        .getResultList();
  }

  private void addNewValue(ContractInstanceEntity contractInstance, PropertyValueEntity potentialValue) {
    if (!potentialValue.isNull()) {
      PropertyValueEntity propertyValueEntity = new PropertyValueEntity(potentialValue.getProperty(), potentialValue.getHeader(), potentialValue.getValue());
      propertyValueEntity.persist(em, contractInstance);
    }
  }

  @Override
  public List<GlobalBudgetEntity> getGlobalBudgets(Integer contractInstanceId) {
    return em.createQuery("select b from GlobalBudgetEntity b where b.contract.id = :contractInstanceId", GlobalBudgetEntity.class)
        .setParameter("contractInstanceId", contractInstanceId)
        .getResultList();
  }

  private void addNewGlobalBudget(ContractInstanceEntity contractInstance, GlobalBudgetEntity potentialBudget) {
    if (!potentialBudget.isNull()) {
      GlobalBudgetEntity globalBudgetEntity = new GlobalBudgetEntity(potentialBudget.getActivity(), potentialBudget.getHeader(), potentialBudget.getBudget());
      globalBudgetEntity.persist(em, contractInstance);
    }
  }

  @Override
  public ContractInstanceEntity findAssignedContract(Integer contractInstanceID) {
    ContractInstanceEntity contractInstance = em.find(ContractInstanceEntity.class, contractInstanceID);

    ContractTemplateEntity contractTemplate = contractInstance.getContractTemplate();
    Hibernate.initialize(contractTemplate);
    Hibernate.initialize(contractTemplate.getProperties());
    Hibernate.initialize(contractTemplate.getActivities());

    Hibernate.initialize(contractInstance.getBudgets());
    Hibernate.initialize(contractInstance.getGlobalBudgets());
    Hibernate.initialize(contractInstance.getPropertyValues());
    Hibernate.initialize(contractInstance.getAssociation());
    return contractInstance;
  }

  @Override
  public void updateAchievements(ContractInstanceEntity contract) {
    ContractInstanceEntity entity = em.find(ContractInstanceEntity.class, contract.getId());
    if (entity != null)
      entity.setAchievement(contract.getAchievement());
  }

  @Override
  public void updateAchievementLevel(ContractInstanceEntity contract) {
    ContractInstanceEntity entity = em.find(ContractInstanceEntity.class, contract.getId());
    if (entity != null)
      entity.setAchievementLevel(contract.getAchievementLevel());
  }

  @Override
  public void resetAllContractInstancesState(Integer currentSeasonID) {
    em.createQuery("update ContractInstanceEntity contract set contract.state = :archivedState")
        .setParameter("archivedState", ContractInstanceState.ARCHIVED)
        .executeUpdate();
//    em.createNativeQuery("update `contract_instance` set `contract_instance`.`state` = :archivedState")
//        .setParameter("archivedState", ContractInstanceState.ARCHIVED.ordinal())
//        .executeUpdate();

    if (currentSeasonID != null) {
      em.createQuery("update ContractInstanceEntity contract set contract.state = :activeState where contract.contractTemplate.id in (select c.id from ContractTemplateEntity c where c.season.id = :currentSeasonID)")
          .setParameter("activeState", ContractInstanceState.ACTIVE)
          .setParameter("currentSeasonID", currentSeasonID)
          .executeUpdate();
//      em.createNativeQuery("update `contract_instance` set `contract_instance`.`state` = :activeState where `contract_instance`.`contract_template` in (select `contract_template`.`contract_template_id` from `contract_template` where `contract_template`.`season` = :currentSeasonID)")
//          .setParameter("activeState", ContractInstanceState.ACTIVE.ordinal())
//          .setParameter("currentSeasonID", currentSeasonID)
//          .executeUpdate();
    }
  }

  private List<ContractTemplateEntity> getTemplates(List<TemplateInfo> contracts) {
    Set<Integer> contractsID = new HashSet<>();

    for (TemplateInfo contract : contracts)
      contractsID.add(contract.getTemplateID());

    if (contractsID.isEmpty())
      return Collections.emptyList();
    else
      return em.createQuery("select t from ContractTemplateEntity t where t.id in :contractsID", ContractTemplateEntity.class)
          .setParameter("contractsID", contractsID)
          .getResultList();
  }

  @Override
  public void updateContractsSeason(List<TemplateInfo> editedContractTemplates, Integer currentSeasonID) {
    Check.argNotNull(editedContractTemplates);

    List<ContractTemplateEntity> persistedTemplates = getTemplates(editedContractTemplates);
    boolean stateChanged = false;

    for (TemplateInfo editedTemplate : editedContractTemplates) {
      ContractTemplateEntity persistedTemplate = DatabaseEntity.get(editedTemplate.getTemplateID(), persistedTemplates);
      if (persistedTemplate != null && !persistedTemplate.getSeason().getId().equals(editedTemplate.getSeasonID())) {
        persistedTemplate.setSeason(em.getReference(SeasonEntity.class, editedTemplate.getSeasonID()));
        stateChanged = true;
      }
    }

    if (stateChanged) {
      resetAllContractInstancesState(currentSeasonID);
      recalcAllContractDownloadState();
    }
  }

  @Override
  public void recalcAllContractDownloadState() {
    em.createQuery("update SportAssociationEntity asso set " +
        "asso.contractsCount = (select count(c.id) from ContractInstanceEntity c where c.association.id = asso.id and c.state = :activeState), "
        + "asso.downloadedContractsCount = (select count(c.id) from ContractInstanceEntity c where c.association.id = asso.id and c.state = :activeState and c.retrait > 0)")
        .setParameter("activeState", ContractInstanceState.ACTIVE)
        .executeUpdate();

    em.createQuery("update YouthAssociationEntity asso set " +
        "asso.contractsCount = (select count(c.id) from ContractInstanceEntity c where c.association.id = asso.id and c.state = :activeState), "
        + "asso.downloadedContractsCount = (select count(c.id) from ContractInstanceEntity c where c.association.id = asso.id and c.state = :activeState and c.retrait > 0)")
        .setParameter("activeState", ContractInstanceState.ACTIVE)
        .executeUpdate();
  }

  @Override
  public List<ContractInstanceEntity> getContractsAssignedBetween(long start, long end) {
    if (end > start)
      return Collections.emptyList();
    else {
      return em.createQuery("select c from ContractInstanceEntity c where c.assignmentDate >= :startDate and c.assignmentDate <= :endDate order by c.assignmentDate", ContractInstanceEntity.class)
          .setParameter("startDate", start)
          .setParameter("endDate", end)
          .getResultList();
    }
  }

  @Override
  public List<TemplateInfo> getTemplatesInfo() {
    return em.createQuery("select new dz.ngnex.entity.TemplateInfo(t.id, t.name, t.season.id) from ContractTemplateEntity t order by t.season.id desc, t.lastUpdate desc, t.name asc", TemplateInfo.class)
        .getResultList();
  }

  @Override
  public List<TemplateInfo> getTemplatesInfo(Integer seasonID) {
    if (seasonID == null)
      return getTemplatesInfo();
    else
      return em.createQuery("select new dz.ngnex.entity.TemplateInfo(t.id, t.name, t.season.id) from ContractTemplateEntity t where t.season.id = :seasonID order by t.lastUpdate desc, t.name asc", TemplateInfo.class)
          .setParameter("seasonID", seasonID)
          .getResultList();
  }

  @Override
  public List<TemplateDetails> getTemplatesDetails(Integer seasonID) {
    if (seasonID == null)
      return getTemplatesDetails();
    else
      return em.createQuery("select new dz.ngnex.entity.TemplateDetails(t.id, t.name, t.version, t.season.id, t.season.name) from ContractTemplateEntity t where t.season = :seasonID order by t.lastUpdate desc, t.name asc", TemplateDetails.class)
          .setParameter("seasonID", seasonID)
          .getResultList();
  }

  @Override
  public List<TemplateDetails> getTemplatesDetails() {
    return em.createQuery("select new dz.ngnex.entity.TemplateDetails(t.id, t.name, t.version, t.season.id, t.season.name) from ContractTemplateEntity t order by t.lastUpdate desc, t.name asc", TemplateDetails.class)
        .getResultList();
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
