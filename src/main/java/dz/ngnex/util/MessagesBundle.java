package dz.ngnex.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public final class MessagesBundle implements Messages {

  private static final long serialVersionUID = -3812935492187334476L;
  private final String bundleName;
  private final Mutex mutex = new Mutex();
  private volatile ResourceBundle currentBundle = null;

  public MessagesBundle(@NotNull String bundleName) {
    this.bundleName = Objects.requireNonNull(bundleName);
  }

  @NotNull
  @Override
  public String get(@NotNull String messageKey) {
    return getCurrentBundle().getString(messageKey);
  }

  private ResourceBundle getCurrentBundle() {
    final ResourceBundle currentBundle = this.currentBundle;

    if (currentBundle != null)
      return currentBundle;
    else
      synchronized (mutex) {
        ResourceBundle latestCurrentBundle = this.currentBundle;
        if (latestCurrentBundle != null)
          return latestCurrentBundle;
        else {
          final FacesContext context = FacesContext.getCurrentInstance();
          final ResourceBundle newBundle = context.getApplication().getResourceBundle(context, bundleName);
          this.currentBundle = newBundle;
          return newBundle;
        }
      }
  }

  @Override
  public void invalidate() {
    synchronized (mutex) {
      currentBundle = null;
    }
  }

  @NotNull
  @Override
  public String getString(@NotNull String key) {
    return getCurrentBundle().getString(key);
  }

  @NotNull
  @Override
  public String[] getStringArray(@NotNull String key) {
    return getCurrentBundle().getStringArray(key);
  }

  @Override
  @Nullable
  public Locale getLocale() {
    return getCurrentBundle().getLocale();
  }

  @Override
  public boolean containsKey(@NotNull String key) {
    return getCurrentBundle().containsKey(key);
  }

  private void writeObject(java.io.ObjectOutputStream out)
      throws IOException {
    out.defaultWriteObject();
  }

  private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    Objects.requireNonNull(bundleName);
    Objects.requireNonNull(mutex);
    currentBundle = null;
  }
}
