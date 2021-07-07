package dz.ngnex.view;

import dz.ngnex.bean.AttachmentsBean;
import dz.ngnex.bean.MessagesBean;
import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.control.LocaleManager;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.*;
import dz.ngnex.util.ViewModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    private List<ClientMessageEntity> allMessages;
    private List<ClientMessageEntity> importantMessages;

    private ClientMessageEntity selectedMessage;

    @Inject
    private LocaleManager localeManager;

    @Inject
    Meta meta;

    @Inject
    CurrentPrincipal currentPrincipal;

    private String groupMessageContent;

    private AttachmentContentEntity groupAttachment;

    private List<String> selectedReceivers;

    private List<MenuItem> allReceivers;

    private boolean onlyImportantMessages;

    @PostConstruct
    public void init() {
        refreshMessages();
        refreshReceivers();
    }

    public void refreshMessages() {
        try {
            List<ClientMessageEntity> messages = messagesBean.getInboxMessagesReceivedBy(currentPrincipal.getService());
            this.allMessages = messages;
            importantMessages = messages.stream()
                    .filter(ClientMessageEntity::notFromGuest)
                    .collect(Collectors.toList());
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

    public List<ClientMessageEntity> getMessages() {
        return onlyImportantMessages ? importantMessages : allMessages;
    }

  public boolean isOnlyImportantMessages() {
    return onlyImportantMessages;
  }

  public void setOnlyImportantMessages(boolean onlyImportantMessages) {
    this.onlyImportantMessages = onlyImportantMessages;
  }

  public ClientMessageEntity getSelectedMessage() {
        return selectedMessage;
    }

    public String getGuestEmail() {
        if (selectedMessage instanceof GuestMessageEntity)
            return ((GuestMessageEntity) selectedMessage).getEmail();
        else
            return null;
    }

    public String getGuestPhone() {
        if (selectedMessage instanceof GuestMessageEntity)
            return ((GuestMessageEntity) selectedMessage).getPhone();
        else
            return null;
    }

    public String getGuestName() {
        if (selectedMessage instanceof GuestMessageEntity)
            return ((GuestMessageEntity) selectedMessage).getGuestName();
        else
            return null;
    }

    public void setSelectedMessage(ClientMessageEntity selectedMessage) {
        this.selectedMessage = selectedMessage;
        if (DatabaseEntity.getID(selectedMessage) != null) {
            markAsRead(selectedMessage);
            AttachmentInfoEntity attachment = selectedMessage.getAttachment();
            if (DatabaseEntity.getID(attachment) != null && !Hibernate.isInitialized(attachment))
                selectedMessage.setAttachment(messagesBean.getAttachment(attachment.getId()));
        }
    }

    public void markAsRead(ClientMessageEntity selectedMessage) {
        if (selectedMessage.getState() != MessageState.READ)
            try {
                if (this.selectedMessage.isFromGuest())
                    messagesBean.markGuestMessageAsRead(this.selectedMessage.getId());
                else
                    messagesBean.markMessageAsRead(this.selectedMessage.getId());
            } catch (Exception e) {
                meta.handleException(e);
            }
    }

    public void deleteCurrentMessage() {
        if (DatabaseEntity.getID(selectedMessage) != null) {
            try {
                if (selectedMessage.isFromGuest())
                    messagesBean.deleteGuestMessage(selectedMessage.getId());
                else
                    messagesBean.deleteMessage(selectedMessage.getId());

                selectedMessage = null;
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
                messagesBean.sendAdminMessages(currentPrincipal.getName(), localeManager.formatTextAsHtml(groupMessageContent), selectedReceivers, groupAttachment);
                resetGroupMessageInput();
                meta.workDoneSuccessfully("messageSent");
                PrimeFaces.current().executeScript("PF('group_msg_dialog').hide()");
                adminMessageNotifications.send("refresh", selectedReceivers);
            } catch (RuntimeException e) {
                meta.handleException(e);
            }
    }

    public List<MenuItem> getAllReceivers() {
        return allReceivers;
    }

    public List<String> getSelectedReceivers() {
        return selectedReceivers;
    }

    public void setSelectedReceivers(List<String> selectedReceivers) {
        this.selectedReceivers = selectedReceivers;
    }

    private void resetGroupMessageInput() {
        groupMessageContent = null;
        groupAttachment = null;
    }

    public void handleGroupAttachment(FileUploadEvent event) {
        try {
            AttachmentContentEntity uploadedFile = attachmentsBean.add(event.getFile(), currentPrincipal.getName(), System.currentTimeMillis());
            if (groupAttachment != null)
                meta.workDoneSuccessfully("fileReplaced");
            else
                meta.workDoneSuccessfully("fileUploaded");

            groupAttachment = uploadedFile;
        } catch (Exception e) {
            meta.handleException(e);
        }
    }

    public AttachmentContentEntity getGroupAttachment() {
        return groupAttachment;
    }

    public void deleteGroupAttachment() {
        groupAttachment = null;
        meta.workDoneSuccessfully("fileDeleted");
    }

    public int getMaxFileSize() {
        return AttachmentsBean.MAX_FILE_SIZE;
    }
}
