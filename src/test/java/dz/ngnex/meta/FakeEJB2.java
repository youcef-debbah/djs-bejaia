package dz.ngnex.meta;

import javax.ejb.Local;

@Local
public interface FakeEJB2 {

  void assertInjected();
}
