package dz.ngnex.security;

import org.jetbrains.annotations.NotNull;

public interface ReadableResource {

  @NotNull
  ReadAccess getAccess();
}
