package dz.ngnex.entity;

import dz.ngnex.bean.Progress;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class SimpleUnitTests {

  @Test
  public void equalsVerification() {
    EqualsVerifier.forClasses(
        Progress.class,
        SeasonStats.class,
        TemplateInfo.class,
        Snippet.class,
        EntityReference.class
    ).suppress(Warning.ALL_FIELDS_SHOULD_BE_USED).verify();
  }
}
