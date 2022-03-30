package restel.core.model.comparators;

import com.techconative.restel.core.http.RESTResponse;
import com.techconative.restel.core.http.ResponseBody;
import com.techconative.restel.core.model.comparator.ExactMatchComparator;
import java.util.Arrays;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.junit.Assert;
import org.junit.Test;

public class ExactMatcherComparatorTest {
  @Test
  public void testCompare() {
    RESTResponse restResponse = new RESTResponse();
    restResponse.setResponse(ResponseBody.builder().body(Map.of("key", "value")).build());
    ExactMatchComparator comparator = new ExactMatchComparator();
    Assert.assertTrue(comparator.compare(restResponse, Map.of("key", "value")));
    Assert.assertTrue(comparator.compare(Map.of("key", "value"), Map.of("key", "value")));
  }

  @Test(expected = AssertionError.class)
  public void testCompareFailure() {
    RESTResponse restResponse = new RESTResponse();
    restResponse.setResponse(ResponseBody.builder().body(Map.of("key", "value")).build());
    ExactMatchComparator comparator = new ExactMatchComparator();
    comparator.compare(restResponse, Map.of("key", "value", "p", "q"));
  }

  @Test(expected = AssertionError.class)
  public void testCompareMismatch() {
    RESTResponse restResponse = new RESTResponse();
    restResponse.setResponse(ResponseBody.builder().body(Map.of("key", "value", "p", "q")).build());
    ExactMatchComparator comparator = new ExactMatchComparator();
    comparator.compare(restResponse, Map.of("key", "value"));
  }

  @Test(expected = AssertionError.class)
  public void testCompareTestArrayInvalidOrderResponse() {
    RESTResponse restResponse = new RESTResponse();
    restResponse.setResponse(ResponseBody.builder().body(Arrays.asList("key", "value")).build());
    restResponse.setHeaders(
        Map.of(HttpHeaders.CONTENT_TYPE.toLowerCase(), MediaType.APPLICATION_JSON));
    ExactMatchComparator comparator = new ExactMatchComparator();
    comparator.compare(restResponse, Arrays.asList("value", "key"));
  }

  @Test
  public void testCompareTestArrayValidOrderResponse() {
    RESTResponse restResponse = new RESTResponse();
    restResponse.setResponse(ResponseBody.builder().body(Arrays.asList("key", "value")).build());
    restResponse.setHeaders(
        Map.of(HttpHeaders.CONTENT_TYPE.toLowerCase(), MediaType.APPLICATION_JSON));
    ExactMatchComparator comparator = new ExactMatchComparator();
    Assert.assertTrue(comparator.compare(restResponse, Arrays.asList("key", "value")));
  }

  @Test
  public void testCompareTestPlainTextResponse() {
    RESTResponse restResponse = new RESTResponse();
    restResponse.setResponse(ResponseBody.builder().body("I am healthy").build());
    restResponse.setHeaders(Map.of(HttpHeaders.CONTENT_TYPE.toLowerCase(), MediaType.TEXT_PLAIN));
    ExactMatchComparator comparator = new ExactMatchComparator();
    Assert.assertTrue(comparator.compare(restResponse, "I am healthy"));
  }
}
