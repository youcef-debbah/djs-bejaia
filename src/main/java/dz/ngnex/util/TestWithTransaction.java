package dz.ngnex.util;

import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@InjectableByTests
@Inherited
@InterceptorBinding
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface TestWithTransaction {

  @Nonbinding
  int timeout() default 0;

  @Nonbinding
  boolean rollbackOnly() default false;

  @Nonbinding
  boolean traceSQL() default true;

  final class Literal extends AnnotationLiteral<TestWithTransaction> implements TestWithTransaction {
    private static final long serialVersionUID = -2839485981918201119L;

    public static final TestWithTransaction INSTANCE = new Literal();

    private Literal() {
    }

    @Override
    public int timeout() {
      return 0;
    }

    @Override
    public boolean rollbackOnly() {
      return false;
    }

    @Override
    public boolean traceSQL() {
      return true;
    }
  }
}