package dz.ngnex.view;

import dz.ngnex.bean.*;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.*;
import dz.ngnex.util.Messages;
import dz.ngnex.util.ViewModel;
import org.jetbrains.annotations.Nullable;
import org.primefaces.PrimeFaces;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

@ViewModel
public class SectionsView implements Serializable {
  private static final long serialVersionUID = -4387803076096822665L;

  @EJB
  private SectionsBean sectionsBean;

  @EJB
  private ContractBean contractBean;

  @EJB
  private PrincipalBean principalBean;

  @EJB
  private SeasonBean seasonBean;

  @Inject
  private Event<RefreshAccountEvent> refreshEvent;

  private BasicAssociationEntity currentAssociation;

  private ContractInstanceEntity currentContract;
  private BigDecimal currentContractGlobalBudget;

  private SectionEntity currentSection;
  private BigDecimal currentSectionTotalBudget;

  private final List<SectionEntity> sections = new LinkedList<>();
  private final List<ContractInstanceEntity> assignedContracts = new ArrayList<>();
  private final List<TemplateInfo> availableContracts = new ArrayList<>();
  private Integer newContractTemplateID;

  @Size(min = Constrains.Min_IDENTIFIER_SIZE, max = Constrains.MAX_IDENTIFIER_SIZE)
  private String newSectionName;
  private List<EntityReference> availableSectionNames;

  private final Map<Integer, List<BudgetEntity>> potentialBudgetsMap = new HashMap<>();

  @Nullable
  private Integer currentSeasonID;

  private String currentSeasonName;

  @Inject
  private Meta meta;

  @Inject
  private Messages messages;
  private List<TemplateInfo> contractTemplates;

  @PostConstruct
  private void init() {
    initCurrentSeason();
    updateState(null, null, null);
  }

  private void initCurrentSeason() {
    SeasonEntity currentSeason = contractBean.getCurrentSeason();
    if (currentSeason == null) {
      currentSeasonID = null;
      currentSeasonName = messages.get("nothing");
    } else {
      currentSeasonID = currentSeason.getId();
      currentSeasonName = currentSeason.getName();
    }
  }

  private void initNewSectionInput() {
    newSectionName = null;

    try {
      availableSectionNames = sectionsBean.getAvailableSectionNames();
    } catch (Exception e) {
      meta.handleException(e);
      availableSectionNames = Collections.emptyList();
    }
  }

  public void setState(BasicAssociationEntity updatedAssociation,
                       Integer contractTemplateID,
                       Integer sectionID) {
    DatabaseEntity.requireID(updatedAssociation);
    if (DatabaseEntity.notEqualsID(updatedAssociation, currentAssociation))
      potentialBudgetsMap.clear();

    currentAssociation = updatedAssociation;

    refreshContracts();
    updateState(contractTemplateID, sectionID);
  }

  public void updateState(BasicAssociationEntity newAssociation,
                          Integer contractTemplateID,
                          Integer sectionID) {
    if (newAssociation != null && availableSectionNames == null)
      initNewSectionInput();

    setNewAssociation(newAssociation);
    refreshContracts();
    updateState(contractTemplateID, sectionID);
  }

  private void updateState(Integer contractTemplateID, Integer sectionID) {
    selectContract(contractTemplateID);
    refreshSections();
    selectSection(sectionID);
  }

  private void setNewAssociation(BasicAssociationEntity newAssociation) {
    if (DatabaseEntity.notEqualsID(newAssociation, currentAssociation))
      potentialBudgetsMap.clear();

    if (newAssociation != null)
      currentAssociation = principalBean.findSelectedAssociation(newAssociation.getReference());
    else
      currentAssociation = null;
  }

  private void refreshContracts() {
    assignedContracts.clear();
    availableContracts.clear();

    if (currentAssociation != null) {
      assignedContracts.addAll(currentAssociation.getActiveContractInstances(currentSeasonID));
      Collections.sort(assignedContracts);

      if (contractTemplates == null)
        contractTemplates = contractBean.getTemplatesInfo(currentSeasonID);
      for (TemplateInfo contractTemplate : contractTemplates)
        tryAddToAvailableContracts(contractTemplate);
    }
  }

  private void tryAddToAvailableContracts(TemplateInfo contractTemplate) {
    if (contractTemplate != null) {
      for (ContractInstanceEntity assignedContract : assignedContracts)
        if (DatabaseEntity.equalsID(contractTemplate.getTemplateID(), assignedContract.getContractTemplate()))
          return;

      availableContracts.add(contractTemplate);
    }
  }

  private void selectContract(Integer contractTemplateID) {
    BasicAssociationEntity association = currentAssociation;
    if (association != null) {
      if (contractTemplateID != null)
        currentContract = association.getActiveContractInstance(currentSeasonID, contractTemplateID);
      else {
        Collection<ContractInstanceEntity> activeContractInstances = association.getActiveContractInstances(currentSeasonID);
        if (!activeContractInstances.isEmpty())
          currentContract = activeContractInstances.iterator().next();
      }
    } else {
      currentContract = null;
    }

    if (currentContract == null)
      currentContractGlobalBudget = BigDecimal.ZERO;
    else
      currentContractGlobalBudget = currentContract.getGlobalMontant();
  }

  public void selectSection(Integer sectionID) {
    if (sectionID != null)
      for (SectionEntity section : getSections())
        if (sectionID.equals(section.getId())) {
          currentSection = section;
          currentSectionTotalBudget = calcTotalSectionBudget(sectionID);
          return;
        }

    currentSection = null;
    currentSectionTotalBudget = BigDecimal.ZERO;
  }

  private BigDecimal calcTotalSectionBudget(Integer sectionID) {
    BigDecimal total = BigDecimal.ZERO;

    if (currentContract != null) {
      for (BudgetEntity sectionBudget : currentContract.getSectionBudgets(sectionID)) {
        total = total.add(sectionBudget.getBudget());
      }
    }

    return total;
  }

  private void refreshSections() {
    try {
      sections.clear();
      if (currentAssociation != null)
        sections.addAll(currentAssociation.getVirtualSections());

      updatePotentialBudgets();
    } catch (Exception e) {
      meta.handleException(e);
    }
  }

  public List<BudgetEntity> getPotentialBudgetsOfCurrentSection() {
    if (currentSection != null && currentSection.getId() != null) {
      List<BudgetEntity> budgets = potentialBudgetsMap.get(currentSection.getId());
      if (budgets != null)
        return budgets;
    }
    return Collections.emptyList();
  }

  private void updatePotentialBudgets() {
    Set<ActivityEntity> activities = getCurrentActivities();
    for (SectionEntity section : getSections()) {
      List<BudgetEntity> potentialBudgets = potentialBudgetsMap.computeIfAbsent(section.getId(), key -> new ArrayList<>());
      updatePotentialBudgets(activities, section, potentialBudgets);
    }
  }

  private Set<ActivityEntity> getCurrentActivities() {
    ContractInstanceEntity contract = getCurrentContract();
    if (contract != null)
      return contract.getContractTemplate().getActivities();
    else
      return Collections.emptySet();
  }

  private void updatePotentialBudgets(Set<ActivityEntity> activities,
                                      SectionEntity section,
                                      List<BudgetEntity> potentialBudgets) {
    Objects.requireNonNull(activities);
    Objects.requireNonNull(section);
    Objects.requireNonNull(potentialBudgets);
    Collection<BudgetEntity> sectionBudgets = getSectionBudgets(section);

    removeUnrelatedBudgets(activities, potentialBudgets);
    updatePersistedBudgets(section, sectionBudgets, potentialBudgets);
    addMissingBudgets(section, activities, potentialBudgets);
  }

  private Collection<BudgetEntity> getSectionBudgets(SectionEntity section) {
    if (currentContract != null)
      return currentContract.getSectionBudgets(section.getId());
    else
      return Collections.emptyList();
  }

  public static void removeUnrelatedBudgets(Collection<ActivityEntity> activities, Iterable<BudgetEntity> potentialBudgets) {
    Iterator<BudgetEntity> iterator = potentialBudgets.iterator();
    while (iterator.hasNext())
      if (!activities.contains(iterator.next().getActivity()))
        iterator.remove();
  }

  public static void updatePersistedBudgets(SectionEntity section,
                                            Iterable<BudgetEntity> sectionBudgets,
                                            List<BudgetEntity> potentialBudgets) {
    for (BudgetEntity budget : sectionBudgets) {
      BudgetEntity potentialBudget = BudgetDatabaseEntity.extractBudget(budget, potentialBudgets);
      if (potentialBudget != null)
        potentialBudget.setBudget(budget.getBudget());
      else
        potentialBudgets.add(new BudgetEntity(section, budget.getActivity(), budget.getBudget()));
    }
  }

  public static void addMissingBudgets(SectionEntity sectionEntity,
                                       Iterable<ActivityEntity> activities,
                                       List<BudgetEntity> potentialBudgets) {
    Set<ActivityEntity> coveredActivates = new HashSet<>();
    for (BudgetEntity potentialBudget : potentialBudgets)
      coveredActivates.add(potentialBudget.getActivity());

    for (ActivityEntity activity : activities) {
      if (!coveredActivates.contains(activity))
        potentialBudgets.add(new BudgetEntity(sectionEntity, activity));
    }
  }

  public BasicAssociationEntity getCurrentAssociation() {
    return currentAssociation;
  }

  public ContractInstanceEntity getCurrentContract() {
    return currentContract;
  }

  public BigDecimal getCurrentContractGlobalBudget() {
    return currentContractGlobalBudget;
  }

  public Integer getCurrentContractTemplateID() {
    return currentContract == null ? null : currentContract.getContractTemplate().getId();
  }

  public SectionEntity getCurrentSection() {
    return currentSection;
  }

  public Integer getCurrentSectionID() {
    if (currentSection == null)
      return null;
    else
      return currentSection.getId();
  }

  public BigDecimal getCurrentSectionTotalBudget() {
    return currentSectionTotalBudget;
  }

  public BigDecimal calcSectionTotalBudget(Integer sectionID) {
    BigDecimal result = BigDecimal.ZERO;

    ContractInstanceEntity currentContract = getCurrentContract();
    if (currentContract != null && sectionID != null)
      for (BudgetEntity sectionBudget : currentContract.getSectionBudgets(sectionID))
        result = result.add(sectionBudget.getBudget());

    return result;
  }

  public List<SectionEntity> getSections() {
    return sections;
  }

  public List<ContractInstanceEntity> getAssignedContracts() {
    return assignedContracts;
  }

  public List<TemplateInfo> getAvailableContracts() {
    return availableContracts;
  }

  public void addContractInstance() {
    BasicAssociationEntity currentAccount = getCurrentAssociation();
    if (currentAccount == null)
      meta.noSelectionError();
    else
      try {
        principalBean.addContractInstance(newContractTemplateID, currentAccount.getReference());
        meta.dataUpdatedSuccessfully();
        PrimeFaces.current().executeScript("PF('new_contract_dialog').hide()");
        refreshEvent.fire(new RefreshAccountEvent(currentAccount.getReference(), newContractTemplateID, getCurrentSectionID()));
      } catch (Exception e) {
        meta.handleException(e);
      }
  }

  public void deleteContractInstance(Integer id) {
    if (currentAssociation != null && id != null)
      try {
        principalBean.deleteContractInstance(id);
        meta.dataUpdatedSuccessfully();
        refreshEvent.fire(RefreshAccountEvent.refreshAll());
      } catch (Exception e) {
        meta.handleException(e);
      }
  }

  public List<EntityReference> getAvailableSectionNames() {
    return availableSectionNames;
  }

  public Integer getNewContractTemplateID() {
    return newContractTemplateID;
  }

  public void setNewContractTemplateID(Integer newContractTemplateID) {
    this.newContractTemplateID = newContractTemplateID;
  }

  public String getNewSectionName() {
    return newSectionName;
  }

  public void setNewSectionName(String newSectionName) {
    this.newSectionName = newSectionName;
  }

  public void addNewSection() {
    BasicAssociationEntity currentAssociation = getCurrentAssociation();
    if (currentAssociation == null) {
      meta.noSelectionError();
    } else
      try {
        SectionEntity newSection = sectionsBean.add(currentAssociation.getId(), newSectionName);
        initNewSectionInput();
        meta.dataUpdatedSuccessfully();
        PrimeFaces.current().executeScript("PF('new_section_dialog').hide()");
        refreshEvent.fire(new RefreshAccountEvent(currentAssociation.getReference(), getCurrentContractTemplateID(), newSection.getId()));
      } catch (Exception e) {
        meta.handleException(e);
      }
  }

  public void moveCurrentSectionUp() {
    if (currentSection != null && currentAssociation != null && currentAssociation.getId() != null) {
      try {
        sectionsBean.moveSectionUp(currentAssociation.getId(), getCurrentSectionID());
        refreshEvent.fire(new RefreshAccountEvent(currentAssociation.getReference(), getCurrentContractTemplateID(), getCurrentSectionID()));
      } catch (Exception e) {
        meta.handleException(e);
      }
    } else
      meta.noSelectionError();
  }

  public void moveCurrentSectionDown() {
    if (currentSection != null && currentAssociation != null && currentAssociation.getId() != null) {
      try {
        sectionsBean.moveSectionDown(currentAssociation.getId(), getCurrentSectionID());
        refreshEvent.fire(new RefreshAccountEvent(currentAssociation.getReference(), getCurrentContractTemplateID(), getCurrentSectionID()));
      } catch (Exception e) {
        meta.handleException(e);
      }
    } else
      meta.noSelectionError();
  }

  public boolean isCurrentSectionIsFirst() {
    if (currentSection == null || currentSection.getIndex() == null)
      return true;
    else {
      Integer firstIndex = currentSection.getIndex();
      for (SectionEntity section : getSections())
        if (section.getIndex() < firstIndex)
          firstIndex = section.getIndex();
      return firstIndex.equals(currentSection.getIndex());
    }
  }

  public boolean isCurrentSectionIsLast() {
    if (currentSection == null || currentSection.getIndex() == null)
      return true;
    else {
      Integer lastIndex = currentSection.getIndex();
      for (SectionEntity section : getSections())
        if (section.getIndex() > lastIndex)
          lastIndex = section.getIndex();
      return lastIndex.equals(currentSection.getIndex());
    }
  }

  public ProcessingState[] getSelectableProcessingStates() {
    return ProcessingState.values();
  }

  public void deleteSection(Integer id) {
    if (id != null) {
      try {
        sectionsBean.delete(id);
        meta.dataUpdatedSuccessfully();
        refreshEvent.fire(RefreshAccountEvent.refreshAll());
      } catch (Exception e) {
        meta.handleException(e);
      }
    }
  }

  void save() throws IntegrityException {
    if (currentSection != null) {
      if (currentSection.getProcessingState() != ProcessingState.DONE_PROCESSING)
        currentSection.setProcessingState(ProcessingState.ON_PROGRESS);
      sectionsBean.updateSection(currentSection);
    }

    if (currentAssociation != null && currentContract != null)
      sectionsBean.updateBudgets(currentAssociation.getId(), currentContract.getId(), potentialBudgetsMap);
  }

  public boolean getCanDownloadContract() {
    BasicAssociationEntity currentAssociation = getCurrentAssociation();
    ContractInstanceEntity currentContract = getCurrentContract();
    if (currentAssociation != null && currentContract != null)
      return currentAssociation.getValid() || currentContract.getValid();
    else
      return false;
  }

  public Boolean getCurrentAssociationIsSport() {
    return currentAssociation instanceof SportAssociationEntity;
  }

  public String getCurrentSeasonName() {
    return currentSeasonName;
  }
}
