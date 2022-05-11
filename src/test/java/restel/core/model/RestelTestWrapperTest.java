package restel.core.model;

import com.techconative.restel.core.model.RestelTestApiDefinition;
import com.techconative.restel.core.model.RestelTestApiWrapper;
import com.techconative.restel.utils.ObjectMapperUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class RestelTestWrapperTest {
  @Test
  public void testRestelTestMethod() {
    RestelTestApiWrapper restelTestWrapper = new RestelTestApiWrapper();

    RestelTestApiDefinition restelTestApiDefinition = new RestelTestApiDefinition();
    restelTestApiDefinition.setAcceptedStatusCodes(List.of("200"));
    restelTestApiDefinition.setApiDescription("desc");
    restelTestApiDefinition.setApiUniqueName("name");
    restelTestApiDefinition.setDependentOn(null);
    restelTestApiDefinition.setExpectedHeader(Map.of("k", "v"));
    restelTestApiDefinition.setExpectedHeaderMatcher("matcher");
    restelTestApiDefinition.setExpectedResponse("response");
    restelTestApiDefinition.setExpectedResponseMatcher("resp_match");
    restelTestApiDefinition.setRequestBodyParams("body_param");
    restelTestApiDefinition.setRequestHeaders(Map.of("k", "v"));
    restelTestApiDefinition.setRequestPostCallHook(
        ObjectMapperUtils.convertToJsonNode(Map.of("name", "post")));
    restelTestApiDefinition.setRequestPreCallHook(
        ObjectMapperUtils.convertToJsonNode(Map.of("name", "pre")));
    restelTestApiDefinition.setRequestQueryParams(Map.of("k", "v"));
    restelTestApiDefinition.setRequestUrl("url");
    restelTestApiDefinition.setRequestMethod("GET");
    restelTestWrapper.setTestApiDefinition(restelTestApiDefinition);

    restelTestWrapper.setTestApiWrapperName("testApiWrapperName");
    Assert.assertEquals("testApiWrapperName", restelTestWrapper.getApiUniqueName());

    restelTestWrapper.setTestApiWrapperDescription("a read test that returns 500 status code");
    Assert.assertEquals(
        "a read test that returns 500 status code", restelTestWrapper.getApiDescription());

    restelTestWrapper.setApiParameters(Map.of("param1", "param2"));
    Assert.assertEquals(Map.of("param1", "param2"), restelTestWrapper.getApiParameters());

    Assert.assertEquals(Arrays.asList("200"), restelTestWrapper.getAcceptedStatusCodes());
    Assert.assertEquals(null, restelTestWrapper.getDependentOn());
    Assert.assertEquals(Map.of("k", "v"), restelTestWrapper.getExpectedHeader());
    Assert.assertEquals("matcher", restelTestWrapper.getExpectedHeaderMatcher());
    Assert.assertEquals("response", restelTestWrapper.getExpectedResponse());
    Assert.assertEquals("resp_match", restelTestWrapper.getExpectedResponseMatcher());
    Assert.assertEquals("body_param", restelTestWrapper.getRequestBodyParams());
    Assert.assertEquals(Map.of("k", "v"), restelTestWrapper.getRequestHeaders());
    Assert.assertEquals("GET", restelTestWrapper.getRequestMethod());
    Assert.assertEquals(
        ObjectMapperUtils.convertToJsonNode(Map.of("name", "post")),
        restelTestWrapper.getRequestPostCallHook());
    Assert.assertEquals(
        ObjectMapperUtils.convertToJsonNode(Map.of("name", "pre")),
        restelTestWrapper.getRequestPreCallHook());
    Assert.assertEquals(Map.of("k", "v"), restelTestWrapper.getRequestQueryParams());
    Assert.assertEquals("url", restelTestWrapper.getRequestUrl());

    Assert.assertNotEquals(restelTestWrapper.hashCode(), new RestelTestWrapperTest().hashCode());
    Assert.assertNotNull(restelTestWrapper.toString());
  }
}
