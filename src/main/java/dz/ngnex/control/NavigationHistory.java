package dz.ngnex.control;

import dz.ngnex.util.InjectableByTests;
import dz.ngnex.util.LoggerProvider;
import dz.ngnex.util.WebKit;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.event.PhaseEvent;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SessionScoped
@InjectableByTests
public class NavigationHistory implements Serializable {
  private static final long serialVersionUID = -1289123030838064939L;

  private final Logger log = LoggerProvider.getLogger(NavigationHistory.class);

  private final List<String> EMPTY_HISTORY = Arrays.asList("", "", "", "");
  private final ArrayDeque<String> urlHistory = new ArrayDeque<>(EMPTY_HISTORY.size());

  @PostConstruct
  private void init() {
    urlHistory.addAll(EMPTY_HISTORY);
  }

  public void refresh(PhaseEvent event) {
    ExternalContext context = event.getFacesContext().getExternalContext();
    String path = context.getRequestServletPath();
    Map<String, String[]> parameterValues = context.getRequestParameterValuesMap();
    try {
      urlHistory.pollLast();
      urlHistory.offerFirst(WebKit.getURL(path, null, parameterValues, StandardCharsets.UTF_8));
    } catch (Exception e) {
      log.error("could not save last for '" + path + "' with params " + parameterValues, e);
    }
  }

  public String getLastUrl() {
    return urlHistory.peekFirst();
  }

  @Override
  public String toString() {
    return "NavigationHistory" + Arrays.toString(urlHistory.toArray());
  }
}
