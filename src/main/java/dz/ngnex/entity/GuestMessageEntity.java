package dz.ngnex.entity;

import dz.ngnex.util.HtmlCleaner;
import dz.ngnex.util.WebKit;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

@Entity
@Table(name = "guest_message", indexes = {
    @Index(columnList = "state,epoch", name = "index_guest_message_state")
})
public class GuestMessageEntity extends BasicMessageEntity implements ClientMessageEntity {
  private static final long serialVersionUID = 1325780366592196266L;
  public static final int MAX_TITLE_LENGTH = 250;

  private String guestName;

  private String phone;

  private String email;

  private Service destination;

  private String title;

  private AttachmentInfoEntity attachment;

  public GuestMessageEntity() {
  }

  public GuestMessageEntity(GuestMessageEntity draft) {
    setContent(HtmlCleaner.secureHtml(WebKit.requireNotBlank(draft.getContent())));
    setGuestName(WebKit.requireNotBlank(draft.getGuestName()));
    setPhone(WebKit.requireNotBlank(draft.getPhone()));
    setEmail(WebKit.requireNotBlank(draft.getEmail()));
    setDestination(Objects.requireNonNull(draft.getDestination()));
    setTitle(draft.getTitle());
    setAttachment(null);
  }

  @NotNull
  @Size(max = Constrains.MAX_IDENTIFIER_SIZE)
  @Column(nullable = false, length = Constrains.MAX_IDENTIFIER_SIZE)
  public String getGuestName() {
    return guestName;
  }

  public void setGuestName(String senderName) {
    this.guestName = senderName;
  }

  @NotNull
  @Size(max = Constrains.MAX_PHONE_NUMBER_SIZE)
  @Column(nullable = false, length = Constrains.MAX_PHONE_NUMBER_SIZE)
  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  @NotNull
  @Size(max = Constrains.MAX_EMAIL_LENGTH)
  @Column(nullable = false, length = Constrains.MAX_EMAIL_LENGTH)
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @NotNull
  @Enumerated
  @Column(nullable = false)
  public Service getDestination() {
    return destination;
  }

  public void setDestination(Service destination) {
    this.destination = destination;
  }

  @Column(length = MAX_TITLE_LENGTH)
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attachment", foreignKey = @ForeignKey(name = "fk_guest_message_to_attachment"))
  public AttachmentInfoEntity getAttachment() {
    return attachment;
  }

  public void setAttachment(AttachmentInfoEntity attachment) {
    this.attachment = attachment;
  }

  @Override
  @Transient
  public String getSnippetSourceName() {
    return BasicMessageEntity.GLOBAL;
  }

  @Override
  @Transient
  public String getSenderName() {
    return null;
  }

  @Override
  @Transient
  public boolean isFromGuest() {
    return true;
  }

  @Override
  @Transient
  public String getKey() {
    return "g" + getId();
  }

  @Override
  public final boolean equals(Object other) {
    return isEqualsById(other, GuestMessageEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }
}
