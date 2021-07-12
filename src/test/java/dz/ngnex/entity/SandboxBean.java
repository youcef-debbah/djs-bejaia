package dz.ngnex.entity;

import dz.ngnex.bean.IntegrityException;
import dz.ngnex.util.TestWithTransaction;

import javax.ejb.Local;
import java.util.List;

@Local
public interface SandboxBean {

    long countPost1();

    List<Post1Entity> selectPost1();

    List<Post2Entity> selectPost2();

    Picture1InfoEntity uploadPicture1(String contentType, String name, String uploader) throws IntegrityException;

    Picture2InfoEntity uploadPicture2(String contentType, String name, String uploader) throws IntegrityException;

    Post1Entity addPost1(Post1Entity post, List<Picture1InfoEntity> asList);

    Post2Entity addPost2(Post2Entity post, List<Picture2InfoEntity> asList);

    void noop() throws InterruptedException;

    void clear();
}
