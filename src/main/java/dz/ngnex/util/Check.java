package dz.ngnex.util;

public final class Check {

  private Check() throws InstantiationException {
    throw new InstantiationException();
  }

  public static <T> T argNotNull(T argument) {
    if (argument == null)
      throw new IllegalArgumentException("null is an illegal argument");

    return argument;
  }

  public static void argNotNull(Object... arguments) {
    for (Object argument : arguments)
      if (argument == null)
        throw new IllegalArgumentException("null is an illegal argument");
  }
}
