package dz.ngnex.entity;

import dz.ngnex.bean.MessagesBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class Snippet {

  public static final int IS_CHAINED_WITH_PREVIEWS = 1;
  public static final int IS_CHAINED_WITH_NEXT = 2;
  public static final int IS_CONVERSATION_END_MASK = 4;

  private final int message_id;

  private final long stamp;

  private final String value;

  private final String source;

  private final int representation;

  private final Integer attachment;

  public Snippet(Object message_id, Object stamp, Object value, Object source, Object representation, Object attachment) {
    this.message_id = Integer.parseInt(message_id.toString());
    this.stamp = Long.parseLong(stamp.toString());
    this.value = Objects.requireNonNull(value.toString());
    this.source = Objects.requireNonNull(source.toString());
    this.representation = Integer.parseInt(representation.toString());

    if (attachment != null)
      this.attachment = Integer.parseInt(attachment.toString());
    else
      this.attachment = null;
  }

  public int getMessage_id() {
    return message_id;
  }

  public long getStamp() {
    return stamp;
  }

  @NotNull
  public String getValue() {
    return value;
  }

  @NotNull
  public String getSource() {
    return source;
  }

  public int getRepresentation() {
    return representation;
  }

  public boolean isChainedWithPreviews() {
    return (representation & IS_CHAINED_WITH_PREVIEWS) != 0;
  }

  public boolean isChainedWithNext() {
    return (representation & IS_CHAINED_WITH_NEXT) != 0;
  }

  public boolean isConversationEnd() {
    return (representation & IS_CONVERSATION_END_MASK) != 0;
  }

  @Nullable
  public Integer getAttachment() {
    return attachment;
  }

  private static int addAttribute(int representation, int mask) {
    return representation | mask;
  }

  public static int chainWithPreviews(int representation) {
    return addAttribute(representation, IS_CHAINED_WITH_PREVIEWS);
  }

  public static int chainWithNext(int representation) {
    return addAttribute(representation, IS_CHAINED_WITH_NEXT);
  }

  public static int markAsConversationEnd(int representation) {
    return addAttribute(representation, IS_CONVERSATION_END_MASK);
  }

  public String getStyleClasses() {
    StringBuilder styleClasses = new StringBuilder();

    if (isChainedWithPreviews())
      styleClasses.append("chained-previews ");

    if (isChainedWithNext())
      styleClasses.append("chained-next ");

    return styleClasses.toString();
  }

  public boolean isOldEnoughToShowDate() {
    return System.currentTimeMillis() > (getStamp() + MessagesBean.DURATION_TO_SHOW_DATE);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Snippet))
      return false;
    Snippet snippet = (Snippet) o;
    return getMessage_id() == snippet.getMessage_id() &&
        getStamp() == snippet.getStamp() &&
        getRepresentation() == snippet.getRepresentation() &&
        getValue().equals(snippet.getValue()) &&
        getSource().equals(snippet.getSource()) &&
        Objects.equals(getAttachment(), snippet.getAttachment());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getMessage_id(), getStamp(), getValue(), getSource(), getRepresentation(), getAttachment());
  }
}
