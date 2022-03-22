package restel.core.manager;

import com.pramati.restel.core.managers.ContextManager;
import com.pramati.restel.core.model.TestContext;
import java.util.*;
import org.junit.Assert;
import org.junit.Test;

public class ContextManagerTest {

  @Test
  public void testReplaceContextVariablesForMapObject() {
    ContextManager context = new ContextManager();
    context.setValue("key", "value");
    Object a = Map.of("k1", "${key}");
    // In the above context, this prints {k1=value}
    Map<String, String> reaplcedMap = (Map) context.replaceContextVariables(null, a);
    Assert.assertEquals("value", reaplcedMap.get("k1"));
  }

  @Test
  public void testReplaceContextVariablesForArray() {
    ContextManager context = new ContextManager();
    TestContext testContext = new TestContext("Sample");
    testContext.addValue("key", "1");
    testContext.addValue("key1", "value");

    // In the above context, this will return a list [value]
    // Like ${key${key}} (resolving inner ${key})--> ${key1} resolving key1
    // -> value
    Assert.assertEquals(
        Arrays.asList("value"),
        context.replaceContextVariables(testContext, Arrays.asList("${key${key}}")));
  }

  @Test
  public void testReplaceContextVariablesWithNestedKey() {
    ContextManager context = new ContextManager();

    TestContext testContext = new TestContext("Sample");
    testContext.addValue("key", "1");
    testContext.addValue("key1", "value");

    Assert.assertEquals(
        Arrays.asList("value"),
        context.replaceContextVariables(testContext, (Object) Arrays.asList("${key${key}}")));
  }

  @Test
  public void testReplaceContextVariablesWithCollection() {
    ContextManager context = new ContextManager();

    Map<String, Object> data = new HashMap<>();
    data.put("resp", new HashMap<>(Map.of("k", new ArrayList<>(Arrays.asList(1)))));
    TestContext testContext = new TestContext("Sample");
    testContext.addValue("key", "1");
    testContext.addValue("key1", new ArrayList<>(Arrays.asList("value")));
    testContext.addValue("result", data);

    Assert.assertEquals(
        Arrays.asList("value"),
        context.replaceContextVariables(testContext, "${key${result.resp.k[0]}}"));
  }

  @Test
  public void testReplaceContextVariablesForNull() {
    ContextManager context = new ContextManager();
    Assert.assertEquals("", context.replaceContextVariables(null, "${value}"));
  }

  @Test
  public void testReplaceContextVariable() {
    ContextManager context = new ContextManager();
    context.setValue("nkey", "1");
    Object value = context.replaceContextVariables(null, "${nkey}");
    Assert.assertEquals("1", value);
  }

  @Test
  public void testReplaceContextVariableInValid() {
    ContextManager context = new ContextManager();
    context.setValue("nkey", "1");
    Object valueEmpty = context.replaceContextVariables(null, "${n}");
    Assert.assertEquals("", valueEmpty);
  }

  @Test
  public void testReplaceContextVariableEmptyContext() {
    ContextManager context = new ContextManager();
    Object valueEmpty = context.replaceContextVariables(new TestContext("sample"), "${n}");
    Assert.assertEquals("", valueEmpty);
  }

  @Test
  public void testReplaceContextVariableInvalidExp() {
    ContextManager context = new ContextManager();
    Object value = context.replaceContextVariables(null, "${nkey");
    Assert.assertEquals("${nkey", value);
  }

  @Test
  public void testResolveVariableInNSWithEmptyContext() {
    ContextManager context = new ContextManager();
    Object value = context.resolveVariableInNS(new HashMap<>(), "${nkey");
    Assert.assertNull(value);
  }

  @Test
  public void testResolveVariableInNSINvalid() {
    ContextManager context = new ContextManager();
    Object value = context.resolveVariableInNS(Map.of("nkey", "1"), "${nkey");
    Assert.assertNull(value);
  }

  @Test
  public void testReplaceContextVariablesArray() {
    ContextManager context = new ContextManager();
    TestContext testContext = new TestContext("Sample");
    testContext.addValue("nkey", "[1]");
    Object value = context.replaceContextVariables(testContext, "${nkey}");
    Assert.assertNotNull(value);
  }

  @Test
  public void testReplaceContextVariablesNested() {
    ContextManager context = new ContextManager();
    TestContext testContext = new TestContext("Sample");
    testContext.addValue("nkey", "${self}");
    testContext.addValue("self", "1");
    Object value = context.replaceContextVariables(testContext, "${nkey}");
    Assert.assertEquals("1", value);
  }

  @Test
  public void testReplaceContextVariablesOfMap() {
    ContextManager context = new ContextManager();

    TestContext testContext = new TestContext("Sample");
    testContext.addValue(
        "sample",
        new HashMap(
            Map.of(
                "request",
                new ArrayList(
                    Arrays.asList(
                        new HashMap(Map.of("key", "value")),
                        new HashMap(Map.of("key2", "value2")))))));

    Assert.assertEquals(
        "value", context.replaceContextVariables(testContext, "${sample.request.key}"));
    Assert.assertEquals(
        "value2", context.replaceContextVariables(testContext, "${sample.request.key2}"));
  }

  @Test
  public void testReplaceContextVariablesOfArray() {
    ContextManager context = new ContextManager();

    TestContext testContext = new TestContext("Sample");
    testContext.addValue(
        "sample",
        new HashMap(
            Map.of(
                "request",
                new ArrayList(
                    Arrays.asList(
                        new ArrayList(Arrays.asList(new HashMap(Map.of("key", "value")))),
                        new HashMap(Map.of("key2", "value2")))))));

    Assert.assertEquals(
        "value", context.replaceContextVariables(testContext, "${sample.request[0][0].key}"));
    Assert.assertEquals(
        "value2", context.replaceContextVariables(testContext, "${sample.request[1].key2}"));
    Assert.assertTrue(
        context.replaceContextVariables(testContext, "${sample.request[0,1]}") instanceof List);
    Assert.assertEquals(
        "", context.replaceContextVariables(testContext, "${sample.request[yes].key2}"));
    Assert.assertEquals(
        "", context.replaceContextVariables(testContext, "${sample.request[20].key2}"));
  }

  @Test
  public void testReplaceContextVariablesOfMapArray() {
    ContextManager context = new ContextManager();

    TestContext testContext = new TestContext("Sample");
    testContext.addValue(
        "sample",
        new ArrayList(
            Collections.singletonList(
                new ArrayList(
                    Arrays.asList(
                        new HashMap(Map.of("request", new HashMap(Map.of("key", "value")))),
                        new HashMap(Map.of("key2", "value2")))))));

    Assert.assertEquals(
        "value", context.replaceContextVariables(testContext, "${sample.request.key}"));
    Assert.assertEquals("value2", context.replaceContextVariables(testContext, "${sample.key2}"));
    Assert.assertEquals("", context.replaceContextVariables(testContext, "${sample.request.key2}"));
  }
}
