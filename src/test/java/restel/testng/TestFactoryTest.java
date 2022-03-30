package restel.testng;

import com.techconative.restel.testng.TestCase;
import com.techconative.restel.testng.TestCaseExecutor;
import com.techconative.restel.testng.TestFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.BeanFactory;
import org.testng.Assert;

@RunWith(MockitoJUnitRunner.class)
public class TestFactoryTest {
  @InjectMocks private TestFactory testFactory;

  @Mock private BeanFactory beanFactory;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewInstanceNullParam() {
    testFactory.newInstance(null);
  }

  @Test()
  public void testNewInstance() {
    TestCaseExecutor exec = Mockito.mock(TestCaseExecutor.class);

    Mockito.doReturn(exec).when(beanFactory).getBean(TestCaseExecutor.class, "sample");
    Assert.assertTrue(testFactory.newInstance(null, "sample") instanceof TestCase);
  }
}
