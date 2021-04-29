package dz.ngnex.control;

import dz.ngnex.util.Config;
import dz.ngnex.util.Messages;
import dz.ngnex.util.ResourcesProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import java.util.List;

public class GlobalNotificationsListener implements PhaseListener {
  private static final long serialVersionUID = -77027226431960441L;

  private static Logger log = LogManager.getLogger(GlobalNotificationsListener.class);

  @Override
  public void afterPhase(PhaseEvent event) {

  }

  @Override
  public void beforePhase(PhaseEvent event) {
    FacesContext facesContext = event.getFacesContext();
    if (facesContext.isValidationFailed()) {
      log.error("validation failed: " + formatMessages(facesContext.getMessageList()));

      Messages messages = ResourcesProvider.getMessagesBundle();
      FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR,
          messages.get("validationFailed"),
          messages.get("recheckInput"));
      facesContext.addMessage(Config.GLOBAL_MSG, facesMessage);
    }
  }

  private String formatMessages(List<FacesMessage> messageList) {
    if (messageList == null || messageList.isEmpty())
      return "";

    StringBuilder result = new StringBuilder();

    for (FacesMessage message : messageList)
      result.append('\n')
          .append(message.getSeverity())
          .append(" (")
          .append(message.isRendered() ? "rendered" : "not rendered")
          .append(") ")
          .append(message.getSummary())
          .append(" - ")
          .append(message.getDetail());

    return result.toString();
  }

  @Override
  public PhaseId getPhaseId() {
    return PhaseId.RENDER_RESPONSE;
  }
}
