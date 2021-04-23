package dz.ngnex.control;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.inject.Inject;

public class NavigationListener implements PhaseListener {
  private static final long serialVersionUID = -4006018392464635329L;

  @Inject
  private NavigationHistory navigationHistory;

  @Override
  public void beforePhase(PhaseEvent event) {
    navigationHistory.refresh(event);
  }

  @Override
  public void afterPhase(PhaseEvent event) {
  }

  @Override
  public PhaseId getPhaseId() {
    return PhaseId.RENDER_RESPONSE;
  }
}
