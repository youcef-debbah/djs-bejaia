package dz.ngnex.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "message", indexes = {
    @Index(columnList = "senderName,epoch", name = "index_sender_name"),
    @Index(columnList = "state,epoch", name = "index_message_state")
})
public class MessageEntity extends BasicMessageEntity implements ClientMessageEntity {
  private static final long serialVersionUID = 1325780366592196266L;
  public static final int MAX_TITLE_LENGTH = 250;
  public static final String ATTACHMENT_PROPERTY = "attachment";

  private String senderName;

  private Service destination;

  private String title;

  private AttachmentInfoEntity attachment;

  public MessageEntity() {
  }

  @NotNull
  @Size(max = Constrains.MAX_IDENTIFIER_SIZE)
  @Column(nullable = false, length = Constrains.MAX_IDENTIFIER_SIZE)
  @Override
  public String getSenderName() {
    return senderName;
  }

  public void setSenderName(String senderName) {
    this.senderName = senderName;
  }

  @NotNull
  @Enumerated
  @Column(nullable = false)
  @Override
  public Service getDestination() {
    return destination;
  }

  public void setDestination(Service destination) {
    this.destination = destination;
  }

  @Column(length = MAX_TITLE_LENGTH)
  @Override
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = ATTACHMENT_PROPERTY, foreignKey = @ForeignKey(name = "fk_message_to_attachment"))
  @Override
  public AttachmentInfoEntity getAttachment() {
    return attachment;
  }

  public void setAttachment(AttachmentInfoEntity attachment) {
    this.attachment = attachment;
  }

  @Override
  @Transient
  public String getSnippetSourceName() {
    return "user";
  }

  @Override
  @Transient
  public boolean isFromGuest() {
    return false;
  }

  @Override
  @Transient
  public String getKey() {
    return "u" + getId();
  }

  @Override
  public final boolean equals(Object other) {
    return isEqualsById(other, MessageEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }
}
