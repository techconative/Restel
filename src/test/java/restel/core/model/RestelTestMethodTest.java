package restel.core.model;

import com.pramati.restel.core.model.RestelTestMethod;
import com.pramati.restel.utils.ObjectMapperUtils;
import java.util.Arrays;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class RestelTestMethodTest {
  @Test
  public void testRestelTestMethod() {
    RestelTestMethod testMethod = new RestelTestMethod();

    testMethod.setAcceptedStatusCodes(Arrays.asList(200));
    Assert.assertEquals(Arrays.asList(200), testMethod.getAcceptedStatusCodes());

    testMethod.setCaseDescription("desc");
    Assert.assertEquals("desc", testMethod.getCaseDescription());

    testMethod.setCaseUniqueName("name");
    Assert.assertEquals("name", testMethod.getCaseUniqueName());

    testMethod.setDependentOn(null);
    Assert.assertEquals(null, testMethod.getDependentOn());

    testMethod.setExpectedHeader(Map.of("k", "v"));
    Assert.assertEquals(Map.of("k", "v"), testMethod.getExpectedHeader());

    testMethod.setExpectedHeaderMatcher("matcher");
    Assert.assertEquals("matcher", testMethod.getExpectedHeaderMatcher());

    testMethod.setExpectedResponse("response");
    Assert.assertEquals("response", testMethod.getExpectedResponse());

    testMethod.setExpectedResponseMatcher("resp_match");
    Assert.assertEquals("resp_match", testMethod.getExpectedResponseMatcher());

    testMethod.setRequestBodyParams("body_param");
    Assert.assertEquals("body_param", testMethod.getRequestBodyParams());

    testMethod.setRequestHeaders(Map.of("k", "v"));
    Assert.assertEquals(Map.of("k", "v"), testMethod.getRequestHeaders());

    testMethod.setRequestMethod("GET");
    Assert.assertEquals("GET", testMethod.getRequestMethod());

    testMethod.setRequestPostCallHook(ObjectMapperUtils.convertToJsonNode(Map.of("name", "post")));
    Assert.assertEquals(
        ObjectMapperUtils.convertToJsonNode(Map.of("name", "post")),
        testMethod.getRequestPostCallHook());

    testMethod.setRequestPreCallHook(ObjectMapperUtils.convertToJsonNode(Map.of("name", "pre")));
    Assert.assertEquals(
        ObjectMapperUtils.convertToJsonNode(Map.of("name", "pre")),
        testMethod.getRequestPreCallHook());

    testMethod.setRequestQueryParams(Map.of("k", "v"));
    Assert.assertEquals(Map.of("k", "v"), testMethod.getRequestQueryParams());

    testMethod.setRequestUrl("url");
    Assert.assertEquals("url", testMethod.getRequestUrl());

    Assert.assertNotEquals(new RestelTestMethod(), testMethod);
    Assert.assertNotEquals(testMethod.hashCode(), new RestelTestMethod().hashCode());
    Assert.assertNotNull(testMethod.toString());
  }
}
