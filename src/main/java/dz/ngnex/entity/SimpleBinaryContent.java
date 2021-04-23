package dz.ngnex.entity;

import java.util.Objects;

public final class SimpleBinaryContent implements BinaryContent {

  private final byte[] contents;
  private final Integer size;
  private final Long upload;
  private final String name;
  private final String contentType;

  public SimpleBinaryContent(byte[] contents, Long upload, String name, String contentType) {
    this.size = contents.length;
    this.contents = contents;
    this.upload = Objects.requireNonNull(upload);
    this.name = Objects.requireNonNull(name);
    this.contentType = Objects.requireNonNull(contentType);
  }

  @Override
  public byte[] getContents() {
    return contents;
  }

  @Override
  public Integer getSize() {
    return size;
  }

  @Override
  public Long getUploadTime() {
    return upload;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public String toString() {
    return "SimpleBinaryContent{" +
        "size=" + getSize() +
        ", name='" + getName() + '\'' +
        ", contentType='" + getContentType() + '\'' +
        '}';
  }
}
