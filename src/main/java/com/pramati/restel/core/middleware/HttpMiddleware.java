package com.pramati.restel.core.middleware;

import com.pramati.restel.core.http.RESTClient;

/**
 * Interface represents the request middleware to be used by the
 * {@link RESTClient}.
 * 
 * Before making a request, the {@link RESTClient} passes the request through
 * given list of middlewares, thus any changes to the request can be achieved by
 * adding a required middleware.
 * 
 * @author kannanr
 *
 */
public interface HttpMiddleware<T> {

	/**
	 * Process the request. The implementing middlewares are expected to make the
	 * changes to the request object passed to it.
	 * 
	 * @param request The request
	 */
	public T process(T request);

}
