package com.techconative.restel.core.middleware.request;

import com.techconative.restel.core.http.RESTRequest;
import com.techconative.restel.core.model.oauth.BasicAuth;
import com.techconative.restel.exception.RestelException;
import com.techconative.restel.utils.Constants;
import java.util.Base64;
import javax.ws.rs.core.HttpHeaders;

/** Creates an Authorization token and adds in the request headers. */
public class BasicAuthMiddleware implements RequestMiddleware {

  private final BasicAuth basicAuth;

  public BasicAuthMiddleware(BasicAuth basicAuth) {
    this.basicAuth = basicAuth;
  }

  @Override
  public RESTRequest process(RESTRequest request) {
    try {
      String auth = basicAuth.getUsername() + ":" + basicAuth.getPassword();
      byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
      request.addHeader(
          HttpHeaders.AUTHORIZATION, Constants.BASIC.concat(" ").concat(new String(encodedAuth)));
      return request;
    } catch (Exception ex) {
      throw new RestelException(ex, "OAUTH_FAILURE", basicAuth);
    }
  }
}
