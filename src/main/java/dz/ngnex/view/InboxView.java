package dz.ngnex.view;

import dz.ngnex.bean.AttachmentsBean;
import dz.ngnex.bean.MessagesBean;
import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.control.LocaleManager;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.AttachmentContentEntity;
import dz.ngnex.entity.MessageEntity;
import dz.ngnex.entity.MessageState;
import dz.ngnex.util.ViewModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;

@ViewModel
public class InboxView implements Serializable {

  private static final long serialVersionUID = 7390757397211957055L;

  private static final Logger log = LogManager.getLogger(UsersView.class);

  @EJB
  private MessagesBean messagesBean;

  @EJB
  private AttachmentsBean attachmentsBean;

  @Inject
  @Push
  private PushContext adminMessageNotifications;

  private List<MessageEntity> messages;

  private MessageEntity currentMessage;

  @Inject
  private LocaleManager localeManager;

  @Inject
  Meta meta;

  @Inject
  CurrentPrincipal currentPrincipal;

  private String groupMessageContent;

  private AttachmentContentEntity attachment;

  private List<ReceiverItem> selectedReceivers;

  private List<ReceiverItem> allReceivers;

  @PostConstruct
  public void init() {
    refreshMessages();
    refreshReceivers();
  }

  public void refreshMessages() {
    try {
      messages = messagesBean.getAllUnReadMessagesReceivedBy(currentPrincipal.getService());
    } catch (Exception e) {
      meta.handleException(e);
    }
  }

  private void refreshReceivers() {
    try {
      allReceivers = messagesBean.getAllReceivers(currentPrincipal.getService());
    } catch (Exception e) {
      allReceivers = Collections.emptyList();
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

  public String getGroupMessageContent() {
    return groupMessageContent;
  }

  public void setGroupMessageContent(String groupMessageContent) {
    this.groupMessageContent = groupMessageContent;
  }

  public void sendGroupMessage() {
    if (groupMessageContent != null && selectedReceivers != null && !selectedReceivers.isEmpty())
      try {
        Collection<String> receiversNames = ReceiverItem.getNames(selectedReceivers);
        messagesBean.sendAdminMessages(currentPrincipal.getName(), localeManager.formatTextAsHtml(groupMessageContent), receiversNames, attachment);
        resetGroupMessageInput();
        meta.workDoneSuccessfully("messageSent");
        PrimeFaces.current().executeScript("PF('group_msg_dialog').hide()");
        adminMessageNotifications.send("refresh", receiversNames);
      } catch (RuntimeException e) {
        meta.handleException(e);
      }
  }

  public List<ReceiverItem> getSelectedReceivers() {
    return selectedReceivers;
  }

  public void setSelectedReceivers(List<ReceiverItem> selectedReceivers) {
    this.selectedReceivers = selectedReceivers;
  }

  private void resetGroupMessageInput() {
    groupMessageContent = null;
    attachment = null;
  }

  public void handleAttachment(FileUploadEvent event) {
    try {
      AttachmentContentEntity uploadedFile = attachmentsBean.add(event.getFile(), currentPrincipal.getName(), System.currentTimeMillis());
      if (attachment != null)
        meta.workDoneSuccessfully("fileReplaced");
      else
        meta.workDoneSuccessfully("fileUploaded");

      attachment = uploadedFile;
    } catch (Exception e) {
      meta.handleException(e);
    }
  }

  public AttachmentContentEntity getAttachment() {
    return attachment;
  }

  public void deleteAttachment() {
    attachment = null;
    meta.workDoneSuccessfully("fileDeleted");
  }

  public List<ReceiverItem> completeReceiver(String input) {
    return ReceiverItem.filter(allReceivers, input);
  }
}
