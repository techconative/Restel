package restel.core.model;

import com.techconative.restel.core.model.GlobalContext;
import com.techconative.restel.core.model.TestContext;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestContextTest {

  @ClassRule
  public static final EnvironmentVariables environmentVariables =
      new EnvironmentVariables().set("env", "val");

  @Before
  public void setUp() throws Exception {
    GlobalContext.getInstance().reset();
  }

  @Test
  public void testTestContext() {
    TestContext context = new TestContext("test-A");
    context.addValue("test-A", "Success");
    GlobalContext.getInstance().addValue("Global", "Global-Value");

    assertEquals("Global-Value", context.resolveValue("Global"));
    assertEquals("Success", context.resolveValue("test-A"));
    assertNull(context.resolveValue("Test"));
  }

  @Test
  public void testTestContextResolveProperty() {
    TestContext context = new TestContext("test-A");
    context.addValue("test-A", "Success");
    GlobalContext.getInstance().addValue("Global", "Global-Value");

    System.setProperty("key", "value");
    Object actual = context.resolveValue("key");
    assertEquals(actual, "value");
  }

  @Test
  public void testTestContextResolvePropertyNotOverridingParentContext() {
    TestContext context = new TestContext("test-A");
    context.addValue("test-A", "Success");
    GlobalContext.getInstance().addValue("key", "Global-Value");

    System.setProperty("key", "value");
    Object actual = context.resolveValue("key");
    assertEquals(actual, "Global-Value");
  }

  @Test
  public void testTestContextResolveEnvNotOverridingParentContext() {
    TestContext context = new TestContext("test-A");
    context.addValue("test-A", "Success");
    GlobalContext.getInstance().addValue("env", "Global-Value");

    Object actual = context.resolveValue("env");
    assertEquals(actual, "Global-Value");
  }

  @Test
  public void testTestContextResolveEnv() {
    TestContext context = new TestContext("test-A");
    context.addValue("test-A", "Success");
    GlobalContext.getInstance();

    Object actual = context.resolveValue("env");
    assertEquals(actual, "val");
  }
}
