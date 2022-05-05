package restel.core.manager;

import com.techconative.restel.core.model.TestContext;
import com.techconative.restel.core.utils.ContextUtils;
import java.util.*;
import org.junit.Assert;
import org.junit.Test;

public class ContextUtilsTest {

  @Test
  public void testReplaceContextVariablesForArray() {
    TestContext testContext = new TestContext("Sample");
    testContext.addValue("key", "1");
    testContext.addValue("key1", "value");

    // In the above context, this will return a list [value]
    // Like ${key${key}} (resolving inner ${key})--> ${key1} resolving key1
    // -> value
    Assert.assertEquals(
        Arrays.asList("value"),
        ContextUtils.replaceContextVariables(testContext, Arrays.asList("${key${key}}")));
  }

  @Test
  public void testReplaceContextVariablesWithNestedKey() {

    TestContext testContext = new TestContext("Sample");
    testContext.addValue("key", "1");
    testContext.addValue("key1", "value");

    Assert.assertEquals(
        Arrays.asList("value"),
        ContextUtils.replaceContextVariables(testContext, (Object) Arrays.asList("${key${key}}")));
  }

  @Test
  public void testReplaceContextVariablesWithCollection() {

    Map<String, Object> data = new HashMap<>();
    data.put("resp", new HashMap<>(Map.of("k", new ArrayList<>(Arrays.asList(1)))));
    TestContext testContext = new TestContext("Sample");
    testContext.addValue("key", "1");
    testContext.addValue("key1", new ArrayList<>(Arrays.asList("value")));
    testContext.addValue("result", data);

    Assert.assertEquals(
        Arrays.asList("value"),
        ContextUtils.replaceContextVariables(testContext, "${key${result.resp.k[0]}}"));
  }

  @Test
  public void testReplaceContextVariablesForNull() {
    Assert.assertEquals("${value}", ContextUtils.replaceContextVariables(null, "${value}"));
  }

  @Test
  public void testReplaceContextVariableEmptyContext() {
    Object valueEmpty = ContextUtils.replaceContextVariables(new TestContext("sample"), "${n}");
    Assert.assertEquals("", valueEmpty);
  }

  @Test
  public void testReplaceContextVariableInvalidExp() {
    Object value = ContextUtils.replaceContextVariables(null, "${nkey");
    Assert.assertEquals("${nkey", value);
  }
  //
  //  @Test
  //  public void testResolveVariableInNSWithEmptyContext() {
  //    Object value = ContextUtils.resolveVariableInNS(new HashMap<>(), "${nkey");
  //    Assert.assertNull(value);
  //  }
  //
  //  @Test
  //  public void testResolveVariableInNSINvalid() {
  //    Object value = ContextUtils.resolveVariableInNS(Map.of("nkey", "1"), "${nkey");
  //    Assert.assertNull(value);
  //  }

  @Test
  public void testReplaceContextVariablesArray() {
    TestContext testContext = new TestContext("Sample");
    testContext.addValue("nkey", "[1]");
    Object value = ContextUtils.replaceContextVariables(testContext, "${nkey}");
    Assert.assertNotNull(value);
  }

  @Test
  public void testReplaceContextVariablesNested() {
    TestContext testContext = new TestContext("Sample");
    testContext.addValue("nkey", "${self}");
    testContext.addValue("self", "1");
    Object value = ContextUtils.replaceContextVariables(testContext, "${nkey}");
    Assert.assertEquals("1", value);
  }

  @Test
  public void testReplaceContextVariablesOfMap() {

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
        "value", ContextUtils.replaceContextVariables(testContext, "${sample.request.key}"));
    Assert.assertEquals(
        "value2", ContextUtils.replaceContextVariables(testContext, "${sample.request.key2}"));
  }

  @Test
  public void testReplaceContextVariablesOfArray() {

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
        "value", ContextUtils.replaceContextVariables(testContext, "${sample.request[0][0].key}"));
    Assert.assertEquals(
        "value2", ContextUtils.replaceContextVariables(testContext, "${sample.request[1].key2}"));
    Assert.assertTrue(
        ContextUtils.replaceContextVariables(testContext, "${sample.request[0,1]}")
            instanceof List);
    Assert.assertEquals(
        "", ContextUtils.replaceContextVariables(testContext, "${sample.request[yes].key2}"));
    Assert.assertEquals(
        "", ContextUtils.replaceContextVariables(testContext, "${sample.request[20].key2}"));
  }

  @Test
  public void testReplaceContextVariablesOfMapArray() {

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
        "value", ContextUtils.replaceContextVariables(testContext, "${sample.request.key}"));
    Assert.assertEquals(
        "value2", ContextUtils.replaceContextVariables(testContext, "${sample.key2}"));
    Assert.assertEquals(
        "", ContextUtils.replaceContextVariables(testContext, "${sample.request.key2}"));
  }
}
