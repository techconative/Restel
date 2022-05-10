package com.techconative.restel.core.managers;

import static com.techconative.restel.core.utils.ContextUtils.replaceContextVariables;

import com.fasterxml.jackson.databind.JsonNode;
import com.techconative.restel.core.http.RESTRequest;
import com.techconative.restel.core.http.RESTResponse;
import com.techconative.restel.core.middleware.request.BasicAuthMiddleware;
import com.techconative.restel.core.middleware.request.Oauth2ClientCredentialMiddleware;
import com.techconative.restel.core.middleware.request.Oauth2ResourceOwnerMiddleware;
import com.techconative.restel.core.middleware.request.RequestMiddleware;
import com.techconative.restel.core.middleware.response.ResponseMiddleware;
import com.techconative.restel.core.middleware.response.ResponseWriterMiddleware;
import com.techconative.restel.core.model.*;
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

/** */
@Slf4j
public class RestelDefinitionManager {

  private RequestManager requestManager;
  private MatcherFactory matcherFactory;
  private List<RestelApiDefinition> testDefinitions;

  private TestContext testContext;

  public RestelDefinitionManager(
      List<RestelApiDefinition> testDefinitions,
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

    for (RestelApiDefinition restelTestMethod : testDefinitions) {
      boolean result = executeTestMethod(scenarioName, suiteName, restelTestMethod);
      if (!result) {
        return result;
      }
    }
    return true;
  }

  private boolean executeTestMethod(
      String scenarioName, String suiteName, RestelApiDefinition restelTestMethod) {

    TestContext apiContext = new TestContext(restelTestMethod.getApiUniqueName(), testContext);
    if (restelTestMethod.getApiParameters() != null) {
      apiContext.putAll(restelTestMethod.getApiParameters());
    }
    // Prepare the request object
    RESTRequest request = createRequest(restelTestMethod);

    // add test level context params

    // Populate the request to context, so that it can be referenced in
    // other test
    populateRequestToContext(apiContext, request);

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
    populateResponseToContext(apiContext, response);

    // validate response status
    validateStatus(response, restelTestMethod, apiContext);

    // Test if the header matches as per the config
    List<ResponseComparator> headerMatchers = getHeaderMatchers(restelTestMethod);
    boolean isHeaderMatched =
        doMatching(
            headerMatchers,
            response.getHeaders(),
            getExpectedHeaders(restelTestMethod, apiContext));
    log.info("Headers matched for the response of " + scenarioName + ":" + isHeaderMatched);

    if (!isHeaderMatched) {
      return false;
    }

    // Test if the body matches as per the config
    List<ResponseComparator> responseMatchers = getResponseMatchers(restelTestMethod);
    boolean isBodyMatched =
        doMatching(responseMatchers, response, getExpectedBody(restelTestMethod, apiContext));
    log.info(
        "Response content matched for the response of " + scenarioName + ":" + ":" + isBodyMatched);

    return isBodyMatched;
  }

  private void validateStatus(
      RESTResponse response, RestelApiDefinition restelTestMethod, TestContext apiContext) {
    List<String> expectedStatus =
        (List<String>)
            replaceContextVariables(apiContext, restelTestMethod.getAcceptedStatusCodes());
    if (!expectedStatus.contains(String.valueOf(response.getStatus()))) {
      Assert.fail(
          "Invalid Response Status Code: "
              .concat(String.valueOf(response.getStatus()))
              .concat(" must be one of ".concat(expectedStatus.toString())));
    }
  }

  /**
   * Gets the expected body for the given test name.
   *
   * @param restelTestMethod
   * @param apiContext
   * @return The expected response object.
   */
  private Object getExpectedBody(RestelApiDefinition restelTestMethod, TestContext apiContext) {
    // Check if expected body is Json type
    if (Objects.isNull(restelTestMethod.getExpectedResponse())) {
      return replaceContextVariables(apiContext, restelTestMethod.getExpectedResponse());
    }
    if (ObjectMapperUtils.isJSONValid(restelTestMethod.getExpectedResponse().toString())) {
      boolean isArray = Utils.isArray(restelTestMethod.getExpectedResponse().toString());
      if (!isArray) {
        return replaceContextVariables(
            apiContext,
            ObjectMapperUtils.convertToMap(restelTestMethod.getExpectedResponse().toString()));
      } else {
        return ObjectMapperUtils.convertToArray(
            replaceContextVariables(apiContext, restelTestMethod.getExpectedResponse()).toString());
      }
    }

    return replaceContextVariables(apiContext, restelTestMethod.getExpectedResponse());
  }

  /**
   * Creates the {@link RESTRequest} instance based on the values for the given test name.
   *
   * @return {@link RESTRequest} instance corresponding to the given test name.
   * @param restelTestMethod
   */
  private RESTRequest createRequest(RestelApiDefinition restelTestMethod) {
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
   * @param restelTestMethod
   * @param apiContext
   * @return The expected response object.
   */
  private Map<String, Object> getExpectedHeaders(
      RestelApiDefinition restelTestMethod, TestContext apiContext) {
    if (CollectionUtils.isEmpty(restelTestMethod.getExpectedHeader())) {
      return null;
    }
    return replaceContextVariables(apiContext, restelTestMethod.getExpectedHeader());
  }

  /**
   * Gets the request parameters to be sent for the API, with the variables resolved.
   *
   * @return The request headers map.
   * @param restelTestMethod
   */
  private Map<String, Object> getRequestQueryParams(RestelApiDefinition restelTestMethod) {
    if (CollectionUtils.isEmpty(restelTestMethod.getRequestQueryParams())) {
      return restelTestMethod.getRequestQueryParams();
    }
    return replaceContextVariables(testContext, restelTestMethod.getRequestQueryParams());
  }

  /**
   * Gets the headers to be sent for the API, with the variables resolved.
   *
   * @return The request headers map.
   * @param restelTestMethod
   */
  private Map<String, Object> getRequestHeaders(RestelApiDefinition restelTestMethod) {
    if (CollectionUtils.isEmpty(restelTestMethod.getRequestHeaders())) {
      return restelTestMethod.getRequestHeaders();
    }
    return replaceContextVariables(testContext, restelTestMethod.getRequestHeaders());
  }

  /**
   * Gets the body to be sent for the API, with the variables resolved.
   *
   * @return The request headers map.
   * @param restelTestMethod
   */
  private Object getRequestBody(RestelApiDefinition restelTestMethod) {
    if (Objects.isNull(restelTestMethod.getRequestBodyParams())) {
      return restelTestMethod.getRequestBodyParams();
    }

    // Check if request body is Json type
    if (ObjectMapperUtils.isJSONValid(restelTestMethod.getRequestBodyParams().toString())) {
      return replaceContextVariables(
          testContext,
          ObjectMapperUtils.convertToMap(restelTestMethod.getRequestBodyParams().toString()));
    }

    return replaceContextVariables(testContext, restelTestMethod.getRequestBodyParams());
  }

  /**
   * Gets the request url, with the variables resolved.
   *
   * @return The expected response object.
   * @param restelTestMethod
   */
  private String getRequestURL(RestelApiDefinition restelTestMethod) {
    return replaceContextVariables(testContext, restelTestMethod.getRequestUrl()).toString();
  }

  /**
   * Get the list of matchers that does the response body matching.
   *
   * @param restelTestMethod
   * @return List of {@link ResponseComparator} instances for the given test name.
   */
  private List<ResponseComparator> getResponseMatchers(RestelApiDefinition restelTestMethod) {
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
   * @param restelTestMethod The method to get headers for.
   * @return The list of matchers for the header as configured in the test.
   */
  private List<ResponseComparator> getHeaderMatchers(RestelApiDefinition restelTestMethod) {
    return Collections.singletonList(getMatcher(restelTestMethod.getExpectedHeaderMatcher()));
  }

  /**
   * Populates the given response to the context.
   *
   * @param apiContext The context to which the response to be populated. Typically, the test api
   *     context.
   * @param response The response object to be populated to the context.
   */
  private void populateResponseToContext(TestContext apiContext, RESTResponse response) {
    if (response.getResponse() != null) {
      if (!Objects.isNull(response.getResponse().getBody())) {
        if (StringUtils.isNotEmpty(response.getResponse().getBody().toString())) {

          Object opToStore = response.getResponse().getBody();
          if (ObjectMapperUtils.isJSONValid(opToStore.toString())) {
            opToStore = ObjectMapperUtils.convertToMap(opToStore.toString());
          }
          apiContext.addValue(Constants.RESPONSE, opToStore);
        }
      }
    }
  }

  /**
   * Gets the list of middlewares to be executed after the API execution.
   *
   * @return List of {@link ResponseMiddleware} instances configured for the given test.
   * @param restelTestMethod
   */
  private List<ResponseMiddleware> getPostRequestMiddlewares(RestelApiDefinition restelTestMethod) {
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
  private List<RequestMiddleware> getPreRequestMiddlewares(RestelApiDefinition restelTestMethod) {
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
        throw new RestelException(ex, "PRE_HOOKS_ERROR", restelTestMethod.getApiUniqueName());
      }
    }
    return middlewares;
  }

  /**
   * Populates the request to the context for the given test name
   *
   * @param apiContext The context to which the response to be populated. Typically, the test api *
   *     context.
   * @param request The request for the given test.
   */
  private void populateRequestToContext(TestContext apiContext, RESTRequest request) {
    if (request.getRequestBody() != null) {
      apiContext.addValue(Constants.REQUEST, request.getRequestBody());
    }
  }
}
