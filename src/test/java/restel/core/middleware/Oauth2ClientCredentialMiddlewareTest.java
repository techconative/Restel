package restel.core.middleware;

import com.pramati.restel.core.http.RESTRequest;
import com.pramati.restel.core.middleware.request.Oauth2ClientCredentialMiddleware;
import com.pramati.restel.core.model.oauth.ClientCredentials;
import com.pramati.restel.exception.RestelException;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import java.util.HashMap;

public class Oauth2ClientCredentialMiddlewareTest {
    @Test
    public void testOauthClientCredential() {
        // Sample testing with Okta, needs to mock the testcase RestClient class.
        ClientCredentials cred = getClientCred();
        cred.setScope("restel tester");
        Oauth2ClientCredentialMiddleware middleware = new Oauth2ClientCredentialMiddleware(cred);
        RESTRequest restRequest = new RESTRequest();
        restRequest.setHeaders(new HashMap<>());
        RESTRequest request = middleware.process(restRequest);
        Assert.assertNotNull(request.getHeaders().get(HttpHeaders.AUTHORIZATION));
    }

    @Test(expected = RestelException.class)
    public void testOauthClientCredentialInvalid() {
        // Sample testing with Okta, needs to mock the testcase RestClient class.
        ClientCredentials cred = getClientCred();
        Oauth2ClientCredentialMiddleware middleware = new Oauth2ClientCredentialMiddleware(cred);
        RESTRequest restRequest = new RESTRequest();
        restRequest.setHeaders(new HashMap<>());
        RESTRequest request = middleware.process(restRequest);
        request.getHeaders().get(HttpHeaders.AUTHORIZATION);
    }

    private ClientCredentials getClientCred() {
        ClientCredentials clientCredentials = new ClientCredentials();
        clientCredentials.setAuthUrl("https://dev-600672.okta.com/oauth2/default/v1/token");
        clientCredentials.setClientId("0oa10nhj6gkhyPLXb4x7");
        clientCredentials.setClientSecret("8A6eAB8hUjOb9w5hWCT6CndX5FY0gFomfRMvv27j");
        return clientCredentials;
    }
}
