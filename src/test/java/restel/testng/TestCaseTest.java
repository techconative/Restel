package restel.testng;

import com.techconative.restel.core.model.RestelTestScenario;
import com.techconative.restel.testng.TestCase;
import com.techconative.restel.testng.TestCaseExecutor;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class TestCaseTest {

  @Mock private TestCaseExecutor executor;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testExecuteTest() {
    RestelTestScenario group = Mockito.mock(RestelTestScenario.class);
    Mockito.doReturn(group).when(executor).getExecutionGroup();
    Mockito.doReturn(List.of("name")).when(group).getTestApis();
    Mockito.doReturn(Boolean.TRUE).when(executor).executeTest();

    Assert.assertTrue(executor.executeTest());
    TestCase testCase = new TestCase("sample", executor);
    testCase.executeTest("sample");
  }
}
