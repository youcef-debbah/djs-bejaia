package dz.ngnex.entity;

import java.util.regex.Pattern;

public final class Constrains {
  public static final int Min_IDENTIFIER_SIZE = 2;
  public static final int MAX_IDENTIFIER_SIZE = 80;
  public static final int MAX_PHONE_NUMBER_SIZE = 20;
  public static final int MAX_PHRASE_LENGTH = 250;
  public static final int MAX_TEXT_LENGTH = 32_000;
  public static final int MAX_URL_LENGTH = 2000;
  public static final String MAX_BUDGET = "20000000";
  public static final int MAX_FILENAME_LENGTH = 80;
  public static final int MAX_DATE_LENGTH = 10;

  private static final String FRENCH_ACCENTS = "\u00e7\u00e9\u00e2\u00ea\u00ee\u00f4\u00fb\u00e0\u00e8\u00f9\u00eb\u00ef\u00fc\u00c7\u00c9\u00c2\u00ca\u00ce\u00d4\u00db\u00c0\u00c8\u00d9\u00cb\u00cf\u00dc";
  private static final String ALPHANUMERIC_CHAR = "(?:\\w|\\d|[" + FRENCH_ACCENTS + "])";
  public static final Pattern IDENTIFIER_PATTERN = Pattern.compile(ALPHANUMERIC_CHAR +
      "(?:\\w|\\d|[ _-" + FRENCH_ACCENTS + "])*" + ALPHANUMERIC_CHAR);

  private Constrains() throws IllegalAccessException {
    throw new IllegalAccessException();
  }
}
