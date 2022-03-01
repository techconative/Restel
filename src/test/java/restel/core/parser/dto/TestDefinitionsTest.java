package restel.core.parser.dto;

import com.pramati.restel.core.parser.dto.TestDefinitions;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Set;

public class TestDefinitionsTest {

    @Test
    public void testTestDefinitions() {
        TestDefinitions testDefinitions = new TestDefinitions();

        testDefinitions.setAcceptedStatusCodes(Arrays.asList("200"));
        Assert.assertEquals(Arrays.asList("200"),testDefinitions.getAcceptedStatusCodes());

        testDefinitions.setCaseDescription("Desc");
        Assert.assertEquals("Desc",testDefinitions.getCaseDescription());

        testDefinitions.setCaseUniqueName("name");
        Assert.assertEquals("name",testDefinitions.getCaseUniqueName());

        testDefinitions.setDependsOn("depends");
        Assert.assertEquals("depends",testDefinitions.getDependsOn());

        testDefinitions.setExpectedHeader("header");
        Assert.assertEquals("header",testDefinitions.getExpectedHeader());

        testDefinitions.setExpectedHeaderMatcher("header_matcher");
        Assert.assertEquals("header_matcher",testDefinitions.getExpectedHeaderMatcher());

        testDefinitions.setExpectedResponse("response");
        Assert.assertEquals("response",testDefinitions.getExpectedResponse());

        testDefinitions.setExpectedResponseMatcher("response_matcher");
        Assert.assertEquals("response_matcher",testDefinitions.getExpectedResponseMatcher());

        testDefinitions.setRequestBodyParams("body_params");
        Assert.assertEquals("body_params",testDefinitions.getRequestBodyParams());

        testDefinitions.setRequestHeaders("req_header");
        Assert.assertEquals("req_header",testDefinitions.getRequestHeaders());

        testDefinitions.setRequestMethod("method");
        Assert.assertEquals("method",testDefinitions.getRequestMethod());

        testDefinitions.setRequestPathParams("path_params");
        Assert.assertEquals("path_params",testDefinitions.getRequestPathParams());

        testDefinitions.setRequestPostCallHook("post_hook");
        Assert.assertEquals("post_hook",testDefinitions.getRequestPostCallHook());

        testDefinitions.setRequestPreCallHook("pre_hook");
        Assert.assertEquals("pre_hook",testDefinitions.getRequestPreCallHook() );

        testDefinitions.setRequestQueryParams("query");
        Assert.assertEquals("query",testDefinitions.getRequestQueryParams() );

        testDefinitions.setRequestUrl("url");
        Assert.assertEquals("url",testDefinitions.getRequestUrl());

        testDefinitions.setTags(Set.of("tag"));
        Assert.assertEquals(testDefinitions.getTags(), Set.of("tag"));

        Assert.assertNotEquals(testDefinitions,new TestDefinitions());
        Assert.assertNotEquals(testDefinitions.hashCode(), new TestDefinitions().hashCode());
        Assert.assertNotNull(testDefinitions.toString());

    }
}
