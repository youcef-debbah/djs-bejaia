package dz.ngnex.view;

import dz.ngnex.util.ViewModel;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

@ViewModel
public class ViewSetting implements Serializable {
  private static final long serialVersionUID = -3273936338433834287L;

  private final AtomicReference<String> redirectQuery = new AtomicReference<>();

  private final AtomicReference<String> nextOutcome = new AtomicReference<>();

  public void setRedirectQuery(String redirectQuery) {
    this.redirectQuery.set(redirectQuery);
  }

  public String getRedirectQuery() {
    return redirectQuery.get();
  }

  public void setNextOutcome(String nextOutcome) {
    this.nextOutcome.set(nextOutcome);
  }

  public String getNextOutcome() {
    return nextOutcome.get();
  }

  @Override
  public String toString() {
    return "ViewSetting{" +
        "redirectQuery=" + getRedirectQuery() +
        ", nextOutcome=" + getNextOutcome() +
        '}';
  }
}
