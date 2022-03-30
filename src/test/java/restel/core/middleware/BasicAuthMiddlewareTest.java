package restel.core.middleware;

import com.techconative.restel.core.http.RESTRequest;
import com.techconative.restel.core.middleware.request.BasicAuthMiddleware;
import com.techconative.restel.core.model.oauth.BasicAuth;
import com.techconative.restel.exception.RestelException;
import java.util.HashMap;
import javax.ws.rs.core.HttpHeaders;
import org.junit.Assert;
import org.junit.Test;

public class BasicAuthMiddlewareTest {
  @Test
  public void testBasicAuth() {
    BasicAuth cred = new BasicAuth();
    cred.setUsername("User");
    cred.setPassword("pass");
    BasicAuthMiddleware middleware = new BasicAuthMiddleware(cred);
    RESTRequest restRequest = new RESTRequest();
    restRequest.setHeaders(new HashMap<>());
    RESTRequest request = middleware.process(restRequest);
    Assert.assertNotNull(request.getHeaders().get(HttpHeaders.AUTHORIZATION));
  }

  @Test(expected = RestelException.class)
  public void testBasicAuthInvalid() {
    BasicAuth cred = new BasicAuth();
    BasicAuthMiddleware middleware = new BasicAuthMiddleware(cred);
    RESTRequest restRequest = new RESTRequest();
    RESTRequest request = middleware.process(restRequest);
    request.getHeaders().get(HttpHeaders.AUTHORIZATION);
  }
}
