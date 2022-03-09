package restel.core.http;

import com.techconative.restel.core.http.RESTRequest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class RestRequestTest {
    @Test
    public void testRestRequest() {
        RESTRequest restRequest = new RESTRequest();

        restRequest.setEndpoint("endpoint");
        Assert.assertEquals("endpoint", restRequest.getEndpoint());

        restRequest.setHeaders(Map.of("k", "v"));
        Assert.assertEquals(Map.of("k", "v"), restRequest.getHeaders());

        restRequest.setRequestParams(Map.of("k", "v"));
        Assert.assertEquals(Map.of("k", "v"), restRequest.getRequestParams());

        restRequest.setMethod("GET");
        Assert.assertEquals("GET", restRequest.getMethod());

        restRequest.setRequestBody("body");
        Assert.assertEquals("body", restRequest.getRequestBody());

        Assert.assertNotEquals(RESTRequest.builder().build(), restRequest);
        Assert.assertNotEquals(restRequest.hashCode(), RESTRequest.builder().hashCode());
        Assert.assertNotNull(restRequest.toString());
    }
}
