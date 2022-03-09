package com.techconative.restel.core.middleware.request;

import com.techconative.restel.core.http.RESTClient;
import com.techconative.restel.core.http.RESTRequest;
import com.techconative.restel.core.middleware.HttpMiddleware;

/**
 * Interface represents the request middleware to be used by the
 * {@link RESTClient}.
 * <p>
 * Before making a request, the {@link RESTClient} passes the request through
 * given list of middlewares, thus any changes to the request can be achieved by
 * adding a required middleware.
 */
public interface RequestMiddleware extends HttpMiddleware<RESTRequest> {
    @Override
    RESTRequest process(RESTRequest request);
}
