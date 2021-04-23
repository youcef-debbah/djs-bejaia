package dz.ngnex.view;

import dz.ngnex.bean.MessagesBean;
import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.control.LocaleManager;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.BasicMessageEntity;
import dz.ngnex.util.ViewModel;
import org.primefaces.PrimeFaces;

import javax.ejb.EJB;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@ViewModel
public class GlobalMessagingView implements Serializable {
  private static final long serialVersionUID = -499103431481462190L;

  @EJB
  private MessagesBean messagesBean;

  @NotNull
  @Size(max = BasicMessageEntity.MAX_CONTENT_LENGTH)
  private String content;

  @Inject
  Meta meta;

  @Inject
  CurrentPrincipal currentPrincipal;

  @Inject
  private LocaleManager localeManager;

  @Inject
  @Push
  private PushContext globalMessageNotifications;

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public void sendGlobalMessage() {
    try {
      if (currentPrincipal.isAdmin()) {
        messagesBean.sendGlobalAdminMessage(currentPrincipal.getName(), localeManager.formatTextAsHtml(content));
        content = null;
        meta.workDoneSuccessfully("messageSent");
        PrimeFaces.current().executeScript("PF('global_msg_dialog').hide()");
        globalMessageNotifications.send("refresh");
      }
    } catch (Exception e) {
      meta.handleException(e);
    }
  }
}
