package dz.ngnex.view;

import dz.ngnex.bean.ContractBean;
import dz.ngnex.bean.PrincipalBean;
import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.entity.*;
import dz.ngnex.util.ViewModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@ViewModel
public class PropertiesView implements Serializable {
    private static final long serialVersionUID = 7543091292366645879L;

    @EJB
    private ContractBean contractBean;

    @EJB
    private PrincipalBean principalBean;

    @Inject
    private CurrentPrincipal currentPrincipal;

    @Nullable
    private ContractInstanceEntity currentContract;

    private BasicAssociationEntity currentAssociation;

    @NotNull
    private final List<PropertyValueEntity> potentialValues = new ArrayList<>();

    @NotNull
    private final List<GlobalBudgetEntity> potentialBudgets = new ArrayList<>();

    @PostConstruct
    private void init() {
        updateState(null, null);
    }

    void updateState(@Nullable BasicAssociationEntity associationEntity, @Nullable ContractInstanceEntity currentContract) {
        currentAssociation = associationEntity;
        setNewContract(currentContract);
        refreshPotentialValues();
    }

    private void setNewContract(@Nullable ContractInstanceEntity newcontract) {
        if (newcontract == null || currentContract == null ||
                !Objects.equals(newcontract.getId(), currentContract.getId()))
            clearAll();
        currentContract = newcontract;
    }

    private void clearAll() {
        potentialValues.clear();
        potentialBudgets.clear();
    }

    @Nullable
    public ContractInstanceEntity getCurrentContract() {
        return currentContract;
    }

    public BasicAssociationEntity getCurrentAssociation() {
        return currentAssociation;
    }

    private void refreshPotentialValues() {
        ContractInstanceEntity currentContract = getCurrentContract();
        BasicAssociationEntity currentAssociation = getCurrentAssociation();
        if (currentContract != null && currentAssociation != null) {
            Service currentAssociationService = currentAssociation.getAccessType().getService();
            updateProperties(currentContract, currentAssociationService);
            updateBudgets(currentContract, currentAssociationService);
        } else
            clearAll();
    }

    private void updateProperties(ContractInstanceEntity currentContract, Service currentAssociationService) {
        Set<PropertyEntity> properties = currentContract.getContractTemplate().getProperties()
                .stream()
                .filter(property -> currentPrincipal.hasAccess(property, currentAssociationService))
                .collect(Collectors.toSet());

        removeUnrelatedValues(properties, potentialValues);
        updatePersistedValues(properties, currentContract.getPropertyValues(), potentialValues);
        addMissingValues(properties, potentialValues);
        Collections.sort(potentialValues);
    }

    private void updateBudgets(ContractInstanceEntity currentContract, Service currentAssociationService) {
        if (currentAssociationService == Service.SPORT_SERVICE && currentPrincipal.isAdmin()) {
            Set<ActivityEntity> activities = currentContract.getContractTemplate().getActivities();

            removeUnrelatedBudgets(activities, potentialBudgets);
            updatePersistedBudgets(activities, currentContract.getGlobalBudgets(), potentialBudgets);
            addMissingBudgets(activities, potentialBudgets);
            Collections.sort(potentialBudgets);
        } else
            potentialBudgets.clear();
    }

    public static void removeUnrelatedValues(Set<PropertyEntity> properties,
                                             Iterable<PropertyValueEntity> potentialValues) {
        Iterator<PropertyValueEntity> iterator = potentialValues.iterator();
        while (iterator.hasNext())
            if (!properties.contains(iterator.next().getProperty()))
                iterator.remove();
    }

    public static void removeUnrelatedBudgets(Set<ActivityEntity> activities,
                                              Iterable<GlobalBudgetEntity> potentialBudgets) {
        Iterator<GlobalBudgetEntity> iterator = potentialBudgets.iterator();
        while (iterator.hasNext())
            if (!activities.contains(iterator.next().getActivity()))
                iterator.remove();
    }

    public static void updatePersistedValues(Set<PropertyEntity> properties,
                                             Iterable<PropertyValueEntity> persistedValues,
                                             List<PropertyValueEntity> potentialValues) {
        for (PropertyValueEntity entity : persistedValues)
            if (DatabaseEntity.contains(entity.getProperty().getId(), properties)) {
                PropertyValueEntity potentialValue = PropertyValueEntity.extractValue(entity.getProperty(), potentialValues);
                if (potentialValue != null)
                    potentialValue.copyValue(entity);
                else
                    potentialValues.add(new PropertyValueEntity(entity.getProperty(), entity.getHeader(), entity.getValue()));
            }
    }

    public static void updatePersistedBudgets(Set<ActivityEntity> activities,
                                              Iterable<GlobalBudgetEntity> persistedValues,
                                              List<GlobalBudgetEntity> potentialValues) {
        for (GlobalBudgetEntity entity : persistedValues)
            if (DatabaseEntity.contains(entity.getActivity().getId(), activities)) {
                GlobalBudgetEntity budget = BudgetDatabaseEntity.extractBudget(entity, potentialValues);
                if (budget != null)
                    budget.copyBudget(entity);
                else
                    potentialValues.add(new GlobalBudgetEntity(entity.getActivity(), entity.getHeader(), entity.getBudget()));
            }
    }

    public static void addMissingValues(Iterable<PropertyEntity> properties, List<PropertyValueEntity> potentialValues) {
        Set<PropertyEntity> coveredProperties = new HashSet<>();
        for (PropertyValueEntity potentialValue : potentialValues)
            coveredProperties.add(potentialValue.getProperty());

        for (PropertyEntity property : properties) {
            if (!coveredProperties.contains(property))
                potentialValues.add(new PropertyValueEntity(property));
        }
    }

    public static void addMissingBudgets(Iterable<ActivityEntity> activities, List<GlobalBudgetEntity> potentialBudgets) {
        Set<ActivityEntity> coveredBudgets = new HashSet<>();
        for (GlobalBudgetEntity potentialBudget : potentialBudgets)
            coveredBudgets.add(potentialBudget.getActivity());

        for (ActivityEntity activity : activities) {
            if (!coveredBudgets.contains(activity))
                potentialBudgets.add(new GlobalBudgetEntity(activity));
        }
    }

    void save(BasicAssociationEntity currentAssociation) {
      this.currentAssociation = currentAssociation;
        if (getCurrentContract() != null)
            contractBean.updateValues(getCurrentContract().getId(), potentialValues, potentialBudgets);
    }

    @NotNull
    public List<PropertyValueEntity> getPotentialValues() {
        return potentialValues;
    }

    @NotNull
    public List<GlobalBudgetEntity> getPotentialBudgets() {
        return potentialBudgets;
    }

    @Override
    public String toString() {
        return "PropertiesView{" +
                "currentContract=" + currentContract +
                ", potentialValues=" + potentialValues +
                ", potentialBudgets=" + potentialBudgets +
                '}';
    }
}
