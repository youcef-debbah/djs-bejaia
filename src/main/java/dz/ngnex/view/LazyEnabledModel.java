package dz.ngnex.view;

import dz.ngnex.util.Synchronized;

import java.io.Serializable;

import static dz.ngnex.util.Synchronized.*;

abstract class LazyEnabledModel implements Serializable {
  private static final long serialVersionUID = -6196217193057067831L;

  private boolean enabled;

  @Synchronized(By.VOLATILE_ACCESS)
  public boolean isEnabled() {
    return enabled;
  }

  @Synchronized(By.VOLATILE_ACCESS)
  public void enable() {
    enabled = true;
  }
}
