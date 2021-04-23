package dz.ngnex.security;

import javax.validation.constraints.NotNull;

public interface WritableResource {

  @NotNull
  WriteAccess getAccess();
}
