package restel.core.parser.dto;

import com.techconative.restel.core.parser.dto.TestApiDefinitions;
import java.util.Arrays;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class TestDefinitionsTest {

  @Test
  public void testTestDefinitions() {
    TestApiDefinitions testApiDefinitions = new TestApiDefinitions();

    testApiDefinitions.setAcceptedStatusCodes(Arrays.asList("200"));
    Assert.assertEquals(Arrays.asList("200"), testApiDefinitions.getAcceptedStatusCodes());

    testApiDefinitions.setApiDescription("Desc");
    Assert.assertEquals("Desc", testApiDefinitions.getApiDescription());

    testApiDefinitions.setApiUniqueName("name");
    Assert.assertEquals("name", testApiDefinitions.getApiUniqueName());

    testApiDefinitions.setDependsOn("depends");
    Assert.assertEquals("depends", testApiDefinitions.getDependsOn());

    testApiDefinitions.setExpectedHeader("header");
    Assert.assertEquals("header", testApiDefinitions.getExpectedHeader());

    testApiDefinitions.setExpectedHeaderMatcher("header_matcher");
    Assert.assertEquals("header_matcher", testApiDefinitions.getExpectedHeaderMatcher());

    testApiDefinitions.setExpectedResponse("response");
    Assert.assertEquals("response", testApiDefinitions.getExpectedResponse());

    testApiDefinitions.setExpectedResponseMatcher("response_matcher");
    Assert.assertEquals("response_matcher", testApiDefinitions.getExpectedResponseMatcher());

    testApiDefinitions.setRequestBodyParams("body_params");
    Assert.assertEquals("body_params", testApiDefinitions.getRequestBodyParams());

    testApiDefinitions.setRequestHeaders("req_header");
    Assert.assertEquals("req_header", testApiDefinitions.getRequestHeaders());

    testApiDefinitions.setRequestMethod("method");
    Assert.assertEquals("method", testApiDefinitions.getRequestMethod());

    testApiDefinitions.setRequestPathParams("path_params");
    Assert.assertEquals("path_params", testApiDefinitions.getRequestPathParams());

    testApiDefinitions.setRequestPostCallHook("post_hook");
    Assert.assertEquals("post_hook", testApiDefinitions.getRequestPostCallHook());

    testApiDefinitions.setRequestPreCallHook("pre_hook");
    Assert.assertEquals("pre_hook", testApiDefinitions.getRequestPreCallHook());

    testApiDefinitions.setRequestQueryParams("query");
    Assert.assertEquals("query", testApiDefinitions.getRequestQueryParams());

    testApiDefinitions.setRequestUrl("url");
    Assert.assertEquals("url", testApiDefinitions.getRequestUrl());

    testApiDefinitions.setTags(Set.of("tag"));
    Assert.assertEquals(testApiDefinitions.getTags(), Set.of("tag"));

    Assert.assertNotEquals(testApiDefinitions, new TestApiDefinitions());
    Assert.assertNotEquals(testApiDefinitions.hashCode(), new TestApiDefinitions().hashCode());
    Assert.assertNotNull(testApiDefinitions.toString());
  }
}
