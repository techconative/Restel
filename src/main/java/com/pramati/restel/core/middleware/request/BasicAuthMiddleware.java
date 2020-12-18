package com.pramati.restel.core.middleware.request;

import com.pramati.restel.core.http.RESTRequest;
import com.pramati.restel.core.model.oauth.BasicAuth;
import com.pramati.restel.exception.RestelException;
import com.pramati.restel.utils.Constants;

import javax.ws.rs.core.HttpHeaders;
import java.util.Base64;

/**
 * Creates an Authorization token and adds in the request headers.
 */
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
            request.addHeader(HttpHeaders.AUTHORIZATION, Constants.BASIC.concat(" ").concat(new String(encodedAuth)));
            return request;
        } catch (Exception ex) {
            throw new RestelException(ex, "OAUTH_FAILURE", basicAuth);
        }
    }

}
