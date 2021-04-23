package dz.ngnex.util;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Locale;

public interface Messages extends Serializable {
  @NotNull
  String get(@NotNull String messageKey);

  void invalidate();

  @NotNull
  String getString(@NotNull String key);

  @NotNull
  String[] getStringArray(@NotNull String key);

  Locale getLocale();

  boolean containsKey(@NotNull String key);
}
