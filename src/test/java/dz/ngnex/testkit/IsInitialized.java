package dz.ngnex.testkit;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hibernate.Hibernate;

import java.util.Collection;
import java.util.Map;

public class IsInitialized extends BaseMatcher<Object> {

  @Override
  public boolean matches(Object item) {
    return Hibernate.isInitialized(item);
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("an initialized Hibernate entity or collection");
  }

  @Override
  public void describeMismatch(Object item, Description description) {
    if (item instanceof Collection)
      description.appendText("was an uninitialized collection proxy");
    else if (item instanceof Map)
      description.appendText("was an uninitialized map proxy");
    else
      description.appendText("was an uninitialized entity proxy");
  }

  public static Matcher<Object> initialized() {
    return new IsInitialized();
  }
}
