package dz.ngnex.entity;

public interface ClientMessageEntity extends ChronologicalDatabaseEntity {

  Integer getId();

  String getContent();

  Long getEpoch();

  MessageState getState();

  String getSenderName();

  Service getDestination();

  String getTitle();

  AttachmentInfoEntity getAttachment();

  void setAttachment(AttachmentInfoEntity attachment);

  boolean isFromGuest();

  String getKey();
}
