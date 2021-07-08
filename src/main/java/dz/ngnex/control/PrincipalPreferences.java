package dz.ngnex.control;

import dz.ngnex.util.InjectableByTests;
import dz.ngnex.util.WebKit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Locale;

@SessionScoped
@InjectableByTests
public class PrincipalPreferences implements Serializable {
    private static final long serialVersionUID = -1752711314963232545L;

    private static final Locale DEFAULT_LOCALE = Locale.FRENCH;

    private Locale locale = null;

    @PostConstruct
    public synchronized void init() {
        Locale initialLocal = WebKit.getRequestLocale();
        if (initialLocal == null)
            initialLocal = WebKit.getFacesLocale();
        setLocale(initialLocal);
    }

    @NotNull
    public synchronized Locale getLocale() {
        Locale locale = this.locale;
        if (locale != null)
            return (Locale) locale.clone();
        else
            return getDefaultLocale();
    }

    @NotNull
    public Locale getDefaultLocale() {
        return (Locale) DEFAULT_LOCALE.clone();
    }

    public synchronized void setLocale(@Nullable Locale locale) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            UIViewRoot viewRoot = context.getViewRoot();
            if (viewRoot != null)
                viewRoot.setLocale(locale);
        }
        this.locale = locale;
    }
}
