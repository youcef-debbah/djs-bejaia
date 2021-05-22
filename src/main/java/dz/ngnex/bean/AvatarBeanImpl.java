package dz.ngnex.bean;

import dz.ngnex.entity.*;
import dz.ngnex.util.TestWithTransaction;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.file.UploadedFile;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Stateless
@TestWithTransaction
public class AvatarBeanImpl implements AvatarBean {
  private static final int MAX_SUFFIX_LENGTH = String.valueOf(Integer.MAX_VALUE).length() + 1;

  @Inject
  private Logger log;

  @Inject
  private EntityManager em;

  @Override
  public AvatarContentEntity getBinaryContent(String name) {
    final List<AvatarContentEntity> filesInfo = em.createQuery("SELECT f FROM AvatarContentEntity f where f.name=:name",
        AvatarContentEntity.class)
        .setParameter("name", name)
        .setMaxResults(1)
        .getResultList();

    return filesInfo.isEmpty() ? null : filesInfo.get(0);
  }

  @Override
  public AvatarInfoEntity getInfo(Integer id) {
    return em.find(AvatarInfoEntity.class, id);
  }

  @Override
  public AvatarInfoEntity getInfoByUploader(String uploader) {
    final List<AvatarInfoEntity> filesInfo = em.createQuery("SELECT f FROM AvatarInfoEntity f where f.uploader = :uploader", AvatarInfoEntity.class)
        .setParameter("uploader", uploader)
        .setMaxResults(1)
        .getResultList();

    return filesInfo.isEmpty() ? null : filesInfo.get(0);
  }

  @Override
  public void deleteByUploader(String uploader) {
    if (uploader != null) {
      em.createQuery("delete from AvatarInfoEntity f where f.uploader = :uploader")
          .setParameter("uploader", uploader)
          .executeUpdate();
    }
  }

  @Override
  public FilesStatistics getStatistics() {
    return em.createQuery("select new dz.ngnex.entity.FilesStatistics(sum(f.size), count(f.size)) from AvatarInfoEntity f", FilesStatistics.class)
        .getSingleResult();
  }

  @Override
  public AvatarContentEntity add(UploadedFile file, String uploader) throws IntegrityException {
    if (file.getSize() > MAX_FILE_SIZE)
      throw new IntegrityException("file too big to be persisted", "fileTooBig");

    deleteByUploader(uploader);

    AvatarContentEntity fileEntity = new AvatarContentEntity(
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
    return em.createQuery("SELECT count(f.id) from AvatarInfoEntity f where f.name = :name", Long.class)
        .setParameter("name", fileName)
        .getSingleResult() == 0;
  }

  @Override
  @TestWithTransaction(traceSQL = false)
  public void clear() {
    BeanUtil.clearCache(em);
  }

  @AroundInvoke
  public Object benchmarkCalls(InvocationContext ctx) throws Exception {
    return BeanUtil.benchmarkCall(log, ctx);
  }
}
