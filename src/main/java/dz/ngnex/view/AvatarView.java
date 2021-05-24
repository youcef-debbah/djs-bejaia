package dz.ngnex.view;

import dz.ngnex.bean.AvatarBean;
import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.AvatarInfoEntity;
import dz.ngnex.util.ViewModel;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SlideEndEvent;
import org.primefaces.model.file.UploadedFile;

import javax.inject.Inject;
import java.io.Serializable;

@ViewModel
public class AvatarView implements Serializable {
  private static final long serialVersionUID = -5415371172410990072L;

  @Inject
  private Meta meta;

  @Inject
  private CurrentPrincipal currentPrincipal;

  public void handleAvatarUpload(FileUploadEvent event) {
    UploadedFile uploadedFile = event.getFile();
    if (uploadedFile != null) {
      try {
        currentPrincipal.setAvatar(uploadedFile);
        meta.dataUpdated("fileUploaded");
      } catch (Exception e) {
        meta.handleException(e);
      }
    }
  }

  public void deleteAvatar() {
    if (currentPrincipal.getAvatar() != null)
      try {
        currentPrincipal.setAvatar(null);
        meta.dataUpdatedSuccessfully();
      } catch (Exception e) {
        meta.handleException(e);
      }
  }

  public int getMaxFileSize() {
    return AvatarBean.MAX_FILE_SIZE;
  }

  public void updateCorrection(SlideEndEvent event) {
    if (event != null)
      try {
        currentPrincipal.setAvatarCorrection((int) event.getValue());
        meta.dataUpdatedSuccessfully();
      } catch (RuntimeException e) {
        meta.handleException(e);
      }
  }

  public int getCorrection() {
    if (currentPrincipal != null) {
      AvatarInfoEntity avatar = currentPrincipal.getAvatar();
      if (avatar != null)
        return avatar.getCorrection();
    }
    return 0;
  }

  public void setCorrection(int value) {
  }


}
