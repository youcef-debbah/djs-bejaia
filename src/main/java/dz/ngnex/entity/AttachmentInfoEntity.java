package dz.ngnex.entity;

import dz.ngnex.control.AttachmentServlet;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "attachment_file", indexes = @Index(columnList = "uploader,uploadTime", name = "index_attachment_uploader_and_upload_time"))
public class AttachmentInfoEntity extends BinaryFileEntity implements FileInfo {
  private static final long serialVersionUID = 7738445664749312324L;

  protected AttachmentInfoEntity() {
  }

  public AttachmentInfoEntity(String contentType, String name, String uploader) {
    super(contentType, name, uploader);
  }

  @Transient
  @Override
  public String getUrl() {
    return AttachmentServlet.getUrl(getName());
  }

  @Transient
  public String getUrlAsAttachment() {
    return AttachmentServlet.getUrlAsAttachment(getName());
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
    return isEqualsById(other, AttachmentInfoEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }
}
