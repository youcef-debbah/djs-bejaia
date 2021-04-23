package dz.ngnex.entity;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

public final class FilesStatistics implements Serializable {
  private static final long serialVersionUID = -2141701885328979188L;

  public static final int ONE_KB = 1024;
  public static final int ONE_MB = 1024 * ONE_KB;

  private final long size;
  private final String formattedSize;
  private final long count;

  public FilesStatistics(Long size, Long count) {
    long rawSize = size == null ? 0 : size;
    this.size = rawSize;
    this.formattedSize = format(rawSize);
    this.count = count == null ? 0 : count;
  }

  public static String format(long size) {
    if (size < ONE_KB)
      return size + " Bytes";
    else if (size < ONE_MB)
      return String.format("%.2f KB", ((float) size) / ONE_KB);
    else
      return String.format("%.2f MB", ((float) size) / ONE_MB);
  }

  public long getSize() {
    return size;
  }

  public long getCount() {
    return count;
  }

  public String getFormattedTotalFilesSize() {
    return formattedSize;
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    Objects.requireNonNull(formattedSize);
  }
}

