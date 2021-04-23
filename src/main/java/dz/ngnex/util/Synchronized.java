package dz.ngnex.util;

import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.*;

@Inherited
@InterceptorBinding
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Synchronized {

  @Nonbinding
  By value() default By.WRITE_LOCK;

  enum By {
    WRITE_LOCK,
    READ_LOCK,
    VOLATILE_ACCESS,
    PLAIN_ACCESS
  }

  final class Literal extends AnnotationLiteral<Synchronized> implements Synchronized {
    private static final long serialVersionUID = 6624100268584236568L;

    public static final Synchronized INSTANCE = new Literal();

    private Literal() {
    }

    @Override
    public By value() {
      return By.WRITE_LOCK;
    }
  }
}
