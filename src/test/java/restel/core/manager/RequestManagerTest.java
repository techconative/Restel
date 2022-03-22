package restel.core.manager;

import com.pramati.restel.core.http.RESTRequest;
import com.pramati.restel.core.http.RESTResponse;
import com.pramati.restel.core.managers.RequestManager;
import com.pramati.restel.core.middleware.request.RequestMiddleware;
import java.util.Arrays;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RequestManagerTest {

  private RequestManager manager;

  @Before
  public void before() {
    manager = new RequestManager("https://petstore.swagger.io");
  }

  @Test
  public void testMakeCall() {

    RESTRequest request =
        RESTRequest.builder().method("GET").endpoint("/v2/store/inventory").build();

    RequestMiddleware newFiledAdder =
        new RequestMiddleware() {

          @Override
          public RESTRequest process(RESTRequest request) {
            request.setHeaders(Map.of("Accept", "text/json"));
            return request;
          }
        };

    RESTResponse makeCall = manager.makeCall(request, Arrays.asList(newFiledAdder), null);

    Assert.assertNotNull(makeCall.getResponse());
  }
}
