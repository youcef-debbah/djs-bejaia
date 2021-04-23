package dz.ngnex.bean;

import dz.ngnex.entity.*;
import dz.ngnex.util.TestWithTransaction;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.UploadedFile;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Stateless
@TestWithTransaction
public class ImagesBeanIml implements ImagesBean {

  private static final int MAX_SUFFIX_LENGTH = String.valueOf(Integer.MAX_VALUE).length() + 1;

  @Inject
  private Logger log;

  @Inject
  private EntityManager em;

  @Override
  public ImageContentEntity getBinaryContent(String name) {
    final List<ImageContentEntity> filesInfo = em.createQuery("SELECT f FROM ImageContentEntity f where f.name=:name",
        ImageContentEntity.class)
        .setParameter("name", name)
        .setMaxResults(1)
        .getResultList();

    return filesInfo.isEmpty() ? null : filesInfo.get(0);
  }

  @Override
  public ImageInfoEntity getInfo(Integer id) {
    return em.find(ImageInfoEntity.class, id);
  }

  @Override
  public ImageInfoEntity getInfo(String name) {
    final List<ImageInfoEntity> filesInfo = em.createQuery("SELECT f FROM ImageInfoEntity f where f.name = :name", ImageInfoEntity.class)
        .setParameter("name", name)
        .setMaxResults(1)
        .getResultList();

    return filesInfo.isEmpty() ? null : filesInfo.get(0);
  }

  @Override
  public List<ImageInfoEntity> getAll() {
    return em.createQuery("SELECT f FROM ImageInfoEntity f order by uploadTime desc", ImageInfoEntity.class).getResultList();
  }

  @Override
  public void delete(Integer fileID) {
    if (fileID != null)
      em.createQuery("delete from ImageInfoEntity f where f.id = :fileID")
          .setParameter("fileID", fileID)
          .executeUpdate();
  }

  @Override
  public FilesStatistics getStatistics() {
    return em.createQuery("select new dz.ngnex.entity.FilesStatistics(sum(f.size), count(f.size)) from ImageInfoEntity f", FilesStatistics.class)
        .getSingleResult();
  }

  @Override
  public ImageContentEntity add(UploadedFile file, String uploader) throws IntegrityException {
    if (file.getSize() > MAX_FILE_SIZE)
      throw new IntegrityException("file too big to be persisted", "fileTooBig");

    FilesStatistics statistics = getStatistics();
    if (statistics.getSize() + file.getSize() > MAX_TOTAL_FILE_SIZE)
      throw new IntegrityException("total files size is big: " + statistics.getSize(), "maxFilesSizeReached");

    ImageContentEntity fileEntity = new ImageContentEntity(
        file.getContentType(),
        getUniqueFilename(file),
        uploader,
        file.getContents());

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
    return em.createQuery("SELECT count(f) from ImageInfoEntity f where f.name = :name", Long.class)
        .setParameter("name", fileName)
        .getSingleResult() == 0;
  }

  @Override
  public void toggleGallery(String filename) {
    ImageInfoEntity fileInfo = getInfo(filename);
    if (fileInfo != null) {
      if (fileInfo.getType() == ImageType.GALLEY)
        fileInfo.setType(ImageType.GENERAL_PURPOSE);
      else
        fileInfo.setType(ImageType.GALLEY);
    }
  }

  @Override
  public List<ImageInfoEntity> getGalleryImages() {
    return em.createQuery("SELECT f FROM ImageInfoEntity f where f.type = :imageType order by uploadTime desc", ImageInfoEntity.class)
        .setParameter("imageType", ImageType.GALLEY)
        .getResultList();
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
