package dz.ngnex.view;

import dz.ngnex.bean.AttachmentsBean;
import dz.ngnex.bean.IntegrityException;
import dz.ngnex.bean.MessagesBean;
import dz.ngnex.bean.PrincipalBean;
import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.control.LocaleManager;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.*;
import dz.ngnex.util.ViewModel;
import dz.ngnex.util.WebKit;
import org.primefaces.event.FileUploadEvent;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ViewModel
public class MessagingView implements Serializable {

  private static final long serialVersionUID = 2821593938969430957L;
  public static final String PARAM_SENDER = "sender";

  @EJB
  private MessagesBean messagesBean;

  @EJB
  private PrincipalBean principalBean;

  @Size(max = MessageEntity.MAX_TITLE_LENGTH)
  private String title;

  @NotNull
  @Size(max = BasicMessageEntity.MAX_CONTENT_LENGTH)
  private String content;

  private AttachmentContentEntity attachment;

  private List<Snippet> conversation;

  private Service service;

  private BasicPrincipalEntity entity;

  @EJB
  private AttachmentsBean attachmentsBean;

  @Inject
  CurrentPrincipal currentPrincipal;

  @Inject
  Meta meta;

  @Inject
  private LocaleManager localeManager;

  @Push
  @Inject
  private PushContext messageNotifications;

  @Inject
  @Push
  private PushContext adminMessageNotifications;

  @PostConstruct
  private void init() {
    initEntityAndAdministration();
    if (entity == null)
      meta.pageNotFound();
    else
      fetchMessages();
  }

  private void initEntityAndAdministration() {
    if (currentPrincipal.getAccessType().isAssociation())
      entity = principalBean.findPrincipal(currentPrincipal.getId());
    else {
      String senderName = WebKit.getRequestParam(PARAM_SENDER);
      if (senderName != null)
        entity = principalBean.findPrincipalByName(senderName);
    }

    if (entity != null)
      service = entity.getAccessType().getService();
    else
      service = null;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Service getService() {
    return service;
  }

  public void setService(Service service) {
    this.service = service;
  }

  public void sendMessage() {
    try {
      messagesBean.sendMessage(service, title, localeManager.formatTextAsHtml(content), entity.getName(), attachment);
      resetInput();
      fetchMessages();
      messageNotifications.send("refresh", Arrays.asList(service.name(), "global"));
    } catch (Exception e) {
      meta.handleException(e);
    }
  }

  public void sendAdminMessage() {
    if (entity != null)
      try {
        String receiver = entity.getName();
        messagesBean.sendAdminMessage(currentPrincipal.getName(), localeManager.formatTextAsHtml(content), receiver, attachment);
        resetInput();
        adminMessageNotifications.send("refresh", receiver);
        fetchMessages();
      } catch (Exception e) {
        meta.handleException(e);
      }
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

  public void resetInput() {
    setContent("");
    setTitle("");
    attachment = null;
  }

  public void fetchMessages() {
    if (entity != null)
      try {
        conversation = messagesBean.getConversationWithAdministration(entity.getName(), currentPrincipal.isAdmin());
        return;
      } catch (IntegrityException e) {
        meta.handleException(e);
      }

    conversation = Collections.emptyList();
  }

  public Service[] getPossibleDestinations() {
    return Service.values();
  }

  public List<Snippet> getConversation() {
    return conversation;
  }

  public BasicPrincipalEntity getEntity() {
    return entity;
  }
}
