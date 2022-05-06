package com.techconative.restel.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * Represents a test as parsed from the excel sheet. <br>
 * <b>Note:</b> Each of this will be translated to a TestNG test method, smallest unit of TestNG.
 *
 * @author kannanr
 */
@Data
public class RestelTestMethod {
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

  @Deprecated
  private List<RestelTestMethod> dependentOn;
  private List<String> parentTests = new ArrayList<>();

  @Deprecated
  public void addParentTest(String parentTest) {
    parentTests.add(parentTest);
  }

}
