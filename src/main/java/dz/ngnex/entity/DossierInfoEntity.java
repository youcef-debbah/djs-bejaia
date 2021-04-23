package dz.ngnex.entity;

import dz.ngnex.control.DossierServlet;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "dossier_file", indexes = @Index(columnList = "uploader,uploadTime", name = "index_dossier_uploader_and_upload_time"))
public class DossierInfoEntity extends BinaryFileEntity implements FileInfo {
  private static final long serialVersionUID = 7569061112314902662L;

  protected DossierInfoEntity() {
  }

  public DossierInfoEntity(String contentType, String name, String uploader) {
    super(contentType, name, uploader);
  }

  @Transient
  @Override
  public String getUrl() {
    return DossierServlet.getUrl(getName());
  }

  @Transient
  public String getUrlAsAttachment() {
    return DossierServlet.getUrlAsAttachment(getName());
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
    return isEqualsById(other, DossierInfoEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }
}
