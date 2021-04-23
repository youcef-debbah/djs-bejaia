package dz.ngnex.view;

import dz.ngnex.bean.ContractBean;
import dz.ngnex.bean.PrincipalBean;
import dz.ngnex.bean.SeasonBean;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.Constrains;
import dz.ngnex.entity.SeasonEntity;
import dz.ngnex.entity.SeasonStats;
import dz.ngnex.entity.TemplateInfo;
import dz.ngnex.util.ViewModel;
import org.jetbrains.annotations.Nullable;
import org.primefaces.PrimeFaces;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@ViewModel
public class SeasonView implements Serializable {
  private static final long serialVersionUID = -3882337951316890529L;

  @EJB
  private SeasonBean seasonBean;

  @EJB
  private ContractBean contractBean;

  @EJB
  private PrincipalBean principalBean;

  private List<SeasonEntity> seasons;

  @Inject
  private Meta meta;

  @Nullable
  private Integer currentSeasonID;

  @Nullable
  private SeasonStats currentSeasonStats;

  @Size(min = Constrains.Min_IDENTIFIER_SIZE, max = Constrains.MAX_IDENTIFIER_SIZE)
  private String newSeasonName;

  private List<TemplateInfo> contracts;

  @PostConstruct
  private void init() {
    refreshCurrentSeason();
    refreshSeasons();
    refreshContracts();
  }

  private void refreshAfterContractUpdate() {
    refreshSeasonStats();
    refreshContracts();
  }

  private void refreshContracts() {
    try {
      contracts = contractBean.getTemplatesInfo();
    } catch (Exception e) {
      contracts = Collections.emptyList();
      meta.handleException(e);
    }
  }

  private void refreshSeasons() {
    try {
      seasons = seasonBean.getAllSeasons();
    } catch (Exception e) {
      seasons = Collections.emptyList();
      meta.handleException(e);
    }
  }

  private void refreshCurrentSeason() {
    try {
      currentSeasonID = contractBean.findCurrentSeasonID();
    } catch (Exception e) {
      currentSeasonID = null;
      meta.handleException(e);
    }
    refreshSeasonStats();
  }

  private void refreshSeasonStats() {
    try {
      currentSeasonStats = seasonBean.getSeasonStats(currentSeasonID);
    } catch (Exception e) {
      currentSeasonStats = null;
      meta.handleException(e);
    }
  }

  public List<SeasonEntity> getSeasons() {
    return seasons;
  }

  @Nullable
  public Integer getCurrentSeasonID() {
    return currentSeasonID;
  }

  public void setCurrentSeasonID(Integer currentSeasonID) {
    try {
      seasonBean.updateCurrentSeason(currentSeasonID);
      refreshCurrentSeason();
      meta.dataUpdatedSuccessfully();
    } catch (Exception e) {
      meta.handleException(e);
    }
  }

  @Nullable
  public SeasonStats getCurrentSeasonStats() {
    return currentSeasonStats;
  }

  public String getNewSeasonName() {
    return newSeasonName;
  }

  public void setNewSeasonName(String newSeasonName) {
    this.newSeasonName = newSeasonName;
  }

  public void addNewSeason() {
    if (newSeasonName == null)
      meta.inputMissing();
    else
      try {
        seasonBean.addSeason(newSeasonName);
        newSeasonName = null;

        refreshCurrentSeason();
        refreshSeasons();

        meta.dataUpdatedSuccessfully();
        PrimeFaces.current().executeScript("PF('new-season-dialog').hide()");
      } catch (Exception e) {
        meta.handleException(e);
      }
  }

  public void deleteSeason() {
    Integer currentSeasonID = getCurrentSeasonID();
    if (currentSeasonID == null)
      meta.noSelectionError();
    else
      try {
        seasonBean.deleteSeason(currentSeasonID);
        refreshCurrentSeason();
        refreshSeasons();
        meta.dataUpdatedSuccessfully();
      } catch (Exception e) {
        meta.handleException(e);
      }
  }

  public List<TemplateInfo> getContracts() {
    return contracts;
  }

  public void updateContractsSeason() {
    try {
      contractBean.updateContractsSeason(contracts, currentSeasonID);
      meta.dataUpdatedSuccessfully();
      refreshAfterContractUpdate();
    } catch (Exception e) {
      meta.handleException(e);
    }
  }
}
