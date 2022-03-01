package com.pramati.restel.core.http;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represent a REST API request
 *
 * @author kannanr
 */
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class RESTRequest {
    private String method;
    private String endpoint;
    private Map<String, Object> headers;
    private Map<String, Object> requestParams;
    private Object requestBody;

    public void addHeader(String key, Object value) {
        this.headers.put(key, value);
    }
}
