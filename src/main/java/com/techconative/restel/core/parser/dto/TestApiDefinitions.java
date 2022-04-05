package com.techconative.restel.core.parser.dto;

import java.util.List;
import java.util.Set;
import lombok.Data;

/** TestApiDefinitions type dto for sheet test_api_definitions */
@Data
public class TestApiDefinitions {

  private String apiUniqueName;
  private String dependsOn;
  private String apiDescription;
  private String requestUrl;
  private String requestMethod;
  private String requestHeaders;
  private String requestPathParams;
  private String requestQueryParams;
  private String requestBodyParams;
  private String requestPreCallHook;
  private String requestPostCallHook;
  private String expectedResponse;
  private String expectedResponseMatcher;
  private String expectedHeader;
  private String expectedHeaderMatcher;
  private List<String> acceptedStatusCodes;
  private Set<String> tags;
}
