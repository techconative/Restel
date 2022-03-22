package restel.core.resolver.function;

import com.google.common.collect.Maps;
import com.pramati.restel.core.model.GlobalContext;
import com.pramati.restel.core.resolver.function.RestelFunctionResolver;
import com.pramati.restel.exception.RestelException;
import com.pramati.restel.utils.ObjectMapperUtils;
import java.util.*;
import org.junit.Assert;
import org.junit.Test;

public class RestelFunctionResolverTest {

  @Test
  public void testResolveRemoveOperation() {
    Map<String, Object> context = GlobalContext.getInstance().getContextValues();
    Map<String, Object> data = new HashMap<>();
    data.put("value", new ArrayList<>(Arrays.asList("Sam", "Ram", "Tom")));
    context.put("data", new HashMap<>(Map.of("response", data)));
    Map<String, Object> out =
        (Map<String, Object>)
            RestelFunctionResolver.resolveRemoveOperation(
                context, "data.response.value[0,1]", null);
    Assert.assertEquals(
        "Tom",
        ObjectMapperUtils.convertToJsonNode(out)
            .get("data")
            .get("response")
            .get("value")
            .get(0)
            .asText());
    Assert.assertEquals(
        1,
        ObjectMapperUtils.convertToJsonNode(out).get("data").get("response").get("value").size());
    Assert.assertTrue(
        ObjectMapperUtils.convertToJsonNode(out)
            .get("data")
            .get("response")
            .get("value")
            .isArray());
  }

  @Test
  public void testResolveRemoveOperationForArrayInput() {
    Map<String, Object> context = GlobalContext.getInstance().getContextValues();
    Map<String, Object> data = new HashMap<>();
    data.put("val", new ArrayList<>(Arrays.asList("Sam", "Ram", "Tom")));
    data.put("value", new ArrayList<>(Arrays.asList("Sam", "Ram", "Tom")));
    context.put("data", new HashMap<>(Map.of("response", data)));
    List<Object> out =
        (List)
            RestelFunctionResolver.resolveRemoveOperation(
                Arrays.asList(Arrays.asList(context)), "data.response.value[0,1]", null);
    Assert.assertEquals(
        "Tom",
        ObjectMapperUtils.convertToJsonNode(out)
            .get(0)
            .get(0)
            .get("data")
            .get("response")
            .get("value")
            .get(0)
            .asText());
    Assert.assertEquals(
        1,
        ObjectMapperUtils.convertToJsonNode(out)
            .get(0)
            .get(0)
            .get("data")
            .get("response")
            .get("value")
            .size());
    Assert.assertTrue(
        ObjectMapperUtils.convertToJsonNode(out)
            .get(0)
            .get(0)
            .get("data")
            .get("response")
            .get("value")
            .isArray());
  }

  @Test
  public void testResolveRemoveOperationWithArray() {
    Map<String, Object> context = GlobalContext.getInstance().getContextValues();
    Map<String, Object> data = new HashMap<>();
    data.put(
        "value",
        new ArrayList<>(
            Arrays.asList(
                Maps.newHashMap(
                    Map.of("names", new ArrayList(Arrays.asList("Sam", "Ram", "Tom")))))));
    context.put("data", new HashMap<>(Map.of("response", data)));
    Map<String, Object> out =
        (Map<String, Object>)
            RestelFunctionResolver.resolveRemoveOperation(
                context, "data.response.value.names[0,1]", null);
    Assert.assertEquals(
        "Tom",
        ObjectMapperUtils.convertToJsonNode(out)
            .get("data")
            .get("response")
            .get("value")
            .get(0)
            .get("names")
            .get(0)
            .asText());
  }

  @Test
  public void testResolveRemoveOperationWithMap() {
    Map<String, Object> context = GlobalContext.getInstance().getContextValues();
    Map<String, Object> data = new HashMap<>();
    data.put(
        "value",
        new ArrayList<>(
            Arrays.asList(
                Maps.newHashMap(
                    Map.of("names", new ArrayList(Arrays.asList("Sam", "Ram", "Tom")))))));
    context.put("data", new HashMap<>(Map.of("response", data, "name", "Tom")));
    Map<String, Object> out =
        (Map<String, Object>)
            RestelFunctionResolver.resolveRemoveOperation(context, "data.response.value", null);
    Assert.assertTrue(
        ObjectMapperUtils.convertToJsonNode(out).get("data").get("response").isEmpty());
  }

  @Test(expected = RestelException.class)
  public void testResolveRemoveOperationWithEmptyContext() {
    Map<String, Object> out =
        (Map<String, Object>) RestelFunctionResolver.resolveRemoveOperation(null, "null", null);
  }
}
