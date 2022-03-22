package restel.core.http;

import com.pramati.restel.core.http.RESTClient;
import com.pramati.restel.core.http.RESTResponse;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RestClientTest {

  private RESTClient restClient;

  @Before
  public void before() {
    restClient = new RESTClient("https://petstore.swagger.io");
  }

  @Test
  public void makeCallTest() {
    RESTResponse response =
        restClient.makeCall(
            "GET", "/v2/store/inventory", Map.of("Content-Type", "application/json"), null, null);
    Assert.assertEquals(200, response.getStatus());
  }

  @Test
  public void makeCallTestInvalidAPI() {
    RESTResponse response = restClient.makeCall("POST", "/v2/user", null, null, "Invalid body");
    Assert.assertNotEquals(200, response.getStatus());
  }

  @Test
  public void makeCallTestMultiPartAPI() {
    RESTResponse response =
        restClient.makeCall(
            "POST",
            "/v2/pet/3222/uploadImage",
            new HashMap<>(Map.of("Content-Type", "multipart/multipart/form-data")),
            null,
            Map.of("additionalMetadata", "22", "file", "@src/test/resources/img/dog.jpeg"));
    Assert.assertEquals(200, response.getStatus());
  }
}
