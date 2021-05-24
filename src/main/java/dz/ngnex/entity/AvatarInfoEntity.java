package dz.ngnex.entity;


import dz.ngnex.control.AvatarServlet;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author youcef debbah
 */
@Entity
@Table(name = "avatar_image", indexes = @Index(columnList = "uploader", name = "index_avatar_uploader"))
public class AvatarInfoEntity extends BinaryFileEntity implements FileInfo {
  private static final long serialVersionUID = 7909145091763030038L;

  private Integer correction;

  protected AvatarInfoEntity() {
  }

  public AvatarInfoEntity(String contentType, String name, String uploader) {
    super(contentType, name, uploader);
  }

  public Integer getCorrection() {
    if (correction == null)
      return 0;
    else
      return correction;
  }

  public void setCorrection(Integer correction) {
    this.correction = correction;
  }

  @Transient
  @Override
  public String getUrl() {
    return AvatarServlet.getUrl(getName());
  }

  @Transient
  public String getUrlAsAttachment() {
    return AvatarServlet.getUrlAsAttachment(getName());
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
    return isEqualsById(other, AvatarInfoEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }
}
