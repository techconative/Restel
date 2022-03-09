package com.techconative.restel.core.model.comparator;

import com.techconative.restel.core.http.RESTResponse;

/**
 * Contract defining the comparator that compares the rest response with the
 * expected output
 *
 * @author kannanr
 */
public interface ResponseComparator {
    /**
     * Compares the given response with the expected output based on the given
     * context.
     *
     * @param response       The part of response from the API to be compared.
     * @param expectedOutput The configured expected output.
     * @return true when the response is as expected. false otherwise.
     */
    public default boolean compare(Object response,
                                   Object expectedOutput) {
        if (response instanceof RESTResponse) {
            compareResponse((RESTResponse) response, expectedOutput);
        } else {
            compareHeader(response, expectedOutput);
        }
        return true;
    }

    public void compareResponse(RESTResponse restResponse, Object expectedOutput);

    public void compareHeader(Object headers, Object expectedHeaders);
}
