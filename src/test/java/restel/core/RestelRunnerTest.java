package restel.core;

import com.pramati.restel.core.RestelRunner;
import com.pramati.restel.core.SuiteExecutor;
import com.pramati.restel.core.managers.RestelTestManager;
import com.pramati.restel.core.model.RestelSuite;
import com.pramati.restel.core.model.RestelTestScenario;
import java.util.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.testng.annotations.BeforeMethod;

@RunWith(MockitoJUnitRunner.class)
public class RestelRunnerTest {

  @InjectMocks private RestelRunner restelRunner;

  @Spy private RestelTestManager testManager;

  @Spy private SuiteExecutor suiteExecutor;

  @BeforeMethod
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testRun() {
    Mockito.doReturn(createSuites(true)).when(testManager).getTestSuites();
    Mockito.doReturn(getExecutionGroup(true)).when(testManager).getScenarios();
    Mockito.doNothing().when(suiteExecutor).executeTest(Mockito.any());
    Assert.assertEquals(2, testManager.getTestSuites().size());
    Assert.assertEquals(2, testManager.getScenarios().size());
    restelRunner.run();
  }

  @Test
  public void testRunWithOutExecDependency() {
    Mockito.doReturn(createSuites(true)).when(testManager).getTestSuites();
    Mockito.doReturn(getExecutionGroup(false)).when(testManager).getScenarios();
    Mockito.doNothing().when(suiteExecutor).executeTest(Mockito.any());
    Assert.assertEquals(2, testManager.getTestSuites().size());
    Assert.assertEquals(1, testManager.getScenarios().size());
    restelRunner.run();
  }

  @Test
  public void testRunEmptySuites() {
    Mockito.doReturn(new HashMap<>()).when(testManager).getTestSuites();
    Mockito.doNothing().when(suiteExecutor).executeTest(Mockito.any());
    restelRunner.run();
  }

  @Test
  public void testRunEmptyExecutions() {
    Mockito.doReturn(createSuites(false)).when(testManager).getTestSuites();
    Mockito.doReturn(new ArrayList<>()).when(testManager).getScenarios();
    Mockito.doNothing().when(suiteExecutor).executeTest(Mockito.any());
    Assert.assertEquals(1, testManager.getTestSuites().size());
    Assert.assertEquals(0, testManager.getScenarios().size());
    restelRunner.run();
  }

  private List<RestelTestScenario> getExecutionGroup(boolean addDependency) {
    List<RestelTestScenario> groups = new ArrayList<>();
    RestelTestScenario childGroup = new RestelTestScenario();
    childGroup.setTestSuiteName("Sample-Child");
    childGroup.setScenarioEnabled(Boolean.TRUE);
    childGroup.setTestDefinitionName("Test");
    childGroup.setExecutionParams(null);
    childGroup.setScenarioName("child-exec");
    RestelTestScenario group = new RestelTestScenario();
    group.setTestSuiteName("Sample");
    group.setScenarioEnabled(Boolean.TRUE);
    group.setTestDefinitionName("Test");
    group.setExecutionParams(null);
    group.setScenarioName("exec");
    if (addDependency) {
      group.setDependsOn(new ArrayList<>(Collections.singletonList(childGroup)));
      groups.add(childGroup);
    }
    groups.add(group);

    return groups;
  }

  private Map<String, RestelSuite> createSuites(boolean addDependency) {
    Map<String, RestelSuite> suites = new HashMap<>();
    RestelSuite childSuite = new RestelSuite();
    childSuite.setSuiteName("Sample-Child");
    childSuite.setSuiteDescription("desc");
    childSuite.setSuiteEnable(Boolean.TRUE);
    childSuite.setSuiteParams(null);
    childSuite.setDependsOn(null);

    RestelSuite suite = new RestelSuite();
    suite.setSuiteName("Sample");
    suite.setSuiteDescription("desc");
    suite.setSuiteEnable(Boolean.TRUE);
    suite.setSuiteParams(null);
    if (addDependency) {
      suite.setDependsOn(new ArrayList<>(Collections.singletonList(childSuite)));
      suites.put("Sample-Child", childSuite);
    }
    suites.put("Sample", suite);
    return suites;
  }
}
