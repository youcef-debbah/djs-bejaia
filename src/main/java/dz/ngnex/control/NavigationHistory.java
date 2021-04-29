package dz.ngnex.control;

import dz.ngnex.util.InjectableByTests;
import dz.ngnex.util.LoggerProvider;
import dz.ngnex.util.WebKit;
import org.apache.logging.log4j.Logger;

import javax.enterprise.context.SessionScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

@SessionScoped
@InjectableByTests
public class NavigationHistory implements Serializable {

  public static final String LAST_URL_VISITED = "last-url-visited";

  private static final long serialVersionUID = -1289123030838064939L;
  private final Logger log = LoggerProvider.getLogger(NavigationHistory.class);
  private static final int MAX_ENTRIES = 4;

  private final Deque<String> urlHistory;

  public NavigationHistory() {
    Deque<String> history = new ConcurrentLinkedDeque<>();
    for (int i = 0; i < MAX_ENTRIES; i++) {
      history.add("");
    }
    this.urlHistory = history;
  }

  public static String getRelativeUrl(HttpServletRequest request) {
    String url = request.getServletPath();
    String query = request.getQueryString();
    if (query != null)
      url += "?" + query;
    return url;
  }

  public void refresh(HttpServletRequest req, HttpServletResponse res) {
    String url = getRelativeUrl(req);
    try {
      if (WebKit.isDeepUrl(url) && !Objects.equals(url, urlHistory.peekFirst())) {
        urlHistory.pollLast();
        urlHistory.offerFirst(url);
      }
    } catch (Exception e) {
      log.error("could not save url: '" + url, e);
    }
  }

  public String getLastUrl() {
    return urlHistory.peekFirst();
  }

  @Override
  public String toString() {
    return "NavigationHistory:\n" + String.join("\n", urlHistory);
  }
}
