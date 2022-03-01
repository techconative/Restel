package restel.core;

import com.pramati.restel.core.RestelRunner;
import com.pramati.restel.core.SuiteExecutor;
import com.pramati.restel.core.managers.RestelTestManager;
import com.pramati.restel.core.model.RestelExecutionGroup;
import com.pramati.restel.core.model.RestelSuite;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.testng.annotations.BeforeMethod;

import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class RestelRunnerTest {

    @InjectMocks
    private RestelRunner restelRunner;

    @Spy
    private RestelTestManager testManager;

    @Spy
    private SuiteExecutor suiteExecutor;


    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRun() {
        Mockito.doReturn(createSuites(true)).when(testManager).getTestSuites();
        Mockito.doReturn(getExecutionGroup(true)).when(testManager).getExecutionDefinitions();
        Mockito.doNothing().when(suiteExecutor).executeTest(Mockito.any());
        Assert.assertEquals(2, testManager.getTestSuites().size());
        Assert.assertEquals(2, testManager.getExecutionDefinitions().size());
        restelRunner.run();
    }


    @Test
    public void testRunWithOutExecDependency() {
        Mockito.doReturn(createSuites(true)).when(testManager).getTestSuites();
        Mockito.doReturn(getExecutionGroup(false)).when(testManager).getExecutionDefinitions();
        Mockito.doNothing().when(suiteExecutor).executeTest(Mockito.any());
        Assert.assertEquals(2, testManager.getTestSuites().size());
        Assert.assertEquals(1, testManager.getExecutionDefinitions().size());
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
        Mockito.doReturn(new ArrayList<>()).when(testManager).getExecutionDefinitions();
        Mockito.doNothing().when(suiteExecutor).executeTest(Mockito.any());
        Assert.assertEquals(1, testManager.getTestSuites().size());
        Assert.assertEquals(0, testManager.getExecutionDefinitions().size());
        restelRunner.run();
    }

    private List<RestelExecutionGroup> getExecutionGroup(boolean addDependency) {
        List<RestelExecutionGroup> groups = new ArrayList<>();
        RestelExecutionGroup childGroup = new RestelExecutionGroup();
        childGroup.setTestSuiteName("Sample-Child");
        childGroup.setTestExecutionEnable(Boolean.TRUE);
        childGroup.setTestDefinitionName("Test");
        childGroup.setExecutionParams(null);
        childGroup.setExecutionGroupName("child-exec");
        RestelExecutionGroup group = new RestelExecutionGroup();
        group.setTestSuiteName("Sample");
        group.setTestExecutionEnable(Boolean.TRUE);
        group.setTestDefinitionName("Test");
        group.setExecutionParams(null);
        group.setExecutionGroupName("exec");
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
