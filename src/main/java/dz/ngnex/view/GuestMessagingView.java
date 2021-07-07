package dz.ngnex.view;

import dz.ngnex.bean.AttachmentsBean;
import dz.ngnex.bean.MessagesBean;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.*;
import dz.ngnex.util.Config;
import dz.ngnex.util.ViewModel;
import dz.ngnex.util.WebKit;
import org.intellij.lang.annotations.Language;
import org.primefaces.event.FileUploadEvent;

import javax.ejb.EJB;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Arrays;

@ViewModel
public class GuestMessagingView implements Serializable {
  private static final long serialVersionUID = 2821593938969431995L;

  @EJB
  private MessagesBean messagesBean;

  @EJB
  private AttachmentsBean attachmentsBean;

  @Inject
  Meta meta;

  @Push
  @Inject
  private PushContext messageNotifications;

  private AttachmentContentEntity attachment;

  private GuestMessageEntity draft;

  @Language("RegExp")
  private final String emailRegex = Constrains.EMAIL_REGEX;

  public GuestMessageEntity getDraft() {
    if (draft == null)
      draft = new GuestMessageEntity();
    return draft;
  }

  public void handleAttachment(FileUploadEvent event) {
    try {
      AttachmentContentEntity uploadedFile = attachmentsBean.add(event.getFile(), AttachmentContentEntity.GUEST, System.currentTimeMillis());
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

  public void sendMessage() {
    try {
      GuestMessageEntity messageDraft = getDraft();
      messagesBean.sendMessage(messageDraft, attachment);
      messageNotifications.send("refresh", Arrays.asList(messageDraft.getDestination().name(), BasicMessageEntity.GLOBAL_MSG));
      this.draft = null;
      meta.workDoneSuccessfully("messageSent");
      meta.keepMessages();
      WebKit.redirect(Config.HOME_PAGE);
    } catch (RuntimeException e) {
      meta.handleException(e);
    }
  }

  public Service[] getPossibleDestinations() {
    return Service.values();
  }

  public String getEmailRegex() {
    return emailRegex;
  }
}
