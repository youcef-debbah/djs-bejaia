package dz.ngnex.view;

import dz.ngnex.bean.ContractBean;
import dz.ngnex.bean.PrincipalBean;
import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.*;
import dz.ngnex.util.ViewModel;
import dz.ngnex.util.WebKit;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Inject;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ViewModel
public class ContractsView implements Serializable {
  private static final long serialVersionUID = 1742033278376300147L;
  public static final String ACCOUNT_PARAM = "account";

  private final List<ContractInstanceEntity> contracts = new ArrayList<>();

  @EJB
  private ContractBean contractBean;

  @Inject
  private CurrentPrincipal currentPrincipal;

  @Inject
  private Meta meta;

  private BasicAssociationEntity association;

  @EJB
  private PrincipalBean principalBean;

  private BigDecimal totalBudgetOfActiveContracts = BigDecimal.ZERO;

  private BigDecimal totalBudgetOfArchivedContracts = BigDecimal.ZERO;

  @PostConstruct
  private void init() {
    Integer accountID = WebKit.getRequestParamAsInt(ACCOUNT_PARAM);
    if (accountID != null)
      association = principalBean.findAssociationForContractView(accountID);
    else if (currentPrincipal.isAssociation())
      association = principalBean.findAssociationForContractView(currentPrincipal.getId());

    refresh();

    if (association == null)
      WebKit.redirectToHome();
  }

  public void refresh() {
    try {
      contracts.clear();
      if (association != null) {
        contracts.addAll(principalBean.getAllContracts(association.getReference()));
        Collections.sort(contracts);
      }
      calcBudgets();
    } catch (Exception e) {
      meta.handleException(e);
    }
  }

  private void calcBudgets() {
    BigDecimal active = BigDecimal.ZERO;
    BigDecimal archived = BigDecimal.ZERO;

    for (ContractInstanceEntity contract : contracts)
      if (contract.getState() == ContractInstanceState.ACTIVE)
        active = active.add(contract.getGlobalMontant());
      else if (contract.getState() == ContractInstanceState.ARCHIVED)
        archived = archived.add(contract.getGlobalMontant());

    this.totalBudgetOfActiveContracts = active;
    this.totalBudgetOfArchivedContracts = archived;
  }

  public List<ContractInstanceEntity> getContracts() {
    return contracts;
  }

  public BigDecimal getTotalBudgetOfActiveContracts() {
    return totalBudgetOfActiveContracts;
  }

  public BigDecimal getTotalBudgetOfArchivedContracts() {
    return totalBudgetOfArchivedContracts;
  }

  public AchievementLevel[] getAllAchievementLevels() {
    return AchievementLevel.values();
  }

  public void saveAchievements(Integer id) {
    ContractInstanceEntity contract = DatabaseEntity.get(id, contracts);
    if (contract != null) {
      try {
        contractBean.updateAchievements(contract);
        meta.dataUpdatedSuccessfully();
      } catch (Exception e) {
        meta.handleException(e);
      }
    }
  }

  public void saveAchievementLevel(Integer id) {
    ContractInstanceEntity contract = DatabaseEntity.get(id, contracts);
    if (contract != null) {
      try {
        contractBean.updateAchievementLevel(contract);
        meta.dataUpdatedSuccessfully();
      } catch (Exception e) {
        meta.handleException(e);
      }
    }
  }

  public void delete(Integer id) {
    if (id != null && association != null)
      try {
        principalBean.deleteContractInstance(id);
        meta.dataUpdatedSuccessfully();
        refresh();
      } catch (Exception e) {
        meta.handleException(e);
      }

    refresh();
  }

  public BasicAssociationEntity getAssociation() {
    return association;
  }

  public void archiveContractInstance(Integer contractID) {
    if (association != null)
      try {
        principalBean.archiveContractInstance(contractID, association.getReference());
        meta.workDoneSuccessfully("archivingDone");
        refresh();
      } catch (Exception e) {
        meta.handleException(e);
      }
  }

  public void unarchiveContractInstance(Integer contractID) {
    if (association != null)
      try {
        principalBean.unarchiveContractInstance(contractID, association.getReference());
        meta.dataUpdatedSuccessfully();
        refresh();
      } catch (Exception e) {
        meta.handleException(e);
      }
  }
}
