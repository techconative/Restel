package restel.core.parser.dto;

import com.techconative.restel.core.parser.dto.TestSuites;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class TestSuitesTest {

  @Test
  public void testTestSuites() {
    TestSuites suites = new TestSuites();

    suites.setDependsOn("depends");
    Assert.assertEquals("depends", suites.getDependsOn());

    suites.setSuiteDescription("des");
    Assert.assertEquals("des", suites.getSuiteDescription());

    suites.setSuiteParams("params");
    Assert.assertEquals("params", suites.getSuiteParams());

    suites.setSuiteUniqueName("name");
    Assert.assertEquals("name", suites.getSuiteUniqueName());

    suites.setSuiteScenariosList(List.of("delete_validation"));
    Assert.assertEquals(List.of("delete_validation"), suites.getSuiteScenariosList());

    suites.setSuiteEnable(true);
    Assert.assertTrue(suites.getSuiteEnable());

    Assert.assertNotEquals(new TestSuites(), suites);
    Assert.assertNotEquals(suites.hashCode(), new TestSuites().hashCode());
    Assert.assertNotNull(suites.toString());
  }
}
