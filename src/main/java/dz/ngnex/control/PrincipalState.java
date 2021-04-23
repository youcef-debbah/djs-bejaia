package dz.ngnex.control;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@SessionScoped
public class PrincipalState implements Serializable {
  private static final long serialVersionUID = 6310935500959471740L;

  private final Set<Integer> likedComments = new ConcurrentSkipListSet<>();

  public Set<Integer> getLikedComments() {
    return likedComments;
  }
}
