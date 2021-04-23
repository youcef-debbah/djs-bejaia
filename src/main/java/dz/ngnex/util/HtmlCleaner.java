package dz.ngnex.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

final public class HtmlCleaner {
  private String html;

  public HtmlCleaner(@NotNull String html) {
    this.html = Objects.requireNonNull(html);
  }

  public HtmlCleaner removeTag(@NotNull String tag) {
    Objects.requireNonNull(tag);
    this.html = html.replaceAll("<" + tag + "\\b.*?</" + tag + ">", "")
        .replaceAll("<" + tag + "\\b.*?$", "");
    return this;
  }

  @NotNull
  public String getHtml() {
    return html;
  }

  @Nullable
  public static String secureHtml(@Nullable String html) {
    if (html == null)
      return null;
    else {
      return new HtmlCleaner(html)
          .removeTag("script")
          .removeTag("noscript")
          .removeTag("object")
          .removeTag("applet")
          .removeTag("embed")
          .getHtml();
    }
  }
}
