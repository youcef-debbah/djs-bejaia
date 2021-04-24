package dz.ngnex.view;

import dz.ngnex.bean.ImagesBean;
import dz.ngnex.control.CurrentPrincipal;
import dz.ngnex.control.Meta;
import dz.ngnex.entity.FilesStatistics;
import dz.ngnex.entity.ImageInfoEntity;
import dz.ngnex.util.ViewModel;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

@ViewModel
public class UploadedImagesView implements Serializable {

  private static final long serialVersionUID = -790636825991321051L;

  @EJB
  ImagesBean imagesBean;

  private List<ImageInfoEntity> files;
  private FilesStatistics statistics;

  @Inject
  private Meta meta;

  @Inject
  private CurrentPrincipal currentPrincipal;

  @PostConstruct
  private void init() {
    refresh();
  }

  public List<ImageInfoEntity> getFiles() {
    return files;
  }

  public void handleFileUpload(FileUploadEvent event) {
    UploadedFile uploadedFile = event.getFile();
    if (uploadedFile != null) {
      try {
        imagesBean.add(uploadedFile, currentPrincipal.getName());
        meta.dataUpdated("fileUploaded");
        refresh();
      } catch (Exception e) {
        meta.handleException(e);
      }
    }
  }

  public void delete(Integer fileID) {
    try {
      imagesBean.delete(fileID);
      meta.dataUpdated("fileDeleted");
      refresh();
    } catch (Exception e) {
      meta.handleException(e);
    }
  }

  private void refresh() {
    files = imagesBean.getAll();
    statistics = imagesBean.getStatistics();
  }

  public int getMaxFileSize() {
    return ImagesBean.MAX_FILE_SIZE;
  }

  public long getTotalFilesCount() {
    return statistics.getCount();
  }

  public String getFormattedTotalFilesSize() {
    return statistics.getFormattedTotalFilesSize();
  }

  public void toggleGallery(String filename) {
    try {
      imagesBean.toggleGallery(filename);
      meta.dataUpdatedSuccessfully();
      refresh();
    } catch (Exception e) {
      meta.handleException(e);
    }
  }
}
