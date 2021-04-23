package dz.ngnex.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "dossier_file")
public class DossierContentEntity extends BinaryFileEntity implements BinaryContent {
  private static final long serialVersionUID = 5024287020875182255L;

  private byte[] contents;

  protected DossierContentEntity() {
  }

  public DossierContentEntity(String contentType, String name, String uploader, byte[] contents) {
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
    return isEqualsById(other, DossierContentEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }
}
