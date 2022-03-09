package restel.core.middleware;

import com.techconative.restel.core.http.RESTClient;
import com.techconative.restel.core.http.RESTRequest;
import com.techconative.restel.core.http.RESTResponse;
import com.techconative.restel.core.http.ResponseBody;
import com.techconative.restel.core.middleware.request.Oauth2ClientCredentialMiddleware;
import com.techconative.restel.core.model.oauth.ClientCredentials;
import com.techconative.restel.exception.RestelException;
import com.techconative.restel.utils.Constants;
import com.techconative.restel.utils.ObjectMapperUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.core.HttpHeaders;
import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Oauth2ClientCredentialMiddleware.class)
public class Oauth2ClientCredentialMiddlewareTest {
    @InjectMocks
    Oauth2ClientCredentialMiddleware middleware;

    @Mock
    ClientCredentials cred;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testOauthClientCredential() throws Exception {
        RESTResponse restResponse = new RESTResponse();
        restResponse.setStatus(200);
        restResponse.setHeaders(Map.of());
        restResponse.setResponse(ResponseBody.builder().body(ObjectMapperUtils.getMapper().createObjectNode().put(Constants.ACCESS_TOKEN, "Token").toString()).build());

        RESTClient client = Mockito.mock(RESTClient.class);
        PowerMockito.whenNew(RESTClient.class).withAnyArguments().thenReturn(client);
        PowerMockito.doReturn(restResponse).when(client).makeCall(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap(), Mockito.anyMap(), Mockito.any());
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
        clientCredentials.setAuthUrl("url");
        clientCredentials.setClientId("refdddasdwdwda");
        clientCredentials.setClientSecret("efacefwfe");
        return clientCredentials;
    }
}
