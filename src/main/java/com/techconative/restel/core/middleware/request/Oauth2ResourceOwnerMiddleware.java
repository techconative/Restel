package com.techconative.restel.core.middleware.request;

import com.techconative.restel.core.http.RESTClient;
import com.techconative.restel.core.http.RESTRequest;
import com.techconative.restel.core.http.RESTResponse;
import com.techconative.restel.core.model.oauth.ResourceOwnerPassword;
import com.techconative.restel.exception.RestelException;
import com.techconative.restel.utils.Constants;
import com.techconative.restel.utils.ObjectMapperUtils;
import java.util.Map;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.testng.collections.Maps;

/** Creates an Authorization token and adds in the request headers. */
public class Oauth2ResourceOwnerMiddleware implements RequestMiddleware {

  private final ResourceOwnerPassword resourceOwnerPassword;

  public Oauth2ResourceOwnerMiddleware(ResourceOwnerPassword resourceOwnerPassword) {
    this.resourceOwnerPassword = resourceOwnerPassword;
  }

  @Override
  public RESTRequest process(RESTRequest request) {
    try {
      request.addHeader(HttpHeaders.AUTHORIZATION, fetchAccessToken());
    } catch (Exception ex) {
      throw new RestelException(ex, "OAUTH_FAILURE", resourceOwnerPassword);
    }
    return request;
  }

  private String fetchAccessToken() {
    RESTClient client = new RESTClient(resourceOwnerPassword.getAuthUrl());
    Map<String, Object> headers = Maps.newHashMap();
    headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
    RESTResponse restResponse =
        client.makeCall(HttpMethod.POST, "", headers, Maps.newHashMap(), getCredentialsBody());
    return Constants.BEARER
        .concat(" ")
        .concat(
            (String)
                ObjectMapperUtils.convertToMap(restResponse.getResponse().getBody().toString())
                    .get(Constants.ACCESS_TOKEN));
  }

  private Map<String, Object> getCredentialsBody() {
    Map<String, Object> body = Maps.newHashMap();
    body.put(Constants.CLIENT_ID, resourceOwnerPassword.getClientId());
    body.put(Constants.CLIENT_SECRET, resourceOwnerPassword.getClientSecret());
    body.put(Constants.GRANT_TYPE, Constants.PASSWORD);
    body.put(Constants.SCOPE, resourceOwnerPassword.getScope());
    body.put(Constants.USERNAME, resourceOwnerPassword.getUsername());
    body.put(Constants.PASSWORD, resourceOwnerPassword.getPassword());
    return body;
  }
}
