package com.techconative.restel.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;

public interface RestelApiDefinition {
  String getCaseUniqueName();

  void setCaseUniqueName(String caseUniqueName);

  String getCaseDescription();

  void setCaseDescription(String caseDescription);

  String getRequestUrl();

  void setRequestUrl(String requestUrl);

  String getRequestMethod();

  void setRequestMethod(String requestMethod);

  Map<String, Object> getRequestHeaders();

  void setRequestHeaders(Map<String, Object> requestHeaders);

  Map<String, Object> getRequestQueryParams();

  void setRequestQueryParams(Map<String, Object> requestQueryParams);

  Object getRequestBodyParams();

  void setRequestBodyParams(Object requestBodyParams);

  JsonNode getRequestPreCallHook();

  void setRequestPreCallHook(JsonNode requestPreCallHook);

  JsonNode getRequestPostCallHook();

  void setRequestPostCallHook(JsonNode requestPostCallHook);

  Object getExpectedResponse();

  void setExpectedResponse(Object expectedResponse);

  String getExpectedResponseMatcher();

  void setExpectedResponseMatcher(String expectedResponseMatcher);

  Map<String, Object> getExpectedHeader();

  void setExpectedHeader(Map<String, Object> expectedHeader);

  String getExpectedHeaderMatcher();

  void setExpectedHeaderMatcher(String expectedHeaderMatcher);

  List<String> getAcceptedStatusCodes();

  void setAcceptedStatusCodes(List<String> acceptedStatusCodes);

  List<RestelApiDefinition> getDependentOn();

  void setDependentOn(List<RestelApiDefinition> dependentOn);

  List<String> getParentTests();

  void setParentTests(List<String> parentTests);

  Map<String, Object> getApiParameters();

  void setApiParameters(Map<String, Object> apiParameters);

  public void addParentTest(String parentTest);
}
