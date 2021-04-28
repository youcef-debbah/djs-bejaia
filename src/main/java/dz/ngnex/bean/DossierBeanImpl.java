package dz.ngnex.bean;

import dz.ngnex.entity.*;
import dz.ngnex.util.TestWithTransaction;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.UploadedFile;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Stateless
@TestWithTransaction
public class DossierBeanImpl implements DossierBean {

  private static final int MAX_SUFFIX_LENGTH = String.valueOf(Integer.MAX_VALUE).length() + 1;

  @Inject
  private Logger log;

  @Inject
  private EntityManager em;

  @EJB
  private PrincipalBean principalBean;

  @Override
  public List<? extends BinaryContent> getBinaryContentsFor(String username) {
    Objects.requireNonNull(username);
    return em.createQuery("SELECT f FROM DossierContentEntity f where f.uploader = :name", DossierContentEntity.class)
        .setParameter("name", username)
        .getResultList();
  }

  @Override
  public BinaryContent getBinaryContent(String name) {
    Objects.requireNonNull(name);
    final List<DossierContentEntity> filesInfo = em.createQuery("SELECT f FROM DossierContentEntity f where f.name=:name", DossierContentEntity.class)
        .setParameter("name", name)
        .setMaxResults(1)
        .getResultList();

    return filesInfo.isEmpty() ? null : filesInfo.get(0);
  }

  @Override
  public DossierInfoEntity getInfo(Integer id) {
    return em.find(DossierInfoEntity.class, id);
  }

  @Override
  public DossierInfoEntity getInfo(String name) {
    final List<DossierInfoEntity> filesInfo = em.createQuery("SELECT f FROM DossierInfoEntity f where f.name = :name", DossierInfoEntity.class)
        .setParameter("name", name)
        .setMaxResults(1)
        .getResultList();

    return filesInfo.isEmpty() ? null : filesInfo.get(0);
  }

  @Override
  public List<DossierInfoEntity> getAll(String uploader) {
    Objects.requireNonNull(uploader);
    return em.createQuery("SELECT f FROM DossierInfoEntity f where f.uploader = :uploader order by uploadTime desc", DossierInfoEntity.class)
        .setParameter("uploader", uploader)
        .getResultList();
  }

  @Override
  public void delete(Integer fileID) {
    DossierInfoEntity info = getInfo(fileID);
    if (info != null) {
      em.remove(info);
      updateDossierState(info.getUploader(), System.currentTimeMillis());
    }
  }

  @Override
  public FilesStatistics getStatistics(String uploader) {
    Objects.requireNonNull(uploader);
    return em.createQuery("select new dz.ngnex.entity.FilesStatistics(sum(f.size), count(f.size)) from DossierInfoEntity f where f.uploader = :uploader", FilesStatistics.class)
        .setParameter("uploader", uploader)
        .setMaxResults(1)
        .getSingleResult();
  }

  @Override
  public DossierContentEntity add(UploadedFile file, String uploader) throws IntegrityException {
    if (file.getSize() > MAX_FILE_SIZE)
      throw new IntegrityException("file too big to be persisted", "fileTooBig");

    FilesStatistics statistics = getStatistics(uploader);
    if (statistics.getSize() + file.getSize() > MAX_TOTAL_FILE_SIZE_PER_UPLOADER)
      throw new IntegrityException("total files size is big: " + statistics.getSize(), "maxFilesSizeReached");

    DossierContentEntity fileEntity = new DossierContentEntity(
        file.getContentType(),
        getUniqueFilename(file),
        uploader,
        file.getContents());

    long now = System.currentTimeMillis();
    fileEntity.setUploadTime(now);
    fileEntity.setSize((int) file.getSize());

    em.persist(fileEntity);
    updateDossierState(uploader, now);
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
    return em.createQuery("SELECT count(f.id) from DossierInfoEntity f where f.name = :name", Long.class)
        .setParameter("name", fileName)
        .getSingleResult() == 0;
  }

  @Override
  public void updateDossierState(String username, long now) {
    if (username != null) {
      int updatedSoFar = em.createQuery("update SportAssociationEntity s set s.lastUpdate = :updateTime, s.uploadedFilesCount = (select count(f.id) from DossierInfoEntity f where f.uploader = :username) where s.name = :username")
          .setParameter("updateTime", now)
          .setParameter("username", username)
          .executeUpdate();

      if (updatedSoFar == 0)
        em.createQuery("update YouthAssociationEntity s set s.lastUpdate = :updateTime, s.uploadedFilesCount = (select count(f.id) from DossierInfoEntity f where f.uploader = :username) where s.name = :username")
            .setParameter("updateTime", now)
            .setParameter("username", username)
            .executeUpdate();
    }
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
