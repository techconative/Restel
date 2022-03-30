package restel.core.model.comparators;

import com.techconative.restel.core.http.RESTRequest;
import com.techconative.restel.core.http.RESTResponse;
import com.techconative.restel.core.model.comparator.NoOPMatcher;
import org.junit.Assert;
import org.junit.Test;

public class NoOPMatcherTest {
  @Test
  public void testCompare() {
    NoOPMatcher matcher = new NoOPMatcher();
    Assert.assertTrue(matcher.compare(new RESTResponse(), new RESTRequest()));
  }
}
