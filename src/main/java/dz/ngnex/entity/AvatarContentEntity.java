package dz.ngnex.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "avatar_image")
public class AvatarContentEntity extends BinaryFileEntity implements BinaryContent {
  private static final long serialVersionUID = 1211952776485713264L;

  private byte[] contents;

  protected AvatarContentEntity() {
    super();
  }

  public AvatarContentEntity(String contentType, String name, String uploader, byte[] contents) {
    super(contentType, name, uploader);
    setContents(contents);
  }

  @Lob
  @Basic(fetch = FetchType.LAZY)
  @Column(nullable = false, columnDefinition = "mediumblob")
  public byte[] getContents() {
    return contents;
  }

  public void setContents(byte[] contents) {
    this.contents = contents;
    }

  @Override
  public final boolean equals(Object other) {
    return isEqualsById(other, AvatarContentEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }
}
