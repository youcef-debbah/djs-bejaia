package dz.ngnex.bean;

import dz.ngnex.entity.AvatarInfoEntity;
import dz.ngnex.entity.BinaryContent;
import dz.ngnex.entity.FilesStatistics;
import org.primefaces.model.file.UploadedFile;

import javax.ejb.Local;

@Local
public interface AvatarBean {
  int MAX_FILE_SIZE = 800 * 1024;

  BinaryContent getBinaryContent(String filename);

  AvatarInfoEntity getInfo(Integer id);

  AvatarInfoEntity getInfoByUploader(String name);

  BinaryContent add(UploadedFile file, String uploader) throws IntegrityException;

  void deleteByUploader(String uploader);

  FilesStatistics getStatistics();

  void clear();
}
