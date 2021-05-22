package dz.ngnex.bean;

import dz.ngnex.bean.IntegrityException;
import dz.ngnex.entity.AttachmentContentEntity;
import dz.ngnex.entity.AttachmentInfoEntity;
import dz.ngnex.entity.BinaryContent;
import dz.ngnex.entity.FilesStatistics;
import org.primefaces.model.file.UploadedFile;

public interface AttachmentsBean {
  int MAX_FILE_SIZE = 15 * 1024 * 1024;
  long MAX_TOTAL_FILE_SIZE = 250 * 1024 * 1024;

  BinaryContent getBinaryContent(String filename);

  AttachmentContentEntity add(UploadedFile file, String uploader, long currentTimeMillis) throws IntegrityException;

  AttachmentInfoEntity getById(Integer id);

  FilesStatistics getStatistics();

  BinaryContent getBinaryContent(Integer id);
}
