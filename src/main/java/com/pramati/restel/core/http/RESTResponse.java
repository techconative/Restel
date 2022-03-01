package com.pramati.restel.core.http;

import java.util.Map;

import lombok.Data;

/**
 * The response from the REST call.
 * 
 * @author kannanr
 *
 */
@Data
public class RESTResponse {

	private int status;

	private Map<String, Object> headers;

	private ResponseBody response;

}
