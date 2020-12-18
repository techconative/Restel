package restel.core.middleware;

import com.pramati.restel.core.http.RESTClient;
import com.pramati.restel.core.http.RESTRequest;
import com.pramati.restel.core.http.RESTResponse;
import com.pramati.restel.core.http.ResponseBody;
import com.pramati.restel.core.middleware.request.Oauth2ResourceOwnerMiddleware;
import com.pramati.restel.core.model.oauth.ResourceOwnerPassword;
import com.pramati.restel.exception.RestelException;
import com.pramati.restel.utils.Constants;
import com.pramati.restel.utils.ObjectMapperUtils;
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
@PrepareForTest(Oauth2ResourceOwnerMiddleware.class)
public class Oauth2ResourceOwnerMiddlewareTest {
    @InjectMocks
    Oauth2ResourceOwnerMiddleware middleware;

    @Mock
    ResourceOwnerPassword cred;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testResourceOwnerMiddleware() throws Exception {
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
        password.setAuthUrl("url");
        password.setClientId("effasc");
        password.setClientSecret("rwfavvrre-mUhiImn7COTlgSw5OVQCw8a-VT");
        password.setUsername("mail");
        password.setPassword("pwd");
        password.setScope("refsw");

        return password;
    }
}