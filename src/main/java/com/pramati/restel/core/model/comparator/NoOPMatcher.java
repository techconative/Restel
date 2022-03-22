package com.pramati.restel.core.model.comparator;

import com.pramati.restel.core.http.RESTResponse;
import org.springframework.stereotype.Component;

/**
 * Comparator that doesn't compares just returns true.
 *
 * @author kannanr
 */
@Component(value = "NOOP_MATCHER")
public class NoOPMatcher implements ResponseComparator {

  @Override
  public void compareResponse(RESTResponse response, Object expectedOutput) {
    // No comparison is required.
  }

  @Override
  public void compareHeader(Object response, Object expectedHeaders) {
    // No comparison is required.
  }
}
