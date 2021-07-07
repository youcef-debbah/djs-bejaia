package dz.ngnex.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "admin_message", indexes = @Index(name = "index_receiver_name", columnList = "receiverName,epoch"))
public class AdminMessageEntity extends BasicMessageEntity {
  private static final long serialVersionUID = -7367074881197970006L;

  private String receiverName;

  private String adminName;

  private AttachmentInfoEntity adminAttachment;

  public AdminMessageEntity() {
  }

  @Size(max = Constrains.MAX_IDENTIFIER_SIZE)
  @Column(length = Constrains.MAX_IDENTIFIER_SIZE)
  public String getReceiverName() {
    return receiverName;
  }

  public void setReceiverName(String receiverName) {
    this.receiverName = receiverName;
    }

  @NotNull
  @Size(max = Constrains.MAX_IDENTIFIER_SIZE)
  @Column(nullable = false, length = Constrains.MAX_IDENTIFIER_SIZE)
  public String getAdminName() {
    return adminName;
  }

  public void setAdminName(String adminName) {
    this.adminName = adminName;
    }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "adminAttachment", foreignKey = @ForeignKey(name = "fk_admin_message_to_attachment"))
  public AttachmentInfoEntity getAdminAttachment() {
    return adminAttachment;
  }

  public void setAdminAttachment(AttachmentInfoEntity adminAttachment) {
    this.adminAttachment = adminAttachment;
    }

  @Override
  @Transient
  public String getSnippetSourceName() {
    return receiverName != null? BasicMessageEntity.ADMIN_MSG : BasicMessageEntity.GLOBAL_MSG;
  }

  @Override
  public final boolean equals(Object other) {
    return isEqualsById(other, AdminMessageEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }
}
