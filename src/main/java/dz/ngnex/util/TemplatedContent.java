package dz.ngnex.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplatedContent {

  public static final String VARIABLE_PREFIX = "#[";
  public static final String VARIABLE_SUFFIX = "]";
  public static final Pattern NEW_LINE_PATTERN = Pattern.compile("\\R");
  public static final Pattern BR_ELEMENT_PATTERN = Pattern.compile("<br[ ]?/>");

  private String content;

  public TemplatedContent(@NotNull String content) {
    this.content = Objects.requireNonNull(content);
  }

  @Nullable
  public static String newLinesToBrElements(@Nullable String text) {
    if (text == null || text.isEmpty())
      return text;
    else
      return NEW_LINE_PATTERN.matcher(text).replaceAll("<br />");
  }

  @Nullable
  public static String brElementsToNewLines(@Nullable String text) {
    if (text == null || text.isEmpty())
      return text;
    else
      return BR_ELEMENT_PATTERN.matcher(text).replaceAll("\n");
  }

  public TemplatedContent replace(@Nullable String name, @Nullable String value) {
    String variable = getAsVariableName(name);
    if (variable != null && value != null)
      content = content.replace(variable, value);
    return this;
  }

  @Override
  public String toString() {
    return content;
  }

  @Nullable
  public static String getAsVariableName(@Nullable String name) {
    if (name == null)
      return null;
    else
      return VARIABLE_PREFIX + name + VARIABLE_SUFFIX;
  }

  @NotNull
  public String extractStyleEnclosing(@NotNull String text) {
    Objects.requireNonNull(text);
    Pattern pattern = Pattern.compile("style=[\"']([^\"']*)[\"']>[^<]*" + Pattern.quote(text));
    Matcher matcher = pattern.matcher(content);
    if (matcher.find())
      return matcher.group(1);
    else
      return "";
  }
}
