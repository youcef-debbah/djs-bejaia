package dz.ngnex.view;

import dz.ngnex.bean.AccountDemandBean;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.AccountDemandEntity;
import dz.ngnex.util.ViewModel;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@ViewModel
public class AccountDemandsList implements Serializable {
  private static final long serialVersionUID = -3198221478539489533L;

  @EJB
  private AccountDemandBean accountDemandBean;

  @Inject
  private Meta meta;

  private List<AccountDemandEntity> demands;

  private AccountDemandEntity currentDemand;

  @PostConstruct
  private void init() {
    handleRefresh(null);
  }

  public void handleRefresh(@Observes(notifyObserver = Reception.IF_EXISTS) RefreshAccountEvent event) {
    try {
      demands = accountDemandBean.getDemands();
    } catch (Exception e) {
      demands = Collections.emptyList();
      meta.handleException(e);
    }
  }

  public List<AccountDemandEntity> getDemands() {
    return demands;
  }

  public AccountDemandEntity getCurrentDemand() {
    return currentDemand;
  }

  public void setCurrentDemand(AccountDemandEntity currentDemand) {
    this.currentDemand = currentDemand;
  }

  public void deleteCurrentDemand() {
    if (currentDemand == null)
      meta.noSelectionError();
    else
      try {
        accountDemandBean.delete(currentDemand);
        meta.dataUpdatedSuccessfully();
        handleRefresh(null);
      } catch (Exception e) {
        meta.handleException(e);
      }
  }
}
