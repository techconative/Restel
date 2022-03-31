package com.techconative.restel.core.middleware.request;

import com.techconative.restel.core.http.RESTClient;
import com.techconative.restel.core.http.RESTRequest;
import com.techconative.restel.core.http.RESTResponse;
import com.techconative.restel.core.model.oauth.ClientCredentials;
import com.techconative.restel.exception.RestelException;
import com.techconative.restel.utils.Constants;
import com.techconative.restel.utils.ObjectMapperUtils;
import java.util.Map;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.testng.collections.Maps;

/** Creates an Authorization token and adds in the request headers. */
public class Oauth2ClientCredentialMiddleware implements RequestMiddleware {

  private final ClientCredentials clientCredentials;

  public Oauth2ClientCredentialMiddleware(ClientCredentials clientCredentials) {
    this.clientCredentials = clientCredentials;
  }

  @Override
  public RESTRequest process(RESTRequest request) {
    try {
      request.addHeader(HttpHeaders.AUTHORIZATION, fetchAccessToken());
    } catch (Exception ex) {
      throw new RestelException(ex, "OAUTH_FAILURE", clientCredentials);
    }
    return request;
  }

  private String fetchAccessToken() {
    RESTClient client = new RESTClient(clientCredentials.getAuthUrl());
    Map<String, Object> headers = Maps.newHashMap();
    headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
    RESTResponse restResponse =
        client.makeCall(
            HttpMethod.POST, "", headers, Maps.newHashMap(), getClientCredentialsBody());
    return Constants.BEARER
        .concat(" ")
        .concat(
            (String)
                ObjectMapperUtils.convertToMap(restResponse.getResponse().getBody().toString())
                    .get(Constants.ACCESS_TOKEN));
  }

  private Map<String, Object> getClientCredentialsBody() {
    Map<String, Object> body = Maps.newHashMap();
    body.put(Constants.CLIENT_ID, clientCredentials.getClientId());
    body.put(Constants.CLIENT_SECRET, clientCredentials.getClientSecret());
    body.put(Constants.GRANT_TYPE, Constants.CLIENT_CREDENTIALS);
    body.put(Constants.SCOPE, clientCredentials.getScope());
    return body;
  }
}
