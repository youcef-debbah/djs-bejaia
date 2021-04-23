package dz.ngnex.view;

import dz.ngnex.bean.AccountDemandBean;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.AccountDemandEntity;
import dz.ngnex.util.ViewModel;
import dz.ngnex.util.WebKit;

import javax.ejb.EJB;
import javax.inject.Inject;
import java.io.Serializable;

@ViewModel
public class AccountDemandView implements Serializable {
  private static final long serialVersionUID = -1049718007832314644L;

  @EJB
  private AccountDemandBean accountDemandBean;

  @Inject
  private Meta meta;

  private AccountDemandEntity demand = new AccountDemandEntity();

  public void sendDemand() {
    try {
      accountDemandBean.send(demand);
      meta.addGlobalMessage("demandSent", "weWillCallYouSoon");
      meta.keepMessages();
      WebKit.redirectToHome();
    } catch (Exception e) {
      meta.handleException(e);
    }
  }

  public AccountDemandEntity getDemand() {
    return demand;
  }
}
