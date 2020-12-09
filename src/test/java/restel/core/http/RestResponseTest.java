package restel.core.http;

import com.pramati.restel.core.http.RESTResponse;
import com.pramati.restel.core.http.ResponseBody;
import org.junit.Test;
import org.testng.Assert;

import java.util.Map;

public class RestResponseTest {
    @Test
    public void testRestResponse() {
        RESTResponse restResponse = new RESTResponse();

        restResponse.setHeaders(Map.of("k", "v"));
        Assert.assertEquals(restResponse.getHeaders(), Map.of("k", "v"));

        restResponse.setResponse(ResponseBody.builder().body("resp").build());
        Assert.assertEquals(restResponse.getResponse().getBody(), "resp");

        restResponse.setStatus(200);
        Assert.assertEquals(restResponse.getStatus(), 200);

        Assert.assertNotEquals(restResponse.hashCode(), new RESTResponse().hashCode());
        Assert.assertNotEquals(restResponse, new RESTResponse());
        Assert.assertNotNull(restResponse.toString());

    }
}
