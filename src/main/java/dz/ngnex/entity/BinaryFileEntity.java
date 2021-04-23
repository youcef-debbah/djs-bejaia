/*
 * Copyright (c) 2016 youcef debbah (youcef-kun@hotmail.fr)
 *
 * This file is part of cervex.
 *
 * cervex is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * cervex is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with cervex.  If not, see <http://www.gnu.org/licenses/>.
 *
 * created on 2020/06/13
 * @header
 */

package dz.ngnex.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.Objects;

@MappedSuperclass
public abstract class BinaryFileEntity implements DatabaseEntity {

  private static final long serialVersionUID = -7133410117981311430L;

  protected Integer id;

  protected String contentType;

  protected Long uploadTime;

  protected String name;

  protected String uploader;

  protected Integer size;

  @Transient
  private String formattedSize;

  @Transient
  private Date uploadTimeAsDate;

  private Integer version = 0;

  protected BinaryFileEntity() {
  }

  protected BinaryFileEntity(String contentType, String name, String uploader) {
    setContentType(Objects.requireNonNull(contentType));
    setName(Objects.requireNonNull(name));
    setUploader(Objects.requireNonNull(uploader));
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "binary_file_id", updatable = false, nullable = false)
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
    }

  @Version
  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
    }

  @NotNull
  @Size(max = Constrains.MAX_FILENAME_LENGTH)
  @Column(nullable = false, length = Constrains.MAX_FILENAME_LENGTH)
  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
    }

  @NotNull
  @Column(nullable = false)
  public Long getUploadTime() {
    return uploadTime;
  }

  public void setUploadTime(Long uploadTime) {
    this.uploadTime = uploadTime;
    this.uploadTimeAsDate = new Date(uploadTime);
    }

  @NotNull
  @Size(max = Constrains.MAX_IDENTIFIER_SIZE)
  @Column(unique = true, nullable = false, length = Constrains.MAX_IDENTIFIER_SIZE)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    }

  @NotNull
  @Size(max = Constrains.MAX_IDENTIFIER_SIZE)
  @Column(nullable = false, length = Constrains.MAX_IDENTIFIER_SIZE)
  public String getUploader() {
    return uploader;
  }

  public void setUploader(String uploader) {
    this.uploader = uploader;
    }

  @NotNull
  @Column(nullable = false)
  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
    this.formattedSize = FilesStatistics.format(size);
    }

  @Transient
  public Date getUploadTimeAsDate() {
    return uploadTimeAsDate;
  }

  @Transient
  public String getFormattedSize() {
    return formattedSize;
  }

  @Transient
  public String getNameWithoutExtension() {
    String name = getName();
    int index = name.lastIndexOf('.');
    if (index > 0)
      return name.substring(0, index);
    else
      return name;
  }

  @Override
  public String toString() {
    return "BinaryFileEntity{" +
        "id=" + getId() +
        ", name='" + getName() + '\'' +
        ", uploader='" + getUploader() + '\'' +
        '}';
  }
}
