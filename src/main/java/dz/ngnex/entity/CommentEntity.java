package dz.ngnex.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "comment", indexes = @Index(columnList = "article_id,posted", name = "index_comment_article_id"))
public class CommentEntity implements DatabaseEntity {
  private static final long serialVersionUID = -1832867978064910259L;

  private Integer id;
  private String author;
  private String content;
  private Long posted;
  private Long lastEdit;
  private Integer likes = 0;
  private Integer articleID;

  @Transient
  private FileInfo thumbnails;
  private Integer version = 0;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "comment_id", updatable = false, nullable = false)
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

  @Size(max = Constrains.MAX_IDENTIFIER_SIZE)
  @Column(length = Constrains.MAX_IDENTIFIER_SIZE)
  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
    }

  @NotNull
  @Column(columnDefinition = "TEXT", length = Constrains.MAX_TEXT_LENGTH, nullable = false)
  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
    }

  @NotNull
  @Column(nullable = false)
  public Long getPosted() {
    return posted;
  }

  public void setPosted(Long posted) {
    this.posted = posted;
    }

  public Long getLastEdit() {
    return lastEdit;
  }

  public void setLastEdit(Long lastEdit) {
    this.lastEdit = lastEdit;
    }

  @NotNull
  @Column(nullable = false)
  public Integer getLikes() {
    return likes;
  }

  public void setLikes(Integer likes) {
    this.likes = likes;
    }

  @NotNull
  @Column(name = "article_id", nullable = false)
  public Integer getArticleID() {
    return articleID;
  }

  public void setArticleID(Integer articleID) {
    this.articleID = articleID;
  }

  @Transient
  public void setThumbnails(FileInfo thumbnails) {
    this.thumbnails = thumbnails;
    }

  @Transient
  public FileInfo getThumbnails() {
    return thumbnails;
  }

  @Override
  public String toString() {
    return "CommentEntity{" +
        "id=" + getId() +
        ", author='" + getAuthor() + '\'' +
        '}';
  }

  @Override
  public final boolean equals(Object other) {
    return isEqualsById(other, CommentEntity.class);
  }

  @Override
  public final int hashCode() {
    return getIdHashcode();
  }

  @Override
  @Transient
  public Long getEntityIndex() {
    Long posted = getPosted();
    if (posted == null)
      return null;
    else
      return -posted;
  }
}
