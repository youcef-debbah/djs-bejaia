package dz.ngnex.util;

import javax.enterprise.inject.Stereotype;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Named
@ViewScoped
@Synchronized
@Stereotype
@Documented
@Target({TYPE, METHOD, FIELD})
@Retention(RUNTIME)
public @interface ViewModel {
}
