package dz.ngnex.control;

import dz.ngnex.util.InjectableByTests;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.enterprise.context.SessionScoped;
import javax.servlet.ServletRequest;
import java.io.Serializable;
import java.util.Locale;

@SessionScoped
@InjectableByTests
public class PrincipalPreferences implements Serializable {
  private static final long serialVersionUID = -1752711314963232545L;

  private static final Locale DEFAULT_LOCALE = Locale.FRENCH;

  private Locale locale = null;

  public synchronized void init(@NotNull ServletRequest request) {
    if (this.locale == null)
      this.locale = request.getLocale();
  }

  @NotNull
  public synchronized Locale getLocale() {
    Locale locale = this.locale;
    if (locale != null)
      return (Locale) locale.clone();
    else
      return (Locale) DEFAULT_LOCALE.clone();
  }

  public synchronized void setLocale(@Nullable Locale locale) {
    this.locale = locale;
  }
}
