package dz.ngnex.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "image_file")
public class ImageContentEntity extends BinaryFileEntity implements BinaryContent {
  private static final long serialVersionUID = -2754939655698355112L;

  private byte[] contents;

  protected ImageContentEntity() {
  }

  public ImageContentEntity(String contentType, String name, String uploader, byte[] contents) {
    super(contentType, name, uploader);
    setContents(Objects.requireNonNull(contents));
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
    return isEqualsById(other, ImageContentEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }
}
