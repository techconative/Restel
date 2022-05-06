package restel.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.techconative.restel.core.model.GlobalContext;
import com.techconative.restel.core.model.TestContext;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import java.util.List;
import java.util.Map;

public class TestContextTest {

  @ClassRule
  public static final EnvironmentVariables environmentVariables =
      new EnvironmentVariables().set("env", "val");

  @Before
  public void setUp() throws Exception {
    GlobalContext.getInstance().reset();
  }

  @Test
  public void testResolveAddedValue() {
    TestContext context = new TestContext("test-A");
    context.addValue("test-A", "Success");
    GlobalContext.getInstance().addValue("Global", "Global-Value");

    assertEquals("Global-Value", context.resolveValue("Global"));
    assertEquals("Success", context.resolveValue("test-A"));
    assertNull(context.resolveValue("Test"));
  }

  @Test
  public void testResolveProperty() {
    TestContext context = new TestContext("test-A");
    context.addValue("test-A", "Success");
    GlobalContext.getInstance().addValue("Global", "Global-Value");

    System.setProperty("key", "value");
    Object actual = context.resolveValue("key");
    assertEquals(actual, "value");
  }

  @Test
  public void testResolvePropertyNotOverridingParentContext() {
    TestContext context = new TestContext("test-A");
    context.addValue("test-A", "Success");
    GlobalContext.getInstance().addValue("key", "Global-Value");

    System.setProperty("key", "value");
    Object actual = context.resolveValue("key");
    assertEquals(actual, "Global-Value");
  }

  @Test
  public void testResolveEnvNotOverridingParentContext() {
    TestContext context = new TestContext("test-A");
    context.addValue("test-A", "Success");
    GlobalContext.getInstance().addValue("env", "Global-Value");

    Object actual = context.resolveValue("env");
    assertEquals(actual, "Global-Value");
  }

  @Test
  public void testResolveEnv() {
    TestContext context = new TestContext("test-A");
    context.addValue("test-A", "Success");

    Object actual = context.resolveValue("env");
    assertEquals(actual, "val");
  }

  @Test
  public void testNSResolveFromAddedMap() {
    TestContext context = new TestContext("test-A");
    context.addValue("test-A", Map.of("sub-key", "value"));
    GlobalContext.getInstance();
    Object actual = context.resolveValue("test-A.sub-key");
    assertEquals(actual, "value");
  }

  @Test
  public void testNSResolveFromAddedMapAtGlobalContext() {
    TestContext context = new TestContext("test-A");
    GlobalContext.getInstance().addValue("test-A", Map.of("sub-key", "value"));
    Object actual = context.resolveValue("test-A.sub-key");
    assertEquals(actual, "value");
  }

  @Test
  public void testNSResolveFromChildContext() {
    TestContext context = new TestContext("test-A");
    context.addValue("sub-key", "value");
    Object actual = GlobalContext.getInstance().resolveValue("test-A.sub-key");
    assertEquals(actual, "value");
  }

  @Test
  public void testNSResolveFromChildContextNestedMultiLevel() {

    TestContext child1 = new TestContext("child1");
    TestContext child2 = new TestContext("child2", child1);
    TestContext child3 = new TestContext("child3", child2);
    child3.addValue("key", "value");

    Object actual = GlobalContext.getInstance().resolveValue("child1.child2.child3.key");
    assertEquals(actual, "value");
  }

  @Test
  public void testNSResolveFromChildContextNestedMultiLevelResolvedFromChild() {

    TestContext child1 = new TestContext("child1");
    TestContext child2 = new TestContext("child2", child1);
    TestContext child3 = new TestContext("child3", child2);
    child3.addValue("key", "value");

    // This happens because the child tries to ask the parent context when the key is not available.
    // Eventually invoking GlobalContext::resolveValue("child1.child2.child3.key")
    Object actual = child2.resolveValue("child1.child2.child3.key");
    assertEquals(actual, "value");
  }

  @Test
  public void testNSResolveFromChildContextNestedMultiLevelResolvedFromMapInChild() {

    TestContext child1 = new TestContext("child1");
    TestContext child2 = new TestContext("child2", child1);
    TestContext child3 = new TestContext("child3", child2);
    child3.addValue("key", Map.of("sub-key","value"));


    Object actual = child2.resolveValue("child1.child2.child3.key.sub-key");
    assertEquals(actual, "value");
  }

  @Test
  public void testNSResolveFromChildContextNestedMultiLevelResolvedFromListInChild() {

    TestContext child1 = new TestContext("child1");
    TestContext child2 = new TestContext("child2", child1);
    TestContext child3 = new TestContext("child3", child2);
    child3.addValue("key", List.of("value1","value2"));


    Object actual = child2.resolveValue("child1.child2.child3.key[0]");
    assertEquals(actual, "value1");
    actual = child2.resolveValue("child1.child2.child3.key[1]");
    assertEquals(actual, "value2");
  }
}
