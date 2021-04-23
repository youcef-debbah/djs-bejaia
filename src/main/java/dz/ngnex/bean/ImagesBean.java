package dz.ngnex.bean;

import dz.ngnex.entity.BinaryContent;
import dz.ngnex.entity.FilesStatistics;
import dz.ngnex.entity.ImageInfoEntity;
import org.primefaces.model.UploadedFile;

import javax.ejb.Local;
import java.util.List;

@Local
public interface ImagesBean {

  int MAX_FILE_SIZE = 15 * 1024 * 1024;
  long MAX_TOTAL_FILE_SIZE = 100 * 1024 * 1024;

  BinaryContent getBinaryContent(String name);

  ImageInfoEntity getInfo(Integer id);

  ImageInfoEntity getInfo(String name);

  List<ImageInfoEntity> getAll();

  BinaryContent add(UploadedFile file, String uploader) throws IntegrityException;

  void delete(Integer fileID);

  FilesStatistics getStatistics();

  List<ImageInfoEntity> getGalleryImages();

  void toggleGallery(String filename);

  void clear();
}
