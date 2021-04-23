package dz.ngnex.view;

import dz.ngnex.bean.MessagesBean;
import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.control.Meta;
import dz.ngnex.util.ViewModel;

import javax.ejb.EJB;
import javax.inject.Inject;

@ViewModel
public class MessageCounter extends LazyEnabledModel {
  private static final long serialVersionUID = 7361602425931983906L;

  @EJB
  private MessagesBean messagesBean;

  @Inject
  Meta meta;

  private Long totalUnreadCount;

  @Inject
  CurrentPrincipal currentPrincipal;

  public void refresh() {
    try {
      if (currentPrincipal.isAdmin())
        totalUnreadCount = messagesBean.countUnreadMessagesReceivedBy(currentPrincipal.getService());
      else if (currentPrincipal.isAssociation())
        totalUnreadCount = messagesBean.countUnreadAdminMessagesReceivedBy(currentPrincipal.getName());
      else
        totalUnreadCount = 0L;
    } catch (Exception e) {
      meta.handleException(e);
      totalUnreadCount = 0L;
    }
  }

  public long getTotalUnreadCount() {
    if (totalUnreadCount == null)
      refresh();
    return totalUnreadCount;
  }
}
