package dz.ngnex.control;

import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.concurrent.atomic.AtomicInteger;

public class CurrentVisitorsCounter implements HttpSessionListener {

  private static final AtomicInteger currentVisitors = new AtomicInteger(0);

  @Push
  @Inject
  private PushContext currentVisitorsNotifications;

  // Public constructor is required by servlet spec
  public CurrentVisitorsCounter() {
  }

  // -------------------------------------------------------
  // HttpSessionListener implementation
  // -------------------------------------------------------
  @Override
  public void sessionCreated(HttpSessionEvent se) {
    int currentVisitorsCount = currentVisitors.incrementAndGet();
    currentVisitorsNotifications.send(currentVisitorsCount);
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent se) {
    int currentVisitorsCount = currentVisitors.decrementAndGet();
    currentVisitorsNotifications.send(currentVisitorsCount);
  }

  public static int getCurrentVisitors() {
    return currentVisitors.get();
  }
}
