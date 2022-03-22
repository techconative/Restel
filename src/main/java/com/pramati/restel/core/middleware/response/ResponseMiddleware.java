package com.pramati.restel.core.middleware.response;

import com.pramati.restel.core.http.RESTClient;
import com.pramati.restel.core.http.RESTResponse;
import com.pramati.restel.core.middleware.HttpMiddleware;

/**
 * Interface represents the request middleware to be used by the {@link RESTClient}.
 *
 * <p>Before making a request, the {@link RESTClient} passes the request through given list of
 * middlewares, thus any changes to the request can be achieved by adding a required middleware.
 */
public interface ResponseMiddleware extends HttpMiddleware<RESTResponse> {
  @Override
  RESTResponse process(RESTResponse response);
}
