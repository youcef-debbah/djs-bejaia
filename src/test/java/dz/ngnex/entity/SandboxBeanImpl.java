package dz.ngnex.entity;

import dz.ngnex.bean.BeanUtil;
import dz.ngnex.bean.IntegrityException;
import dz.ngnex.testkit.TestConfig;
import dz.ngnex.util.TestWithTransaction;
import org.hibernate.Hibernate;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Stateless
@TestWithTransaction
public class SandboxBeanImpl implements SandboxBean {

    private static final int MAX_SUFFIX_LENGTH = String.valueOf(Integer.MAX_VALUE).length() + 1;
    private static final Integer MOCK_SIZE = 1204;


    public volatile long blackHole = 0;

    @Inject
    private EntityManager em;

    @Override
    public long countPost2() {
        return em.createQuery("select count(p.id) from Post2Entity p", Long.class)
                .getSingleResult();
    }

    @Override
    public List<Post1Entity> selectPost1() {
        List<Post1Entity> result = em.createQuery("select p from Post1Entity p", Post1Entity.class)
                .setMaxResults(TestConfig.SELECT_LIMIT)
                .getResultList();
        if (!result.isEmpty())
            Hibernate.initialize(result.get(0).getImages());
        return result;
    }

    @Override
    public List<Post2Entity> selectPost2() {
        List<Post2Entity> resultList = em.createQuery("select p from Post2Entity p", Post2Entity.class)
                .setMaxResults(TestConfig.SELECT_LIMIT)
                .getResultList();
        if (!resultList.isEmpty())
            Hibernate.initialize(resultList.get(0));
        return resultList;
    }

    @Override
    public void noop() throws InterruptedException {
        Thread.sleep(400);
        blackHole = System.nanoTime();
    }

    @Override
    public Picture1InfoEntity uploadPicture1(String contentType, String filename, String uploader) throws IntegrityException {
        Picture1InfoEntity fileEntity = new Picture1InfoEntity(
                contentType,
                getUniqueFilename(filename),
                uploader
        );

        fileEntity.setUploadTime(System.currentTimeMillis());
        fileEntity.setSize(MOCK_SIZE);

        em.persist(fileEntity);
        return fileEntity;
    }

    @Override
    public Picture2InfoEntity uploadPicture2(String contentType, String filename, String uploader) throws IntegrityException {
        Picture2InfoEntity fileEntity = new Picture2InfoEntity(
                contentType,
                getUniqueFilename(filename),
                uploader
        );

        fileEntity.setUploadTime(System.currentTimeMillis());
        fileEntity.setSize(MOCK_SIZE);

        em.persist(fileEntity);
        return fileEntity;
    }

    @Override
    public Post1Entity addPost1(Post1Entity post, List<Picture1InfoEntity> pics) {
        long now = System.currentTimeMillis();
        post.setCreated(now);
        post.setLastUpdate(now);
        em.persist(post);

        for (Picture1InfoEntity pic : pics)
            post.addPicture(pic);

        return post;
    }

    @Override
    public Post2Entity addPost2(Post2Entity post, List<Picture2InfoEntity> pics) {
        long now = System.currentTimeMillis();
        post.setCreated(now);
        post.setLastUpdate(now);
        em.persist(post);

        for (Picture2InfoEntity pic : pics)
            post.addPicture(pic);

        return post;
    }

    private String getUniqueFilename(final String rawFileName) throws IntegrityException {
        String fileName = rawFileName
                .substring(0, Math.min(rawFileName.length(), Constrains.MAX_FILENAME_LENGTH))
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
        // in this case the name is indexed so the database will immediately return 1 or 0
        // otherwise you may want to optimize this, depending on how many times it is called
        return em.createQuery("SELECT count(f.id) from Picture1InfoEntity f where f.name = :name", Long.class)
                .setParameter("name", fileName)
                .getSingleResult() == 0;
    }

    @Override
    @TestWithTransaction()
    public void clear() {
        BeanUtil.clearCache(em);
    }

    @Override
    public String toString() {
        return "SandboxBeanImpl{" +
                "blackHole=" + blackHole +
                '}';
    }
}
