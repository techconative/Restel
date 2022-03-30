package restel.core.model;

import com.techconative.restel.core.model.RestelSuite;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class RestelSuiteTest {
  @Test
  public void testRestelSuite() {
    RestelSuite suite = new RestelSuite();
    suite.setDependsOn(null);
    suite.setSuiteName("name");
    suite.setSuiteDescription("desc");
    suite.setSuiteParams(Map.of("k", "v"));

    Assert.assertNull(suite.getDependsOn());
    Assert.assertEquals("desc", suite.getSuiteDescription());
    Assert.assertEquals(Map.of("k", "v"), suite.getSuiteParams());
    Assert.assertEquals("name", suite.getSuiteName());

    Assert.assertNotEquals(new RestelSuite(), suite);
    Assert.assertNotEquals(suite.hashCode(), new RestelSuite().hashCode());
    Assert.assertNotNull(suite.toString());
  }
}
