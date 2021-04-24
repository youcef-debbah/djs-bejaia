package dz.ngnex.entity;

import dz.ngnex.bean.*;
import dz.ngnex.testkit.DatabaseTest;
import org.hibernate.Hibernate;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.ejb.EJB;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static dz.ngnex.testkit.IsInitialized.initialized;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ContractTemplateTest extends DatabaseTest {
  private static final String SOURCE_TEMPLATE_NAME = "source_template";
  private static final String COPY_TEMPLATE_NAME = "copy_template";
  private static Integer SEASON_ID = null;
  private static Integer SOURCE_TEMPLATE_ID = null;
  private static Integer COPY_TEMPLATE_ID = null;
  private static SportAssociationReference[] SPORT_ASSOCIATIONS_FOR_SOURCE = null;
  private static SportAssociationReference[] SPORT_ASSOCIATIONS_FOR_COPY = null;
  private static Integer[] CONTRACT_INSTANCES_FOR_SOURCE = null;
  private static Integer[] CONTRACT_INSTANCES_FOR_COPY = null;
  private static Integer[] ACTIVITIES_FOR_SOURCE = null;

  @EJB
  private ContractBean contractBean;

  @EJB
  private PrincipalBean principalBean;

  @EJB
  private SeasonBean seasonBean;

  @EJB
  private SectionsBean sectionsBean;

  @Test
  public void test01_addTestSeason() throws IntegrityException {
    SEASON_ID = seasonBean.addSeason("ContractBeanTest_Season").getId();
  }

  @Test
  public void test02_addAndUpdateSourceTemplate() throws IntegrityException {
    ContractTemplateEntity sourceTemplate = contractBean.addNewTemplate("temp_source_template_name", null);
    SOURCE_TEMPLATE_ID = sourceTemplate.getId();
    contractBean.clear();

    sourceTemplate.setName(SOURCE_TEMPLATE_NAME);

    String template = "source template <b>html</b>";
    sourceTemplate.setTemplate(template);

    contractBean.updateTemplate(sourceTemplate);
    contractBean.clear();

    ContractTemplateEntity retrievedSourceTemplate = contractBean.getTemplate(SOURCE_TEMPLATE_ID);
    assertThat(retrievedSourceTemplate.getName(), equalTo(SOURCE_TEMPLATE_NAME));
    assertThat(retrievedSourceTemplate.getTemplate(), equalTo(template));
  }

  @Test
  public void test03_addActivitiesToSourceTemplate() throws IntegrityException {
    ContractTemplateEntity sourceTemplate = contractBean.getTemplate(SOURCE_TEMPLATE_ID);
    String[] activitiesNames = {"integrity_test", "update_test", "remove_test"};

    ACTIVITIES_FOR_SOURCE = new Integer[activitiesNames.length];
    for (int i = 0; i < activitiesNames.length; i++)
      ACTIVITIES_FOR_SOURCE[i] = contractBean.addActivity(SOURCE_TEMPLATE_ID, activitiesNames[i]).getId();

    assertThat(sourceTemplate.getProperties(), not(initialized()));
    assertThat(sourceTemplate.getActivities(), not(initialized()));

    Set<String> activityNames = sourceTemplate.getActivities()
        .stream()
        .map(ActivityEntity::getName)
        .collect(Collectors.toSet());

    for (String activitiesName : activitiesNames)
      assertThat("persisted activity not added to the activities set: " + activitiesName,
          activityNames.contains(activitiesName));
  }

  @Test
  public void test04_addPropertiesToSourceTemplate() throws IntegrityException {
    ContractTemplateEntity sourceTemplate = contractBean.getTemplate(SOURCE_TEMPLATE_ID);
    for (InputType inputType : InputType.values())
      contractBean.addProperty(SOURCE_TEMPLATE_ID, inputType.name().toLowerCase() + "_property", inputType);

    assertThat(sourceTemplate.getProperties(), not(initialized()));
    assertThat(sourceTemplate.getActivities(), not(initialized()));

    Set<String> propertiesNames = sourceTemplate.getProperties()
        .stream()
        .map(PropertyEntity::getName)
        .collect(Collectors.toSet());

    for (InputType inputType : InputType.values()) {
      String propertyName = inputType.name().toLowerCase() + "_property";
      assertThat("persisted property not added to the collection: " + propertyName,
          propertiesNames.contains(propertyName));
    }
  }

  @Test
  public void test05_updateSourceProperties() throws IntegrityException {
    Set<PropertyEntity> properties = contractBean.getTemplate(SOURCE_TEMPLATE_ID).getProperties();
    Hibernate.initialize(properties);
    contractBean.clear();

    String updated_label = "updated_label";
    for (PropertyEntity property : properties) {
      property.setLabel(updated_label);
      contractBean.updateProperty(property);
    }

    contractBean.clear();

    for (PropertyEntity property : contractBean.getTemplate(SOURCE_TEMPLATE_ID).getProperties())
      assertThat(property.getLabel(), equalTo(updated_label));
  }

  @Test
  public void test06_updateSourceActivates() throws IntegrityException {
    Set<ActivityEntity> activities = contractBean.getTemplate(SOURCE_TEMPLATE_ID).getActivities();
    Hibernate.initialize(activities);
    contractBean.clear();

    String updated_label = "updated_label";
    for (ActivityEntity activity : activities) {
      activity.setLabel(updated_label);
      contractBean.updateActivity(activity);
    }

    contractBean.clear();

    for (ActivityEntity activity : contractBean.getTemplate(SOURCE_TEMPLATE_ID).getActivities())
      assertThat(activity.getLabel(), equalTo(updated_label));
  }

  @Test
  public void test07_createSourceTemplateCopy() throws IntegrityException {
    ContractTemplateEntity copyTemplate = contractBean.addNewTemplate(COPY_TEMPLATE_NAME, SOURCE_TEMPLATE_ID);
    COPY_TEMPLATE_ID = copyTemplate.getId();
    contractBean.clear();

    ContractTemplateEntity sourceTemplate = contractBean.getTemplate(SOURCE_TEMPLATE_ID);
    assertThat(copyTemplate.getProperties(), hasSize(sourceTemplate.getProperties().size()));
    assertThat(copyTemplate.getActivities(), hasSize(sourceTemplate.getActivities().size()));
  }

  @Test
  public void test08_addTestContractInstance() {
    int instancesCount = 3;
    SPORT_ASSOCIATIONS_FOR_SOURCE = new SportAssociationReference[instancesCount];
    SPORT_ASSOCIATIONS_FOR_COPY = new SportAssociationReference[instancesCount];
    CONTRACT_INSTANCES_FOR_SOURCE = new Integer[instancesCount];
    CONTRACT_INSTANCES_FOR_COPY = new Integer[instancesCount];

    for (int i = 0; i < instancesCount; i++) {
      addAssociationAndContractInstanceToCopy(i);
      addAssociationAndContractInstanceToSource(i);
    }

    addThenUpdateValuesAndBudgets(SOURCE_TEMPLATE_ID, CONTRACT_INSTANCES_FOR_SOURCE);
    addThenUpdateValuesAndBudgets(COPY_TEMPLATE_ID, CONTRACT_INSTANCES_FOR_COPY);
  }

  public void addThenUpdateValuesAndBudgets(Integer templateId, Integer[] contracts) {
    ContractTemplateEntity template = contractBean.getTemplate(templateId);
    List<PropertyValueEntity> values = new ArrayList<>(template.getProperties().size());
    for (PropertyEntity property : template.getProperties())
      values.add(new PropertyValueEntity(property, "test_header", "22"));
    List<GlobalBudgetEntity> budgets = new ArrayList<>(template.getActivities().size());
    for (ActivityEntity activity : template.getActivities())
      budgets.add(new GlobalBudgetEntity(activity, "test_header", BigDecimal.ONE));
    contractBean.clear();

    for (Integer contractInstanceID : contracts) {
      contractBean.updateValues(contractInstanceID, values, budgets);
      contractBean.clear();
    }

    int i = 0;
    for (PropertyValueEntity value : values) {
      switch (i++ % 3) {
        case 1:
          value.setValue("26");
          break;
        case 2:
          value.setValue(null);
          value.setHeader(null);
          break;
      }
    }

    i = 0;
    for (GlobalBudgetEntity budget : budgets) {
      switch (i++ % 3) {
        case 1:
          budget.setBudget(BigDecimal.TEN);
          break;
        case 2:
          budget.setBudget(BigDecimal.ZERO);
          budget.setHeader(null);
          break;
      }
    }

    for (Integer contractInstanceID : contracts) {
      contractBean.updateValues(contractInstanceID, values, budgets);
      contractBean.clear();
    }
  }

  public void addAssociationAndContractInstanceToSource(int index) {
    SportAssociationEntity sportAssociation = EntityTestUtils.createSportAssociation("sport_asso_for_source" + index, null, principalBean);
    SportAssociationReference reference = sportAssociation.getReference();
    CONTRACT_INSTANCES_FOR_SOURCE[index] = principalBean.addContractInstance(SOURCE_TEMPLATE_ID, reference);
    SPORT_ASSOCIATIONS_FOR_SOURCE[index] = reference;
    principalBean.clear();
  }

  public void addAssociationAndContractInstanceToCopy(int index) {
    SportAssociationEntity sportAssociation = EntityTestUtils.createSportAssociation("sport_asso_for_copy" + index, null, principalBean);
    SportAssociationReference reference = sportAssociation.getReference();
    CONTRACT_INSTANCES_FOR_COPY[index] = principalBean.addContractInstance(COPY_TEMPLATE_ID, reference);
    SPORT_ASSOCIATIONS_FOR_COPY[index] = reference;
    principalBean.clear();
  }

  @Test
  public void test09_addSectionsAndBudgets() throws IntegrityException {
    addSectionAndBudgetForAssociation(SPORT_ASSOCIATIONS_FOR_SOURCE[0].getId());
    sectionsBean.clear();
    addSectionAndBudgetForAssociation(SPORT_ASSOCIATIONS_FOR_COPY[0].getId());
  }

  public void addSectionAndBudgetForAssociation(Integer associationID) throws IntegrityException {
    SectionEntity test_section0 = sectionsBean.add(associationID, "test_section0");
    SectionEntity test_section1 = sectionsBean.add(associationID, "test_section1");
    sectionsBean.clear();

    test_section0.setName("test_section0_updated");
    sectionsBean.updateSection(test_section0);
    sectionsBean.clear();

    HashMap<Integer, List<BudgetEntity>> budgets = new HashMap<>();
    SectionEntity section0 = em.getReference(SectionEntity.class, test_section0.getId());
    SectionEntity section1 = em.getReference(SectionEntity.class, test_section1.getId());

    budgets.put(section0.getId(), getFakeBudgets(section0));
    budgets.put(section1.getId(), getFakeBudgets(section1));

    for (Integer contractInstanceID : CONTRACT_INSTANCES_FOR_SOURCE) {
      sectionsBean.updateBudgets(associationID, contractInstanceID, budgets);
      sectionsBean.clear();
    }

    for (Integer sectionID : budgets.keySet()) {
      int i = 0;
      for (BudgetEntity budgetEntity : budgets.get(sectionID))
        switch (i++ % 3) {
          case 1:
            budgetEntity.setBudget(BigDecimal.ONE);
            break;
          case 2:
            budgetEntity.setBudget(BigDecimal.ZERO);
        }
    }

    for (Integer contractInstanceID : CONTRACT_INSTANCES_FOR_SOURCE) {
      sectionsBean.updateBudgets(associationID, contractInstanceID, budgets);
      sectionsBean.clear();
    }
  }

  private List<BudgetEntity> getFakeBudgets(SectionEntity section0) {
    ArrayList<BudgetEntity> result = new ArrayList<>(ACTIVITIES_FOR_SOURCE.length);
    for (Integer activityID : ACTIVITIES_FOR_SOURCE)
      result.add(new BudgetEntity(section0, em.getReference(ActivityEntity.class, activityID), BigDecimal.TEN));

    return result;
  }

  @Test
  public void test10_retrieveActiveContractInstances() throws IntegrityException {
    BasicAssociationEntity association = principalBean.getPrincipal(SPORT_ASSOCIATIONS_FOR_SOURCE[0]);
    Collection<ContractInstanceEntity> contractInstances = association.getActiveContractInstances(SEASON_ID);

    principalBean.clear();
    int contractInstancesCount = EntityTestUtils.countActiveContractInstances(em, association.getId(), SEASON_ID);
    assertThat(contractInstances, hasSize(contractInstancesCount));
  }

//  @Test
//  public void test11_updateTemplateSeason() throws IntegrityException {
//    Integer tempSeasonID = seasonBean.addSeason("ContractBeanTest_Season_temp").getId();
//    seasonBean.clear();
//
//    principalBean.updateContractsSeason(Arrays.asList(
//        new TemplateInfo(SOURCE_TEMPLATE_ID, SOURCE_TEMPLATE_NAME, tempSeasonID),
//        new TemplateInfo(COPY_TEMPLATE_ID, COPY_TEMPLATE_NAME, tempSeasonID))
//    );
//
//    principalBean.clear();
//    assertThat(em.find(ContractTemplateEntity.class, SOURCE_TEMPLATE_ID).getSeason().getId(), equalTo(tempSeasonID));
//    assertThat(em.find(ContractTemplateEntity.class, COPY_TEMPLATE_ID).getSeason().getId(), equalTo(tempSeasonID));
//    principalBean.clear();
//
//    principalBean.updateContractsSeason(Arrays.asList(
//        new TemplateInfo(SOURCE_TEMPLATE_ID, SOURCE_TEMPLATE_NAME, SEASON_ID),
//        new TemplateInfo(COPY_TEMPLATE_ID, COPY_TEMPLATE_NAME, SEASON_ID))
//    );
//
//    principalBean.clear();
//    assertThat(em.find(ContractTemplateEntity.class, SOURCE_TEMPLATE_ID).getSeason().getId(), equalTo(SEASON_ID));
//    assertThat(em.find(ContractTemplateEntity.class, COPY_TEMPLATE_ID).getSeason().getId(), equalTo(SEASON_ID));
//  }

  @Test
  public void test12_findSelectedPrincipal() {
    SportAssociationEntity association = principalBean.findSelectedAssociation(SPORT_ASSOCIATIONS_FOR_SOURCE[0]);
    principalBean.clear();

    assertThat(association, notNullValue());
    assertThat(association.getContractInstances(), initialized());
    assertThat(association.getVirtualSections(), initialized());
    assertThat(association.getActiveContractInstances(), is(notNullValue()));

    for (ContractInstanceEntity contractInstance : association.getContractInstances())
      if (contractInstance.getState() == ContractInstanceState.ACTIVE) {
        assertThat(contractInstance.getGlobalMontant(), greaterThan(BigDecimal.ZERO));
        assertThat(contractInstance.getContractTemplate().getProperties(), is(not(empty())));
        assertThat(contractInstance.getContractTemplate().getActivities(), is(not(empty())));
      }
  }

  @Test
  public void test29_changeSeason() throws IntegrityException {
    Integer newSeasonID = seasonBean.addSeason("ContractBeanTest_temp_Season").getId();

    seasonBean.clear();
    seasonBean.updateCurrentSeason(newSeasonID);

    seasonBean.clear();
    assertThat(principalBean.findCurrentSeasonID(), equalTo(newSeasonID));

    seasonBean.clear();
    seasonBean.updateCurrentSeason(SEASON_ID);

    seasonBean.clear();
    assertThat(principalBean.findCurrentSeasonID(), equalTo(SEASON_ID));
  }

  @Test
  public void test30_deleteOneSection() throws IntegrityException {
    BasicAssociationEntity association = principalBean.getPrincipal(SPORT_ASSOCIATIONS_FOR_SOURCE[0]);
    Set<SectionEntity> sections = association.getVirtualSections();
    int sectionsCount = sections.size();
    Hibernate.initialize(sections);
    principalBean.clear();

    Iterator<SectionEntity> iterator = sections.iterator();
    assertThat("no sections found to test deleting", iterator.hasNext());
    sectionsBean.delete(iterator.next().getId());

    sectionsBean.clear();
    BasicAssociationEntity updatedAssociation = principalBean.getPrincipal(association.getReference());
    assertThat("section not deleted", updatedAssociation.getVirtualSections().size() == (sectionsCount - 1));
  }

  @Test
  public void test31_deleteOnePropertyOneActivity() {
    ContractTemplateEntity template = contractBean.getTemplate(SOURCE_TEMPLATE_ID);

    Iterator<PropertyEntity> properties = template.getProperties().iterator();
    assertThat("no property found to test deleting", properties.hasNext());
    PropertyEntity propertyEntity = properties.next();

    Iterator<ActivityEntity> activities = template.getActivities().iterator();
    assertThat("no activity found to test deleting", activities.hasNext());
    ActivityEntity activityEntity = activities.next();

    contractBean.clear();
    contractBean.deleteProperty(propertyEntity);
    contractBean.clear();
    contractBean.deleteActivity(activityEntity);

    contractBean.clear();

    assertThat(em.find(PropertyEntity.class, propertyEntity.getId()), nullValue());
    assertThat(em.find(ActivityEntity.class, activityEntity.getId()), nullValue());
  }

  @Test
  public void test32_deleteOneContractInstance() {
    principalBean.deleteContractInstance(CONTRACT_INSTANCES_FOR_SOURCE[0], SPORT_ASSOCIATIONS_FOR_SOURCE[0]);
    principalBean.clear();
    assertThat(em.find(ContractInstanceEntity.class, CONTRACT_INSTANCES_FOR_SOURCE[0]), nullValue());
  }

  @Test
  public void test33_deleteOneAssociation() {
    SportAssociationReference associationRef = SPORT_ASSOCIATIONS_FOR_SOURCE[1];
    principalBean.deleteAssociation(associationRef);
    principalBean.clear();
    assertThat(em.find(associationRef.getType(), associationRef.getId()), nullValue());
  }

  @Test
  public void test34_deleteOneContractTemplate() {
    principalBean.deleteTemplate(SOURCE_TEMPLATE_ID);
    principalBean.clear();
    assertThat(em.find(ContractTemplateEntity.class, SOURCE_TEMPLATE_ID), nullValue());
  }

  @Test
  public void test35_deleteSeason() {
    seasonBean.deleteSeason(SEASON_ID);
    seasonBean.clear();
    assertThat(em.find(SeasonEntity.class, SEASON_ID), nullValue());
  }
}
