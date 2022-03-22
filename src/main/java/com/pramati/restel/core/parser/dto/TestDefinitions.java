package com.pramati.restel.core.parser.dto;

import java.util.List;
import java.util.Set;
import lombok.Data;

/** TestDefinitions type dto for sheet test_definitions */
@Data
public class TestDefinitions {

  private String caseUniqueName;
  private String dependsOn;
  private String caseDescription;
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
