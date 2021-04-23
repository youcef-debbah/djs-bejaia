package dz.ngnex.entity;

public interface BinaryContent {
  byte[] getContents();

  Integer getSize();

  Long getUploadTime();

  String getName();

  String getContentType();
}
