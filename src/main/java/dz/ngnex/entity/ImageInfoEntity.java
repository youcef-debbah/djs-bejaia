package dz.ngnex.entity;

import dz.ngnex.control.ImagesServlet;
import org.jetbrains.annotations.NotNull;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.*;

/**
 * @author youcef debbah
 */
@Entity
@Table(name = "image_file", indexes = {
    @Index(columnList = "uploadTime", name = "index_image_upload_time"),
    @Index(columnList = "type,uploadTime", name = "index_image_type_and_upload_time")
})
public class ImageInfoEntity extends BinaryFileEntity implements FileInfo {
  private static final long serialVersionUID = 7909145091763030038L;

  private ImageType type;

  protected ImageInfoEntity() {
  }

  public ImageInfoEntity(String contentType, String name, String uploader) {
    super(contentType, name, uploader);
  }

  @NotNull
  @Enumerated
  public ImageType getType() {
    if (type == null)
      return ImageType.GENERAL_PURPOSE;
    else
      return type;
  }

  public void setType(ImageType type) {
    this.type = type;
    }

  @Transient
  @Override
  public String getUrl() {
    return ImagesServlet.getUrl(getName());
  }

  @Transient
  public String getUrlAsAttachment() {
    return ImagesServlet.getUrlAsAttachment(getName());
  }

  @Transient
  @Override
  public String getFullUrl() {
    ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
    return context.getRequestServerName() + ":" + context.getRequestServerPort() + getUrl();
  }

  @Override
  public String toString() {
    return getUrl();
  }

  @Override
  public final boolean equals(Object other) {
    return isEqualsById(other, ImageInfoEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }
}
