package restel.core.resolver.function;

import com.google.common.collect.Maps;
import com.techconative.restel.core.model.GlobalContext;
import com.techconative.restel.core.model.functions.FunctionOps;
import com.techconative.restel.core.model.functions.RestelFunction;
import com.techconative.restel.core.resolver.function.RestelFunctionExecutor;
import com.techconative.restel.exception.RestelException;
import com.techconative.restel.utils.ObjectMapperUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class RestelFunctionExecutorTest {

  @Test
  public void testExecRemoveFunction() {
    Map<String, Object> context = GlobalContext.getInstance().getContextValues();
    Map<String, Object> data = new HashMap<>();
    data.put("value", new ArrayList<>(Arrays.asList("Sam", "Ram", "Tom")));
    Map<String, Object> data2 = new HashMap<>();
    data2.put("data", new HashMap<>(Map.of("response", data)));
    context.put("exec", data2);

    RestelFunctionExecutor executor = new RestelFunctionExecutor("Sample");
    RestelFunction function = new RestelFunction();
    function.setOperation(FunctionOps.REMOVE);
    function.setData("${exec.data.response.value[0,1]}");
    Map<String, Object> out = (Map<String, Object>) executor.execRemoveFunction(function);
    Assert.assertEquals(
        "Tom", ObjectMapperUtils.convertToJsonNode(out).get("value").get(0).asText());
    Assert.assertEquals(1, ObjectMapperUtils.convertToJsonNode(out).get("value").size());
    Assert.assertTrue(ObjectMapperUtils.convertToJsonNode(out).get("value").isArray());
  }

  @Test
  public void testExecRemoveFunctionWithArray() {
    Map<String, Object> context = GlobalContext.getInstance().getContextValues();
    Map<String, Object> data = new HashMap<>();
    data.put(
        "value",
        new ArrayList<>(
            Arrays.asList(
                Maps.newHashMap(
                    Map.of("names", new ArrayList(Arrays.asList("Sam", "Ram", "Tom")))))));
    Map<String, Object> data2 = new HashMap<>();

    data2.put("data", new HashMap<>(Map.of("request", data)));
    context.put("exec", data2);

    RestelFunctionExecutor executor = new RestelFunctionExecutor("Sample");
    RestelFunction function = new RestelFunction();
    function.setOperation(FunctionOps.REMOVE);
    function.setData("${exec.data.request.value.names[0,1]}");

    Map<String, Object> out = (Map<String, Object>) executor.execRemoveFunction(function);
    Assert.assertEquals(
        "Tom",
        ObjectMapperUtils.convertToJsonNode(out).get("value").get(0).get("names").get(0).asText());
  }

  @Test(expected = RestelException.class)
  public void testExecRemoveFunctionWithInvalidSyntax() {
    Map<String, Object> context = GlobalContext.getInstance().getContextValues();
    Map<String, Object> data = new HashMap<>();
    data.put(
        "value",
        new ArrayList<>(
            Arrays.asList(
                Maps.newHashMap(
                    Map.of("names", new ArrayList(Arrays.asList("Sam", "Ram", "Tom")))))));
    Map<String, Object> data2 = new HashMap<>();
    data2.put("data", new HashMap<>(Map.of("req", data)));
    context.put("exec", data2);

    RestelFunctionExecutor executor = new RestelFunctionExecutor("Sample");
    RestelFunction function = new RestelFunction();
    function.setOperation(FunctionOps.REMOVE);
    function.setData("${exec.data.req.value.names[0,1]}");

    Map<String, Object> out = (Map<String, Object>) executor.execRemoveFunction(function);
  }

  @Test
  public void testExecRemoveFunctionWithoutSyntax() {
    Map<String, Object> context = GlobalContext.getInstance().getContextValues();
    Map<String, Object> data = new HashMap<>();
    data.put(
        "value",
        new ArrayList<>(
            Arrays.asList(
                Maps.newHashMap(
                    Map.of("names", new ArrayList(Arrays.asList("Sam", "Ram", "Tom")))))));
    Map<String, Object> data2 = new HashMap<>();
    data2.put("data", new HashMap<>(Map.of("req", data)));
    context.put("exec", data2);

    RestelFunctionExecutor executor = new RestelFunctionExecutor("Sample");
    RestelFunction function = new RestelFunction();
    function.setOperation(FunctionOps.REMOVE);
    function.setData("exec.data.req.value.names[0,1]");

    Map<String, Object> out = (Map<String, Object>) executor.execRemoveFunction(function);
    Assert.assertEquals(
        "Tom",
        ObjectMapperUtils.convertToJsonNode(out)
            .get("exec")
            .get("data")
            .get("req")
            .get("value")
            .get(0)
            .get("names")
            .get(0)
            .asText());
  }
}
