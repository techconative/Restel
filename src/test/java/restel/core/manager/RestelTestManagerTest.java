package restel.core.manager;

import com.pramati.restel.core.managers.ExcelParseManager;
import com.pramati.restel.core.managers.RestelTestManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RestelTestManagerTest {
  @InjectMocks private RestelTestManager testManager;

  @InjectMocks private ExcelParseManager excelParseManager;

  @Before
  public void initMocks()
      throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException,
          InvocationTargetException {
    MockitoAnnotations.initMocks(this);
    invoke("src/test/resources/Sample_Suite_definition.xlsx");
  }

  private void invoke(String filepath)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
          NoSuchFieldException {
    FieldSetter.setField(
        excelParseManager, excelParseManager.getClass().getDeclaredField("filepath"), filepath);
    Method postConstruct =
        ExcelParseManager.class.getDeclaredMethod("init"); // methodName,parameters
    postConstruct.setAccessible(true);
    postConstruct.invoke(excelParseManager);

    FieldSetter.setField(
        testManager,
        testManager.getClass().getDeclaredField("excelParseManager"),
        excelParseManager);
    Method construct = RestelTestManager.class.getDeclaredMethod("init"); // methodName,parameters
    construct.setAccessible(true);
    construct.invoke(testManager);
  }

  @Test
  public void testGetBaseConfig() {
    Assert.assertNotNull(testManager.getBaseConfig());
  }

  @Test
  public void testGetExecutionDefinitions() {
    Assert.assertNotNull(testManager.getScenarios());
  }

  @Test
  public void testGetTestDefinitions() {
    Assert.assertNotNull(testManager.getTestDefintions());
  }

  @Test
  public void testGetTestSuites() {
    Assert.assertNotNull(testManager.getTestSuites());
  }

  @Test
  public void testGetTestSuite() {
    testManager.getTestSuites().keySet().stream()
        .forEach(key -> Assert.assertNotNull(testManager.getTestSuite(key)));
  }

  @Test
  public void testGetTestDefinition() {
    testManager.getTestDefintions().stream()
        .forEach(
            key -> Assert.assertNotNull(testManager.getTestMethod(key.getCaseUniqueName())));
  }

  @Test
  public void testGetExecutionDefinition() {
    testManager.getScenarios().stream()
        .forEach(key -> Assert.assertNotNull(testManager.getScenario(key.getScenarioName())));
  }
}
