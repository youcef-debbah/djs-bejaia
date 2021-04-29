package dz.ngnex.entity;

import dz.ngnex.util.Config;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@MappedSuperclass
@NamedNativeQuery(
    name = "BasicMessageEntity.getConversation"
    , query = "SELECT `message_id`, `epoch`, `content`, `source`, `representation`, `attachment` FROM ( " +
    "SELECT u.message_id, u.`epoch`, u.`content`, 'user' as `source`, u.`representation`, u.`attachment` FROM `message` AS u  WHERE u.`senderName` = :username " +
    "UNION ALL " +
    "SELECT a.message_id, a.`epoch`, a.`content`, 'admin' as `source`, a.`representation`, a.`adminAttachment` FROM `admin_message` as a WHERE a.`receiverName` = :username " +
    "UNION ALL " +
    "SELECT a.message_id, a.`epoch`, a.`content`, 'global' as `source`, a.`representation`, a.`adminAttachment` FROM `admin_message` as a WHERE a.`receiverName` is null " +
    ") as sc ORDER BY sc.`epoch`", resultSetMapping = "snippet")

@SqlResultSetMapping(
    name = "snippet",
    classes = {
        @ConstructorResult(
            targetClass = Snippet.class,
            columns = {
                @ColumnResult(name = "message_id"),
                @ColumnResult(name = "epoch"),
                @ColumnResult(name = "content"),
                @ColumnResult(name = "source"),
                @ColumnResult(name = "representation"),
                @ColumnResult(name = "attachment"),
            }
        )
    }
)
public abstract class BasicMessageEntity implements ChronologicalDatabaseEntity {
  private static final long serialVersionUID = 6909094408074960759L;
  public static final int MAX_CONTENT_LENGTH = 32_000;
  public static final String GLOBAL = Config.GLOBAL_MSG;

  protected Integer id;

  protected String content;

  protected Long epoch;

  protected MessageState state = MessageState.NOT_READ_YET;

  protected Integer representation = 0;
  private Integer version = 0;

  protected BasicMessageEntity() {
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "message_id", updatable = false, nullable = false)
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

  @Column(columnDefinition = "TEXT", nullable = false, length = MAX_CONTENT_LENGTH)
  public String getContent() {
    return content;
  }


  public void setContent(String content) {
    this.content = content;
  }

  @NotNull
  @Column(nullable = false)
  public Long getEpoch() {
    return epoch;
  }

  @Override
  @Transient
  public Long getPriority() {
    if (state == null)
      return null;
    else if (state == MessageState.READ)
      return 0L;
    else
      return 1L;
  }

  public void setEpoch(Long epoch) {
    this.epoch = epoch;
  }

  @NotNull
  @Enumerated
  @Column(nullable = false)
  public MessageState getState() {
    return state;
  }

  public void setState(MessageState state) {
    this.state = state;
  }

  @NotNull
  @Column(nullable = false)
  public Integer getRepresentation() {
    return representation;
  }

  public void setRepresentation(Integer representation) {
    this.representation = representation;
  }

  @Transient
  public abstract String getSnippetSourceName();

  @Override
  public String toString() {
    return "MessageEntity{" +
        "id=" + getId() +
        ", content='" + getContent() + '\'' +
        '}';
  }
}
