package restel.core.parser.dto;

import com.pramati.restel.core.parser.dto.TestScenarios;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class TestSuiteExecutionTest {
  @Test
  public void testTestSuoteexecution() {
    TestScenarios suiteExecution = new TestScenarios();

    suiteExecution.setDependsOn("depend");
    Assert.assertEquals("depend", suiteExecution.getDependsOn());

    suiteExecution.setTestCases(List.of("test"));
    Assert.assertEquals(List.of("test"), suiteExecution.getTestCases());

    suiteExecution.setScenarioEnabled(false);
    Assert.assertFalse(suiteExecution.getScenarioEnabled());

    suiteExecution.setScenarioEnabled(Boolean.TRUE);
    Assert.assertTrue(suiteExecution.getScenarioEnabled());

    suiteExecution.setScenarioParams("param");
    Assert.assertEquals("param", suiteExecution.getScenarioParams());

    suiteExecution.setTestSuite("suite");
    Assert.assertEquals("suite", suiteExecution.getTestSuite());

    suiteExecution.setScenarioUniqueName("name");
    Assert.assertEquals("name", suiteExecution.getScenarioUniqueName());

    Assert.assertNotEquals(new TestScenarios(), suiteExecution);
    Assert.assertNotEquals(suiteExecution.hashCode(), new TestScenarios().hashCode());
    Assert.assertNotNull(suiteExecution.toString());
  }
}
