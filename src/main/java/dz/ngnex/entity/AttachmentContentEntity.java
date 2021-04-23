package dz.ngnex.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "attachment_file")
public class AttachmentContentEntity extends BinaryFileEntity implements BinaryContent {
  private static final long serialVersionUID = 6408776657783546462L;

  private byte[] contents;

  protected AttachmentContentEntity() {
  }

  public AttachmentContentEntity(String contentType, String name, String uploader, byte[] contents) {
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
    return isEqualsById(other, AttachmentContentEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }
}
