package dz.ngnex.testkit;

import dz.ngnex.bean.BeanUtil;
import dz.ngnex.util.InjectableByTests;
import dz.ngnex.util.TestWithTransaction;
import org.slf4j.Logger;

import javax.annotation.Priority;
import javax.ejb.ApplicationException;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

@InjectableByTests
@Interceptor
@TestWithTransaction
@Priority(Interceptor.Priority.APPLICATION)
@Dependent
public class TransactionInterceptor implements Serializable {

  private static final long serialVersionUID = -7481086833710796902L;

  @Inject
  Logger log;

  @Inject
  UserTransaction tx;

  @Inject
  EntityManager em;

  @AroundInvoke
  public Object manageTransaction(InvocationContext ctx) throws Exception {
    if (log.isTraceEnabled())
      log.trace("transaction interceptor call: method => " + ctx.getMethod()
          + " parameters => " + Arrays.toString(ctx.getParameters())
          + " context data =>" + ctx.getContextData());

    if (tx.getStatus() == Status.STATUS_NO_TRANSACTION) {
      return runMethodInsideNewTransaction(ctx, ctx.getMethod());
    } else
      return ctx.proceed();
  }

  private Object runMethodInsideNewTransaction(InvocationContext ctx, Method method) throws Exception {
    String methodName = method.getDeclaringClass().getSimpleName() + '#' + method.getName() + "()";
    tx.begin();
    try {
      log.debug("transaction started for: " + methodName);
      boolean traceSQL = applyAnnotationConfigurations(method);
      if (traceSQL)
        BeanUtil.logAsSqlComment(em, "start of: " + methodName);
      Object result = ctx.proceed();
      if (traceSQL)
        BeanUtil.logAsSqlComment(em, "  end of: " + methodName);
      tx.commit();
      log.debug("transaction committed for: " + methodName);
      return result;
    } catch (Exception invocationEx) {
      if (exceptionShouldCauseRollback(invocationEx))
        try {
          tx.rollback();
          log.info("transaction rolled back for: " + methodName);
        } catch (Exception rollbackEx) {
          log.error("could not rollback transaction for: " + methodName, rollbackEx);
        }
      else
        log.debug("exception thrown during transaction triggered a rollback: " + invocationEx.getClass().getName());

      throw invocationEx;
    }
  }

  private boolean applyAnnotationConfigurations(Method method) throws SystemException {
    TestWithTransaction testWithTransaction = getAnnotationInstance(method);
    int timeout = testWithTransaction.timeout();
    if (timeout != 0)
      tx.setTransactionTimeout(timeout);

    if (testWithTransaction.rollbackOnly())
      tx.setRollbackOnly();

    return testWithTransaction.traceSQL();
  }

  private TestWithTransaction getAnnotationInstance(Method method) {
    TestWithTransaction methodAnnotation = method.getAnnotation(TestWithTransaction.class);
    if (methodAnnotation != null)
      return methodAnnotation;
    else {
      TestWithTransaction classAnnotation = method.getDeclaringClass().getAnnotation(TestWithTransaction.class);
      if (classAnnotation != null)
        return classAnnotation;
      else
        return TestWithTransaction.Literal.INSTANCE;
    }
  }

  private boolean exceptionShouldCauseRollback(Exception exception) {
    if (exception instanceof RuntimeException)
      return true;

    Class<? extends Exception> exceptionClass = exception.getClass();
    ApplicationException applicationException = exceptionClass.getAnnotation(ApplicationException.class);
    if (applicationException != null)
      return applicationException.rollback();

    Class<?> exceptionSuperclass = exceptionClass.getSuperclass();
    do {
      ApplicationException applicationSuperException = exceptionSuperclass.getAnnotation(ApplicationException.class);
      if (applicationSuperException != null && applicationSuperException.inherited())
        return applicationSuperException.rollback();

      exceptionSuperclass = exceptionSuperclass.getSuperclass();
    } while (!exceptionSuperclass.equals(Object.class));

    return false;
  }

}