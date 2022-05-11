package restel.core.model.comparators;

import com.techconative.restel.extension.jsoncompare.comparator.StringJsonComparator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StringJsonMatchComparatorTest {

  private StringJsonComparator comparator;

  @Before
  public void setup() {
    comparator = new StringJsonComparator();
  }

  @Test
  public void testCompareFields() {
    Assert.assertTrue(
        comparator.compareFields(
            "([A-Z][A-Z0-9]*)\\b[^>]*>(.*?)</\\1", "([A-Z][A-Z0-9]*)\\b[^>]*>(.*?)</\\1"));
  }

  @Test
  public void testCompareValues() {
    Assert.assertTrue(
        comparator.compareValues(
            "([A-Z][A-Z0-9]*)\\b[^>]*>(.*?)</\\1", "([A-Z][A-Z0-9]*)\\b[^>]*>(.*?)</\\1"));
  }
}
