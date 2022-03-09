package restel.testng;

import com.techconative.restel.core.managers.ContextManager;
import com.techconative.restel.core.managers.RequestManager;
import com.techconative.restel.core.managers.RestelDefinitionManager;
import com.techconative.restel.core.managers.RestelTestManager;
import com.techconative.restel.core.model.BaseConfiguration;
import com.techconative.restel.core.model.RestelExecutionGroup;
import com.techconative.restel.core.model.RestelSuite;
import com.techconative.restel.core.model.RestelTestMethod;
import com.techconative.restel.testng.MatcherFactory;
import com.techconative.restel.testng.TestCaseExecutor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.Assert;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TestCaseExecutor.class})
public class TestCaseExecutorTest {

    @InjectMocks
    private TestCaseExecutor executor;

    @Mock
    private RequestManager requestManager;

    @Mock
    private RestelTestManager testManager;

    @Mock
    private ContextManager contextManager;

    @Mock
    private MatcherFactory matcherFactory;

    private RestelExecutionGroup executionGroup = Mockito.mock(RestelExecutionGroup.class);
    private RestelTestMethod testMethod = Mockito.mock(RestelTestMethod.class);
    private RestelSuite restelSuite = Mockito.mock(RestelSuite.class);
    private BaseConfiguration baseConfiguration = Mockito.mock(BaseConfiguration.class);

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetExecutionGroup() throws Exception {
        invokeInit("Sample", "Suite", "Url");
        Assert.assertEquals(executionGroup, executor.getExecutionGroup());
    }

    @Test
    public void testExecuteTest() throws Exception {
        invokeInit("Sample", "Suite", "Request");
        RestelDefinitionManager definitionManager = PowerMockito.mock(RestelDefinitionManager.class);
        PowerMockito.whenNew(RestelDefinitionManager.class).withAnyArguments().thenReturn(definitionManager);
        PowerMockito.doReturn(Boolean.FALSE).when(definitionManager).executeTest(Mockito.anyString(), Mockito.anyString());
        Assert.assertFalse(executor.executeTest());
    }

    private void invokeInit(String definitionName, String suiteName, String urlName) throws Exception {
        Mockito.when(executionGroup.getTestDefinitionName()).thenReturn(definitionName);
        Mockito.when(executionGroup.getTestSuiteName()).thenReturn(suiteName);
        Mockito.when(baseConfiguration.getBaseUrl()).thenReturn(urlName);
        Mockito.when(restelSuite.getSuiteParams()).thenReturn(new HashMap<>(Map.of("key", "value")));
        Mockito.when(executionGroup.getExecutionParams()).thenReturn(new HashMap<>());

        Mockito.doReturn(baseConfiguration).when(testManager).getBaseConfig();
        Mockito.doReturn(executionGroup).when(testManager).getExecutionDefinition(Mockito.any());
        Mockito.doReturn(testMethod).when(testManager).getTestDefinitions(Mockito.any());
        Mockito.doReturn(restelSuite).when(testManager).getTestSuite(Mockito.any());

        //call post-constructor
        Method postConstruct = TestCaseExecutor.class.getDeclaredMethod("init"); // methodName,parameters
        postConstruct.setAccessible(true);
        postConstruct.invoke(executor);
    }
}
