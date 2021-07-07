package dz.ngnex.view;

import dz.ngnex.bean.AttachmentsBean;
import dz.ngnex.bean.ImagesBean;
import dz.ngnex.bean.MessagesBean;
import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.control.LocaleManager;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.AttachmentContentEntity;
import dz.ngnex.entity.BasicMessageEntity;
import dz.ngnex.util.ViewModel;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;

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

    @EJB
    private AttachmentsBean attachmentsBean;

    @NotNull
    @Size(max = BasicMessageEntity.MAX_CONTENT_LENGTH)
    private String content;

    private AttachmentContentEntity attachment;

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
                messagesBean.sendGlobalAdminMessage(currentPrincipal.getName(), localeManager.formatTextAsHtml(content), attachment);
                resetInput();
                meta.workDoneSuccessfully("messageSent");
                PrimeFaces.current().executeScript("PF('global_msg_dialog').hide()");
                globalMessageNotifications.send("refresh");
            }
        } catch (Exception e) {
            meta.handleException(e);
        }
    }

    private void resetInput() {
        content = null;
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

    public int getMaxFileSize() {
        return AttachmentsBean.MAX_FILE_SIZE;
    }
}
