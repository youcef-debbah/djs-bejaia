package dz.ngnex.entity;

public enum ProcessingState {
  NOT_STARTED,
  ON_PROGRESS,
  DONE_PROCESSING;

  private static final ProcessingState[] SELECTABLE_VALUES = {ON_PROGRESS, DONE_PROCESSING};

  public static ProcessingState[] selectableValues() {
    return SELECTABLE_VALUES;
  }

}
