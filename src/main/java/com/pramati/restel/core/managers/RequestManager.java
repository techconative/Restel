package com.pramati.restel.core.managers;

import com.pramati.restel.core.http.RESTClient;
import com.pramati.restel.core.http.RESTRequest;
import com.pramati.restel.core.http.RESTResponse;
import com.pramati.restel.core.middleware.HttpMiddleware;
import com.pramati.restel.core.middleware.request.RequestMiddleware;
import com.pramati.restel.core.middleware.response.ResponseMiddleware;
import com.pramati.restel.utils.Reporter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * Implementation responsible for invoking a request, taking care of the
 * pre-processing and post-processing of the requests
 */
public class RequestManager {

    private RESTClient client;

    public RequestManager(String server) {
        client = new RESTClient(server);
    }

    /**
     * Makes a API call with the given parameters. Execute pre and post
     * middlewares before and after making the API calls, in the given order.
     *
     * @param request                The {@link RESTRequest} to be made.
     * @param preRequestMiddlewares  The list of {@link RequestMiddleware} to be executed in order.
     * @param postRequestMiddlewares The list of {@link ResponseMiddleware} to be executed in
     *                               order.
     * @return {@link RESTResponse} instance with the response for the given
     * call.
     */
    public RESTResponse makeCall(RESTRequest request,
                                 List<RequestMiddleware> preRequestMiddlewares,
                                 List<ResponseMiddleware> postRequestMiddlewares) {

        // Process the request with the list of available middlewares in the
        // given order
        RESTRequest processedRequest = performAll(preRequestMiddlewares,
                request);

        //attach to report
        Reporter.attachRequest(request);

        // Make the API call
        RESTResponse response = client.makeCall(processedRequest.getMethod(),
                processedRequest.getEndpoint(),
                processedRequest.getHeaders(),
                processedRequest.getRequestParams(),
                processedRequest.getRequestBody());

        // Process the response with the list of available middlewares in the
        // given order
        return performAll(postRequestMiddlewares,
                response);
    }

    /**
     * Applies all the middleware operations in the given order on the given
     * data that is to be processed by the middleware.
     *
     * @param <T>            The type of object to be processed.
     * @param middlewares    The List of middlewares to be invoked.
     * @param middlewareData The data to be processed by the middlewares.
     * @return The middleware data after all the middleware operations being
     * applied in order.
     */
    private <T> T performAll(List<? extends HttpMiddleware<T>> middlewares,
                             T middlewareData) {

        if (CollectionUtils.isEmpty(middlewares)) {
            return middlewareData;
        }

        T data = middlewareData;

        for (HttpMiddleware<T> httpMiddleware : middlewares) {
            data = httpMiddleware.process(data);
        }

        return data;
    }
}
