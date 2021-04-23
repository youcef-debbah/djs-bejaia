package dz.ngnex.meta;

import javax.ejb.Local;

@Local
public interface FakeEJB1 {

  void assertInjected();
}
