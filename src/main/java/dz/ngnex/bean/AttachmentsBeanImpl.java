package dz.ngnex.bean;

import dz.ngnex.entity.*;
import dz.ngnex.util.TestWithTransaction;
import org.primefaces.model.file.UploadedFile;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Stateless
@TestWithTransaction
public class AttachmentsBeanImpl implements AttachmentsBean {

  private static final int MAX_SUFFIX_LENGTH = String.valueOf(Integer.MAX_VALUE).length() + 1;

  @Inject
  EntityManager em;

  @Override
  public BinaryContent getBinaryContent(String filename) {
    final List<AttachmentContentEntity> filesInfo = em.createQuery("SELECT f FROM AttachmentContentEntity f where f.name=:name",
        AttachmentContentEntity.class)
        .setParameter("name", filename)
        .getResultList();

    return filesInfo.size() == 0 ? null : filesInfo.get(0);
  }

  @Override
  public AttachmentContentEntity add(UploadedFile file, String uploader, long currentTimeMillis) throws IntegrityException {
    if (file.getSize() > MAX_FILE_SIZE)
      throw new IntegrityException("file too big to be persisted", "fileTooBig");

    FilesStatistics statistics = getStatistics();
    if (statistics.getSize() + file.getSize() > MAX_TOTAL_FILE_SIZE)
      throw new IntegrityException("total files size is big: " + statistics.getSize(), "maxFilesSizeReached");

    AttachmentContentEntity fileEntity = new AttachmentContentEntity(
        file.getContentType(),
        getUniqueFilename(file),
        uploader,
        file.getContent());
    fileEntity.setUploadTime(System.currentTimeMillis());
    fileEntity.setSize((int) file.getSize());

    em.persist(fileEntity);
    return fileEntity;
  }

  private String getUniqueFilename(final UploadedFile file) throws IntegrityException {
    String fileName = file.getFileName()
        .substring(0, Math.min(file.getFileName().length(), Constrains.MAX_FILENAME_LENGTH))
        .replaceAll("\\W", ".")
        .replaceAll("\\.+", ".");

    if (isNameUnique(fileName))
      return fileName;

    final ThreadLocalRandom rng = ThreadLocalRandom.current();

    for (int i = 0; i < 10; i++) {
      String newName = rng.nextInt(Integer.MAX_VALUE) + "_" +
          fileName.substring(0, Math.min(fileName.length(), Constrains.MAX_FILENAME_LENGTH - MAX_SUFFIX_LENGTH));

      if (isNameUnique(newName))
        return newName;
    }

    throw new IntegrityException("could not find an unused filename for: " + fileName, "fileNameAlreadyUsed");
  }

  private boolean isNameUnique(String fileName) {
    return em.createQuery("SELECT count(f.id) from AttachmentInfoEntity f where f.name = :name", Long.class)
        .setParameter("name", fileName)
        .getSingleResult() == 0;
  }

  @Override
  public AttachmentInfoEntity getById(Integer id) {
    return em.find(AttachmentInfoEntity.class, id);
  }

  @Override
  public FilesStatistics getStatistics() {
    return em.createQuery("select new dz.ngnex.entity.FilesStatistics(sum(f.size), count(f.size)) from AttachmentInfoEntity f",
        FilesStatistics.class)
        .getSingleResult();
  }

  @Override
  public BinaryContent getBinaryContent(Integer id) {
    return em.find(AttachmentContentEntity.class, id);
  }
}
