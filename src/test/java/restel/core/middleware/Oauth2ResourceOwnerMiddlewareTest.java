package restel.core.middleware;

import com.pramati.restel.core.http.RESTRequest;
import com.pramati.restel.core.middleware.request.Oauth2ResourceOwnerMiddleware;
import com.pramati.restel.core.model.oauth.ResourceOwnerPassword;
import com.pramati.restel.exception.RestelException;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import java.util.HashMap;


public class Oauth2ResourceOwnerMiddlewareTest {

    @Test
    public void testResourceOwnerMiddleware() {
        Oauth2ResourceOwnerMiddleware middleware = new Oauth2ResourceOwnerMiddleware(getResources());
        RESTRequest restRequest = new RESTRequest();
        restRequest.setHeaders(new HashMap<>());
        RESTRequest request = middleware.process(restRequest);
        Assert.assertNotNull(request.getHeaders().get(HttpHeaders.AUTHORIZATION));

    }

    @Test(expected = RestelException.class)
    public void testInvalidResourceOwnerMiddleware() {
        ResourceOwnerPassword resources = getResources();
        resources.setPassword("*********");
        Oauth2ResourceOwnerMiddleware middleware = new Oauth2ResourceOwnerMiddleware(resources);
        RESTRequest restRequest = new RESTRequest();
        restRequest.setHeaders(new HashMap<>());
        RESTRequest request = middleware.process(restRequest);
        request.getHeaders().get(HttpHeaders.AUTHORIZATION);
    }

    private ResourceOwnerPassword getResources() {
        ResourceOwnerPassword password = new ResourceOwnerPassword();
        password.setAuthUrl("https://dev-600672.okta.com/oauth2/default/v1/token");
        password.setClientId("0oa113t429MUrEPAx4x7");
        password.setClientSecret("TukOzcquiqDdL-mUhiImn7COTlgSw5OVQCw8a-VT");
        password.setUsername("2311uttam@gmail.com");
        password.setPassword("Pramati@123");
        password.setScope("restel tester");

        return password;
    }
}