package dz.ngnex.util;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Synchronized
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class SynchronizedInterceptor implements Serializable {

  private static final long serialVersionUID = 5149810538126546754L;

  private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

  private volatile int state = 0;

  @AroundInvoke
  public Object synchroniseAccess(InvocationContext ctx) throws Exception {
    Synchronized annotation = getAnnotationInstance(ctx.getMethod());
    switch (annotation.value()) {
      case READ_LOCK:
        return proceedWithLock(ctx, lock.readLock());
      case WRITE_LOCK:
        return proceedWithLock(ctx, lock.writeLock());
      case VOLATILE_ACCESS:
        return proceed(ctx);
    }

    return ctx.proceed();
  }

  private Synchronized getAnnotationInstance(Method method) {
    Synchronized methodAnnotation = method.getAnnotation(Synchronized.class);
    if (methodAnnotation != null)
      return methodAnnotation;
    else {
      Synchronized classAnnotation = method.getDeclaringClass().getAnnotation(Synchronized.class);
      if (classAnnotation != null)
        return classAnnotation;
      else
        return Synchronized.Literal.INSTANCE;
    }
  }

  private Object proceedWithLock(InvocationContext ctx, Lock lock) throws Exception {
    lock.lock();
    int state = this.state;
    try {
      return ctx.proceed();
    } finally {
      this.state = state;
      lock.unlock();
    }
  }

  private Object proceed(InvocationContext ctx) throws Exception {
    int state = this.state;
    try {
      return ctx.proceed();
    } finally {
      this.state = state;
    }
  }
}
