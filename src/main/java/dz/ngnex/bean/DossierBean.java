package dz.ngnex.bean;

import dz.ngnex.entity.BinaryContent;
import dz.ngnex.entity.DossierInfoEntity;
import dz.ngnex.entity.FilesStatistics;
import org.primefaces.model.UploadedFile;

import javax.ejb.Local;
import java.util.List;

@Local
public interface DossierBean {

  int MAX_FILE_SIZE = 1024 * 1024;
  long MAX_TOTAL_FILE_SIZE_PER_UPLOADER = 4 * 1024 * 1024;

  List<? extends BinaryContent> getBinaryContentsFor(String username);

  BinaryContent getBinaryContent(String name);

  DossierInfoEntity getInfo(Integer id);

  DossierInfoEntity getInfo(String name);

  List<DossierInfoEntity> getAll(String uploader);

  BinaryContent add(UploadedFile file, String uploader) throws IntegrityException;

  void delete(Integer fileID);

  FilesStatistics getStatistics(String uploader);

  void updateDossierState(String username, long now);

  void clear();
}
