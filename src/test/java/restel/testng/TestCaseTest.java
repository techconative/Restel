package restel.testng;

import com.pramati.restel.core.model.RestelExecutionGroup;
import com.pramati.restel.testng.TestCase;
import com.pramati.restel.testng.TestCaseExecutor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class TestCaseTest {

    @Mock
    private TestCaseExecutor executor;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExecuteTest() {
        RestelExecutionGroup group = Mockito.mock(RestelExecutionGroup.class);
        Mockito.doReturn(group).when(executor).getExecutionGroup();
        Mockito.doReturn("name").when(group).getTestDefinitionName();
        Mockito.doReturn(Boolean.TRUE).when(executor).executeTest();

        Assert.assertTrue(executor.executeTest());
        TestCase testCase = new TestCase("sample", executor);
        testCase.executeTest("sample");

    }
}
