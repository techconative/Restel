package com.techconative.restel.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class RestelTestApiDefinition implements RestelApiDefinition {
  private String apiUniqueName;
  private String apiDescription;
  private String requestUrl;
  private String requestMethod;
  private Map<String, Object> requestHeaders;
  private Map<String, Object> requestQueryParams;
  private Object requestBodyParams;
  private JsonNode requestPreCallHook;
  private JsonNode requestPostCallHook;
  private Object expectedResponse;
  private String expectedResponseMatcher;
  private Map<String, Object> expectedHeader;
  private String expectedHeaderMatcher;
  private List<String> acceptedStatusCodes;
  @Deprecated private List<RestelApiDefinition> dependentOn;
  private List<String> parentTests = new ArrayList<>();

  @Override
  public Map<String, Object> getApiParameters() {
    return null;
  }

  @Override
  public void setApiParameters(Map<String, Object> apiParameters) {}

  @Deprecated
  public void addParentTest(String parentTest) {
    parentTests.add(parentTest);
  }
}
