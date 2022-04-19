package restel.core.model;

import com.techconative.restel.core.model.RestelTestScenario;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class RestelExecutionGroupTest {
  @Test
  public void testRestelExecutionGroup() {
    RestelTestScenario exec = new RestelTestScenario();
    exec.setDependsOn(null);
    exec.setScenarioName("name");
    exec.setScenarioDescription("a sample scenario description");
    exec.setExecutionParams(Map.of("k", "v"));
    exec.setTestApis(List.of("def"));
    // remove this
    exec.setTestSuiteName("suite");

    Assert.assertNull(exec.getDependsOn());
    Assert.assertEquals(Map.of("k", "v"), exec.getExecutionParams());
    Assert.assertEquals("name", exec.getScenarioName());
    Assert.assertEquals("a sample scenario description", exec.getScenarioDescription());
    Assert.assertEquals(List.of("def"), exec.getTestApis());
    Assert.assertEquals("suite", exec.getTestSuiteName());

    Assert.assertNotEquals(new RestelTestScenario(), exec);
    Assert.assertNotEquals(exec.hashCode(), new RestelTestScenario().hashCode());
    Assert.assertNotNull(exec.toString());
  }
}
