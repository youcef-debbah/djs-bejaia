package dz.ngnex.util;

import javax.enterprise.inject.Stereotype;
import javax.inject.Singleton;
import javax.persistence.Inheritance;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates that this type should be managed by CDI during tests
 */
@Stereotype
@Retention(RUNTIME)
@Target(TYPE)
@Singleton
public @interface InjectableByTests {
}
