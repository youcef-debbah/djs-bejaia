package dz.ngnex.view;

import dz.ngnex.bean.MessagesBean;
import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.MessageEntity;
import dz.ngnex.entity.MessageState;
import dz.ngnex.util.ViewModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@ViewModel
public class InboxView implements Serializable {

  private static final long serialVersionUID = 7390757397211957055L;

  private static final Logger log = LogManager.getLogger(UsersView.class);

  @EJB
  private MessagesBean messagesBean;

  private List<MessageEntity> messages;

  private MessageEntity currentMessage;

  @Inject
  Meta meta;

  @Inject
  CurrentPrincipal currentPrincipal;

  @PostConstruct
  public void init() {
    refreshMessages();
  }

  public void refreshMessages() {
    try {
      messages = messagesBean.getAllUnReadMessagesReceivedBy(currentPrincipal.getService());
    } catch (Exception e) {
      meta.handleException(e);
    }
  }

  public List<MessageEntity> getMessages() {
    return messages;
  }

  public MessageEntity getCurrentMessage() {
    return currentMessage;
  }

  public void setCurrentMessage(MessageEntity currentMessage) {
    this.currentMessage = currentMessage;
  }

  public void markAsSeen() {
    if (currentMessage != null && currentMessage.getState() != MessageState.READ)
      try {
        messagesBean.markMessageAsRead(currentMessage.getId());
      } catch (Exception e) {
        meta.handleException(e);
      }
  }

  public void deleteCurrentMessage() {
    if (currentMessage != null) {
      try {
        messagesBean.deleteMessage(currentMessage.getId());
        currentMessage = null;
        refreshMessages();
      } catch (Exception e) {
        meta.handleException(e);
      }
    }
  }
}
