package dz.ngnex.bean;

import dz.ngnex.util.Check;

import javax.persistence.EntityGraph;
import java.util.HashMap;
import java.util.Map;

public class Hints {

  public static final String FETCH_GRAPH = "javax.persistence.fetchgraph";

  private Hints() throws InstantiationException {
    throw new InstantiationException();
  }

  public static Map<String, Object> fetchGraph(EntityGraph<?> graph) {
    Map<String, Object> hints = new HashMap<>();
    hints.put(FETCH_GRAPH, Check.argNotNull(graph));
    return hints;
  }
}
