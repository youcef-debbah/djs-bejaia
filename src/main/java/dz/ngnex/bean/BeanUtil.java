package dz.ngnex.bean;

import dz.ngnex.util.Check;
import org.apache.logging.log4j.Logger;

import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class BeanUtil {

  private static final String DYNAMIC_COMMENT_MARKER = "#log-this-statement-as-the-comment:";
  private static final String CURRENT_BENCHMARK = "dz.ngnex.bean.BeanUtil#CURRENT_BENCHMARK";

  private BeanUtil() throws IllegalAccessException {
    throw new IllegalAccessException();
  }

  public static long getCountResult(TypedQuery<Long> namedQuery) {
    Long result = namedQuery.getSingleResult();
    if (result == null)
      return 0;
    else
      return Math.abs(result);
  }

  public static Object benchmarkCall(Logger log, InvocationContext ctx) throws Exception {
    Map<String, Object> context = ctx.getContextData();
    Method method = ctx.getMethod();
    if (method == null || context.containsKey(CURRENT_BENCHMARK))
      return ctx.proceed();
    else {
      context.put(CURRENT_BENCHMARK, method);
      long t0 = System.nanoTime();
      try {
        return ctx.proceed();
      } finally {
        long t = System.nanoTime();
        context.remove(CURRENT_BENCHMARK);
        log.debug("@EJB " + method.getName() + " done in: " + TimeUnit.NANOSECONDS.toMillis(t - t0) + " ms");
      }
    }
  }

  public static void clearCache(EntityManager em) {
    em.clear();
    logAsSqlComment(em, "Context cleared");
  }

  public static void logAsSqlComment(EntityManager em, String comment) {
    Check.argNotNull(em, comment);
    em.createNativeQuery("select '" + DYNAMIC_COMMENT_MARKER + comment + "';").getSingleResult();
  }

  public static int toInt(Long number) {
    if (number == null || number == 0)
      return 0;
    else if (Integer.MIN_VALUE <= number && number <= Integer.MAX_VALUE)
      return number.intValue();
    else
      return number > 0 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
  }

  public static Integer toInteger(Long number) {
    if (number == null)
      return null;
    else if (Integer.MIN_VALUE <= number && number <= Integer.MAX_VALUE)
      return number.intValue();
    else
      return number > 0 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
  }
}
