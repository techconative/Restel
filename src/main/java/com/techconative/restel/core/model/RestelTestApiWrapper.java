package com.techconative.restel.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class RestelTestApiWrapper implements RestelApiDefinition {
  private RestelTestApiDefinition testApiDefinition;
  private String testApiWrapperName;
  private String testApiWrapperDescription;
  private Map<String, Object> apiParameters;

  @Override
  public String getCaseUniqueName() {
    return testApiWrapperName;
  }

  @Override
  public void setCaseUniqueName(String caseUniqueName) {
    this.testApiWrapperName = caseUniqueName;
  }

  @Override
  public String getCaseDescription() {
    return testApiWrapperDescription;
  }

  @Override
  public void setCaseDescription(String caseDescription) {
    this.testApiWrapperDescription = caseDescription;
  }

  @Override
  public String getRequestUrl() {
    return testApiDefinition.getRequestUrl();
  }

  @Override
  public void setRequestUrl(String requestUrl) {
    testApiDefinition.setRequestUrl(requestUrl);
  }

  @Override
  public String getRequestMethod() {
    return testApiDefinition.getRequestMethod();
  }

  @Override
  public void setRequestMethod(String requestMethod) {
    testApiDefinition.setRequestMethod(requestMethod);
  }

  @Override
  public Map<String, Object> getRequestHeaders() {
    return testApiDefinition.getRequestHeaders();
  }

  @Override
  public void setRequestHeaders(Map<String, Object> requestHeaders) {
    testApiDefinition.setRequestHeaders(requestHeaders);
  }

  @Override
  public Map<String, Object> getRequestQueryParams() {
    return testApiDefinition.getRequestQueryParams();
  }

  @Override
  public void setRequestQueryParams(Map<String, Object> requestQueryParams) {
    testApiDefinition.setRequestQueryParams(requestQueryParams);
  }

  @Override
  public Object getRequestBodyParams() {
    return testApiDefinition.getRequestBodyParams();
  }

  @Override
  public void setRequestBodyParams(Object requestBodyParams) {
    testApiDefinition.setRequestBodyParams(requestBodyParams);
  }

  @Override
  public JsonNode getRequestPreCallHook() {
    return testApiDefinition.getRequestPreCallHook();
  }

  @Override
  public void setRequestPreCallHook(JsonNode requestPreCallHook) {
    testApiDefinition.setRequestPreCallHook(requestPreCallHook);
  }

  @Override
  public JsonNode getRequestPostCallHook() {
    return testApiDefinition.getRequestPostCallHook();
  }

  @Override
  public void setRequestPostCallHook(JsonNode requestPostCallHook) {
    testApiDefinition.setRequestPostCallHook(requestPostCallHook);
  }

  @Override
  public Object getExpectedResponse() {
    return testApiDefinition.getExpectedResponse();
  }

  @Override
  public void setExpectedResponse(Object expectedResponse) {
    testApiDefinition.setExpectedResponse(expectedResponse);
  }

  @Override
  public String getExpectedResponseMatcher() {
    return testApiDefinition.getExpectedResponseMatcher();
  }

  @Override
  public void setExpectedResponseMatcher(String expectedResponseMatcher) {
    testApiDefinition.setExpectedResponseMatcher(expectedResponseMatcher);
  }

  @Override
  public Map<String, Object> getExpectedHeader() {
    return testApiDefinition.getExpectedHeader();
  }

  @Override
  public void setExpectedHeader(Map<String, Object> expectedHeader) {
    testApiDefinition.setExpectedHeader(expectedHeader);
  }

  @Override
  public String getExpectedHeaderMatcher() {
    return testApiDefinition.getExpectedHeaderMatcher();
  }

  @Override
  public void setExpectedHeaderMatcher(String expectedHeaderMatcher) {
    testApiDefinition.setExpectedHeaderMatcher(expectedHeaderMatcher);
  }

  @Override
  public List<String> getAcceptedStatusCodes() {
    return testApiDefinition.getAcceptedStatusCodes();
  }

  @Override
  public void setAcceptedStatusCodes(List<String> acceptedStatusCodes) {
    testApiDefinition.setAcceptedStatusCodes(acceptedStatusCodes);
  }

  @Override
  public List<RestelApiDefinition> getDependentOn() {
    return testApiDefinition.getDependentOn();
  }

  @Override
  public void setDependentOn(List<RestelApiDefinition> dependentOn) {
    testApiDefinition.setDependentOn(dependentOn);
  }

  @Override
  public List<String> getParentTests() {
    return testApiDefinition.getParentTests();
  }

  @Override
  public void setParentTests(List<String> parentTests) {
    testApiDefinition.setParentTests(parentTests);
  }

  @Override
  public void addParentTest(String parentTest) {
    testApiDefinition.addParentTest(parentTest);
  }
}
