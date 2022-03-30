package com.techconative.restel.core.managers;

import com.fasterxml.jackson.databind.JsonNode;
import com.techconative.restel.core.http.RESTRequest;
import com.techconative.restel.core.http.RESTResponse;
import com.techconative.restel.core.middleware.request.BasicAuthMiddleware;
import com.techconative.restel.core.middleware.request.Oauth2ClientCredentialMiddleware;
import com.techconative.restel.core.middleware.request.Oauth2ResourceOwnerMiddleware;
import com.techconative.restel.core.middleware.request.RequestMiddleware;
import com.techconative.restel.core.middleware.response.ResponseMiddleware;
import com.techconative.restel.core.middleware.response.ResponseWriterMiddleware;
import com.techconative.restel.core.model.GlobalContext;
import com.techconative.restel.core.model.RestelTestMethod;
import com.techconative.restel.core.model.TestContext;
import com.techconative.restel.core.model.comparator.ResponseComparator;
import com.techconative.restel.core.model.oauth.BasicAuth;
import com.techconative.restel.core.model.oauth.ClientCredentials;
import com.techconative.restel.core.model.oauth.ResourceOwnerPassword;
import com.techconative.restel.exception.InvalidConfigException;
import com.techconative.restel.exception.RestelException;
import com.techconative.restel.testng.MatcherFactory;
import com.techconative.restel.utils.*;
import java.util.*;
import javax.ws.rs.core.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.testng.Assert;
import org.testng.collections.Maps;

/** */
@Slf4j
public class RestelDefinitionManager {

  private RequestManager requestManager;
  private MatcherFactory matcherFactory;
  private List<RestelTestMethod> testDefinitions;

  private ContextManager contextManager = new ContextManager();

  private TestContext testContext;

  public RestelDefinitionManager(
      List<RestelTestMethod> testDefinitions,
      RequestManager requestManager,
      MatcherFactory matcherFactory,
      TestContext testContext) {
    this.testDefinitions = testDefinitions;
    this.requestManager = requestManager;
    this.matcherFactory = matcherFactory;
    this.testContext = testContext;
  }

  /**
   * Make the API call and execute the test corresponding to the given test name.
   *
   * @return true when the test passes. False otherwise
   */
  public boolean executeTestScenario(String scenarioName, String suiteName) {

    // TODO: Add support to execute dependents
    //    executeDependents(scenarioName, suiteName);

    for (RestelTestMethod restelTestMethod : testDefinitions) {
      boolean result = executeTestMethod(scenarioName, suiteName, restelTestMethod);
      if (!result) {
        return result;
      }
    }
    return true;
  }

  private boolean executeTestMethod(
      String scenarioName, String suiteName, RestelTestMethod restelTestMethod) {
    // Prepare the request object
    RESTRequest request = createRequest(restelTestMethod);

    // Populate the request to context, so that it can be referenced in
    // other test
    populateRequestToContext(request, scenarioName, suiteName, restelTestMethod);

    // Make the API call and get the response
    RESTResponse response =
        requestManager.makeCall(
            request,
            getPreRequestMiddlewares(restelTestMethod),
            getPostRequestMiddlewares(restelTestMethod));

    // Attach to report
    Reporter.attachResponse(request.getEndpoint(), response);

    // Populate the response to context, so that it can be referenced in
    // other test
    populateResponseToContext(response, restelTestMethod, scenarioName, suiteName);

    // validate response status
    validateStatus(response, restelTestMethod);

    // Test if the header matches as per the config
    List<ResponseComparator> headerMatchers = getHeaderMatchers(restelTestMethod);
    boolean isHeaderMatched =
        doMatching(headerMatchers, response.getHeaders(), getExpectedHeaders(restelTestMethod));
    log.info("Headers matched for the response of " + scenarioName + ":" + isHeaderMatched);

    if (!isHeaderMatched) {
      return false;
    }

    // Test if the body matches as per the config
    List<ResponseComparator> responseMatchers = getResponseMatchers(restelTestMethod);
    boolean isBodyMatched =
        doMatching(responseMatchers, response, getExpectedBody(restelTestMethod));
    log.info(
        "Response content matched for the response of " + scenarioName + ":" + ":" + isBodyMatched);

    return isBodyMatched;
  }

  private void validateStatus(RESTResponse response, RestelTestMethod restelTestMethod) {
    if (!restelTestMethod.getAcceptedStatusCodes().contains(response.getStatus())) {
      Assert.fail(
          "Invalid Response Status Code: "
              .concat(String.valueOf(response.getStatus()))
              .concat(
                  " must be one of ".concat(restelTestMethod.getAcceptedStatusCodes().toString())));
    }
  }

  //  /**
  //   * @param scenarioName Test scenario name
  //   * @param suiteName Test suite name
  //   */
  //  private void executeDependents(String scenarioName, String suiteName) {
  //    if (testDefinition.getDependentOn() != null) {
  //      testDefinition
  //          .getDependentOn()
  //          .forEach(
  //              testcase -> {
  //                // pass on the same textContext so that request and response body can be stored
  // and
  //                // reused.
  //                RestelDefinitionManager manager =
  //                    new RestelDefinitionManager(
  //                        testcase, requestManager, matcherFactory, testContext);
  //                if (!manager.executeTestScenario(scenarioName, suiteName)) {
  //                  Allure.step(
  //                      "Execution failed for testcase: "
  //                          + scenarioName
  //                          + " for dependent case: "
  //                          + testcase.getCaseUniqueName());
  //                }
  //              });
  //    }
  //  }

  /**
   * Gets the expected body for the given test name.
   *
   * @return The expected response object.
   * @param restelTestMethod
   */
  private Object getExpectedBody(RestelTestMethod restelTestMethod) {
    // Check if expected body is Json type
    if (Objects.isNull(restelTestMethod.getExpectedResponse())) {
      return contextManager.replaceContextVariables(
          testContext, restelTestMethod.getExpectedResponse());
    }
    if (ObjectMapperUtils.isJSONValid(restelTestMethod.getExpectedResponse().toString())) {
      boolean isArray = Utils.isArray(restelTestMethod.getExpectedResponse().toString());
      if (!isArray) {
        return contextManager.replaceContextVariables(
            testContext,
            ObjectMapperUtils.convertToMap(restelTestMethod.getExpectedResponse().toString()));
      } else {
        return ObjectMapperUtils.convertToArray(
            contextManager
                .replaceContextVariables(testContext, restelTestMethod.getExpectedResponse())
                .toString());
      }
    }

    return contextManager.replaceContextVariables(
        testContext, restelTestMethod.getExpectedResponse());
  }

  /**
   * Creates the {@link RESTRequest} instance based on the values for the given test name.
   *
   * @return {@link RESTRequest} instance corresponding to the given test name.
   * @param restelTestMethod
   */
  private RESTRequest createRequest(RestelTestMethod restelTestMethod) {
    return RESTRequest.builder()
        .method(restelTestMethod.getRequestMethod())
        .endpoint(getRequestURL(restelTestMethod))
        .headers(getRequestHeaders(restelTestMethod))
        .requestParams(getRequestQueryParams(restelTestMethod))
        .requestBody(getRequestBody(restelTestMethod))
        .build();
  }

  /**
   * Gets the expected headers for the given test name.
   *
   * @return The expected response object.
   * @param restelTestMethod
   */
  private Map<String, Object> getExpectedHeaders(RestelTestMethod restelTestMethod) {
    if (CollectionUtils.isEmpty(restelTestMethod.getExpectedHeader())) {
      return null;
    }
    return contextManager.replaceContextVariables(
        testContext, restelTestMethod.getExpectedHeader());
  }

  /**
   * Gets the request parameters to be sent for the API, with the variables resolved.
   *
   * @return The request headers map.
   * @param restelTestMethod
   */
  private Map<String, Object> getRequestQueryParams(RestelTestMethod restelTestMethod) {
    if (CollectionUtils.isEmpty(restelTestMethod.getRequestQueryParams())) {
      return restelTestMethod.getRequestQueryParams();
    }
    return contextManager.replaceContextVariables(
        testContext, restelTestMethod.getRequestQueryParams());
  }

  /**
   * Gets the headers to be sent for the API, with the variables resolved.
   *
   * @return The request headers map.
   * @param restelTestMethod
   */
  private Map<String, Object> getRequestHeaders(RestelTestMethod restelTestMethod) {
    if (CollectionUtils.isEmpty(restelTestMethod.getRequestHeaders())) {
      return restelTestMethod.getRequestHeaders();
    }
    return contextManager.replaceContextVariables(
        testContext, restelTestMethod.getRequestHeaders());
  }

  /**
   * Gets the body to be sent for the API, with the variables resolved.
   *
   * @return The request headers map.
   * @param restelTestMethod
   */
  private Object getRequestBody(RestelTestMethod restelTestMethod) {
    if (Objects.isNull(restelTestMethod.getRequestBodyParams())) {
      return restelTestMethod.getRequestBodyParams();
    }

    // Check if request body is Json type
    if (ObjectMapperUtils.isJSONValid(restelTestMethod.getRequestBodyParams().toString())) {
      return contextManager.replaceContextVariables(
          testContext,
          ObjectMapperUtils.convertToMap(restelTestMethod.getRequestBodyParams().toString()));
    }

    return contextManager.replaceContextVariables(
        testContext, restelTestMethod.getRequestBodyParams());
  }

  /**
   * Gets the request url, with the variables resolved.
   *
   * @return The expected response object.
   * @param restelTestMethod
   */
  private String getRequestURL(RestelTestMethod restelTestMethod) {
    return contextManager
        .replaceContextVariables(testContext, restelTestMethod.getRequestUrl())
        .toString();
  }

  /**
   * Get the list of matchers that does the response body matching.
   *
   * @return List of {@link ResponseComparator} instances for the given test name.
   * @param restelTestMethod
   */
  private List<ResponseComparator> getResponseMatchers(RestelTestMethod restelTestMethod) {
    return Collections.singletonList(getMatcher(restelTestMethod.getExpectedResponseMatcher()));
  }

  /**
   * Gets the matcher with the given name.
   *
   * @param matcherName The matcher name.
   * @return The matcher instance of {@link ResponseComparator} with the given name.
   * @throws InvalidConfigException When no matcher with the given name found.
   */
  private ResponseComparator getMatcher(String matcherName) {
    ResponseComparator matcher = matcherFactory.getMatcher(matcherName);

    if (matcher == null) {
      log.error(MessageUtils.getString("MATCHER_INVALID"));
      throw new InvalidConfigException("MATCHER_INVALID");
    }

    return matcher;
  }

  /**
   * Executes the given matchers on the give response against the expected response.
   *
   * @param matchers The list of matchers using which the headers of the response will be compared
   *     against
   * @param object The part response object form the API call, which has to be taken for matching.
   * @return true if the actual response matches as per the definition of 'all' matchers provided.
   *     false otherwise.
   */
  private boolean doMatching(
      List<ResponseComparator> matchers, Object object, Object expectedResponse) {

    if (CollectionUtils.isEmpty(matchers)) {
      return true;
    }

    return matchers.stream().allMatch(m -> m.compare(object, expectedResponse));
  }

  /**
   * Gets the list of header matchers as configured in the test.
   *
   * @return The list of matchers for the header as configured in the test.
   * @param restelTestMethod
   */
  private List<ResponseComparator> getHeaderMatchers(RestelTestMethod restelTestMethod) {
    return Collections.singletonList(getMatcher(restelTestMethod.getExpectedHeaderMatcher()));
  }

  /**
   * Populates the given response to the context.
   *
   * @param response The response object to be populated to the context.
   * @param restelTestMethod
   */
  private void populateResponseToContext(
      RESTResponse response, RestelTestMethod restelTestMethod, String testName, String suiteName) {
    if (response.getResponse() != null) {
      if (!Objects.isNull(response.getResponse().getBody())) {
        if (StringUtils.isNotEmpty(response.getResponse().getBody().toString())) {
          Map<String, Object> updatedContext =
              getResponseUpdatedContext(response, restelTestMethod);
          testContext.addValue(restelTestMethod.getCaseUniqueName(), updatedContext);
          addToGlobalContext(
              suiteName, testName, restelTestMethod.getCaseUniqueName(), updatedContext);
        }
      }
    }
  }

  private Map<String, Object> getResponseUpdatedContext(
      RESTResponse response, RestelTestMethod restelTestMethod) {
    Map<String, Object> existingContextMap;
    Object body = response.getResponse().getBody();
    Object existingContextVal = testContext.resolveValue(restelTestMethod.getCaseUniqueName());
    if (existingContextVal != null) {
      existingContextMap = Maps.newHashMap((Map<String, Object>) existingContextVal);
      if (ObjectMapperUtils.isJSONValid(body.toString())) {
        existingContextMap.put(Constants.RESPONSE, ObjectMapperUtils.convertToMap(body.toString()));
      } else {
        existingContextMap.put(Constants.RESPONSE, body);
      }
    } else {
      if (ObjectMapperUtils.isJSONValid(body.toString())) {
        body =
            Utils.isArray(body.toString())
                ? ObjectMapperUtils.convertToArray(body.toString())
                : ObjectMapperUtils.convertToMap(body.toString());
      }
      existingContextMap = Maps.newHashMap(Map.of(Constants.RESPONSE, body));
    }
    return existingContextMap;
  }

  /**
   * @param suiteName Test suite name.
   * @param scenarioName Test suite execution name.
   * @param testName test definition name.
   * @param response response body
   */
  private void addToGlobalContext(
      String suiteName, String scenarioName, String testName, Map<String, Object> response) {
    GlobalContext globalContext = GlobalContext.getInstance();
    Map<String, Object> testValue = Maps.newHashMap(Map.of(testName, response));
    Object scenarioContext = globalContext.getContextValues().get(scenarioName);
    if (Objects.isNull(scenarioContext)) {
      scenarioContext = new HashMap<>();
    }

    // If the testName params are already present for child test include the parent testDefinition
    // params in to the List.
    Map<String, Object> testRes = (Map) scenarioContext;
    testRes.put(testName, response);
    globalContext.addValue(scenarioName, testRes);

    if (Objects.isNull(globalContext.getContextValues().get(suiteName))) {
      globalContext.addValue(
          suiteName,
          new ArrayList<>(
              Collections.singletonList(
                  Maps.newHashMap(
                      Map.of(scenarioName, globalContext.getContextValues().get(scenarioName))))));
    } else {
      // If suite param are already present for child testName include additional params from parent
      // testName.
      List<Map<String, Object>> suiteRes = (List) globalContext.getContextValues().get(suiteName);
      suiteRes.add(
          Maps.newHashMap(
              Map.of(scenarioName, globalContext.getContextValues().get(scenarioName))));
      globalContext.addValue(suiteName, suiteRes);
    }
  }

  /**
   * Gets the list of middlewares to be executed after the API execution.
   *
   * @return List of {@link ResponseMiddleware} instances configured for the given test.
   * @param restelTestMethod
   */
  private List<ResponseMiddleware> getPostRequestMiddlewares(RestelTestMethod restelTestMethod) {
    List<ResponseMiddleware> middlewares = new ArrayList<>();
    if (!Objects.isNull(restelTestMethod.getRequestPostCallHook())) {
      String path = restelTestMethod.getRequestPostCallHook().get(Constants.WRITE).toString();
      if (StringUtils.isNotEmpty(path)) {
        middlewares.add(new ResponseWriterMiddleware(path));
      }
    }
    return middlewares;
  }

  /**
   * Gets the list of middlewares to be executed before the API execution.
   *
   * @return List of {@link ResponseMiddleware} instances configured for the given test.
   * @param restelTestMethod
   */
  private List<RequestMiddleware> getPreRequestMiddlewares(RestelTestMethod restelTestMethod) {
    List<RequestMiddleware> middlewares = new ArrayList<>();
    if (!Objects.isNull(restelTestMethod.getRequestPreCallHook())) {
      try {
        JsonNode auth = restelTestMethod.getRequestPreCallHook().get(HttpHeaders.AUTHORIZATION);
        if (!Objects.isNull(auth)) {
          JsonNode oauth2 = auth.get(Constants.OAUTH2);
          if (!Objects.isNull(auth.get(Constants.BASIC_AUTH))) {
            BasicAuth clientCredentials =
                ObjectMapperUtils.getMapper()
                    .convertValue(auth.get(Constants.BASIC_AUTH), BasicAuth.class);
            middlewares.add(new BasicAuthMiddleware(clientCredentials));
          } else if (!Objects.isNull(oauth2)) {
            if (!Objects.isNull(oauth2.get(Constants.CLIENT_CREDENTIALS))) {
              ClientCredentials clientCredentials =
                  ObjectMapperUtils.getMapper()
                      .convertValue(
                          oauth2.get(Constants.CLIENT_CREDENTIALS), ClientCredentials.class);
              middlewares.add(new Oauth2ClientCredentialMiddleware(clientCredentials));
            } else if (!Objects.isNull(oauth2.get(Constants.PASSWORD))) {
              ResourceOwnerPassword clientCredentials =
                  ObjectMapperUtils.getMapper()
                      .convertValue(oauth2.get(Constants.PASSWORD), ResourceOwnerPassword.class);
              middlewares.add(new Oauth2ResourceOwnerMiddleware(clientCredentials));
            }
          }
        }
      } catch (Exception ex) {
        throw new RestelException(ex, "PRE_HOOKS_ERROR", restelTestMethod.getCaseUniqueName());
      }
    }
    return middlewares;
  }

  /**
   * Populates the request to the context for the given test name
   *
   * @param request The request for the given test.
   * @param restelTestMethod
   */
  private void populateRequestToContext(
      RESTRequest request, String testName, String suiteName, RestelTestMethod restelTestMethod) {
    if (request.getRequestBody() != null) {
      Map<String, Object> reqMap = Map.of(Constants.REQUEST, request.getRequestBody());
      testContext.addValue(restelTestMethod.getCaseUniqueName(), reqMap);
      addToGlobalContext(suiteName, testName, restelTestMethod.getCaseUniqueName(), reqMap);
    }
  }
}
