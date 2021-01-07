package com.pramati.restel.core.managers;

import com.fasterxml.jackson.databind.JsonNode;
import com.pramati.restel.core.http.RESTRequest;
import com.pramati.restel.core.http.RESTResponse;
import com.pramati.restel.core.middleware.request.BasicAuthMiddleware;
import com.pramati.restel.core.middleware.request.Oauth2ClientCredentialMiddleware;
import com.pramati.restel.core.middleware.request.Oauth2ResourceOwnerMiddleware;
import com.pramati.restel.core.middleware.request.RequestMiddleware;
import com.pramati.restel.core.middleware.response.ResponseMiddleware;
import com.pramati.restel.core.middleware.response.ResponseWriterMiddleware;
import com.pramati.restel.core.model.GlobalContext;
import com.pramati.restel.core.model.RestelTestMethod;
import com.pramati.restel.core.model.TestContext;
import com.pramati.restel.core.model.comparator.ResponseComparator;
import com.pramati.restel.core.model.oauth.BasicAuth;
import com.pramati.restel.core.model.oauth.ClientCredentials;
import com.pramati.restel.core.model.oauth.ResourceOwnerPassword;
import com.pramati.restel.exception.InvalidConfigException;
import com.pramati.restel.exception.RestelException;
import com.pramati.restel.testng.MatcherFactory;
import com.pramati.restel.utils.*;
import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.testng.Assert;
import org.testng.collections.Maps;

import javax.ws.rs.core.HttpHeaders;
import java.util.*;

/**
 *
 */
@Slf4j
public class RestelDefinitionManager {

    private RequestManager requestManager;
    private MatcherFactory matcherFactory;
    private RestelTestMethod testDefinition;

    private ContextManager contextManager = new ContextManager();

    private TestContext testContext;

    public RestelDefinitionManager(RestelTestMethod testDefinition, RequestManager requestManager, MatcherFactory matcherFactory, TestContext testContext) {
        this.testDefinition = testDefinition;
        this.requestManager = requestManager;
        this.matcherFactory = matcherFactory;
        this.testContext = testContext;
    }

    /**
     * Make the API call and execute the test corresponding to the given test
     * name.
     *
     * @return true when the test passes. False otherwise
     */
    public boolean executeTest(String testName, String suiteName) {

        executeDependents(testName, suiteName);
        // Prepare the request object
        RESTRequest request = createRequest();

        // Populate the request to context, so that it can be referenced in
        // other test
        populateRequestToContext(request, testName, suiteName);


        // Make the API call and get the response
        RESTResponse response = requestManager.makeCall(request,
                getPreRequestMiddlewares(),
                getPostRequestMiddlewares());

        //Attach to report
        Reporter.attachResponse(request.getEndpoint(), response);

        // Populate the response to context, so that it can be referenced in
        // other test
        populateResponseToContext(response, testName, suiteName);

        //validate response status
        validateStatus(response);

        // Test if the header matches as per the config
        List<ResponseComparator> headerMatchers = getHeaderMatchers();
        boolean isHeaderMatched = doMatching(headerMatchers, response.getHeaders(),
                getExpectedHeaders());
        log.info(
                "Headers matched for the response of "
                        + testName + ":"
                        + isHeaderMatched);

        if (!isHeaderMatched) {
            return false;
        }

        // Test if the body matches as per the config
        List<ResponseComparator> responseMatchers = getResponseMatchers();
        boolean isBodyMatched = doMatching(responseMatchers, response,
                getExpectedBody());
        log.info("Response content matched for the response of "
                + testName + ":"
                + ":" + isBodyMatched);


        return isBodyMatched;
    }

    private void validateStatus(RESTResponse response) {
        if (!testDefinition.getAcceptedStatusCodes().contains(response.getStatus())) {
            Assert.fail("Invalid Response Status Code: ".concat(String.valueOf(response.getStatus())).concat(" must be one of ".concat(testDefinition.getAcceptedStatusCodes().toString())));
        }
    }

    /**
     * @param testName  Test suite execution name
     * @param suiteName Test suite name
     */

    private void executeDependents(String testName, String suiteName) {
        if (testDefinition.getDependentOn() != null) {
            testDefinition.getDependentOn().forEach(testcase -> {
                //pass on the same textContext so that request and response body can be stored and reused.
                RestelDefinitionManager manager = new RestelDefinitionManager(testcase, requestManager, matcherFactory, testContext);
                if (!manager.executeTest(testName, suiteName)) {
                    Allure.step("Execution failed for testcase: " + testName + " for dependent case: " + testcase.getCaseUniqueName());
                }
            });
        }

    }

    /**
     * Gets the expected body for the given test name.
     *
     * @return The expected response object.
     */
    private Object getExpectedBody() {
        //Check if expected body is Json type
        if (Objects.isNull(testDefinition.getExpectedResponse())) {
            return contextManager
                    .replaceContextVariables(testContext,
                            testDefinition.getExpectedResponse());

        }
        if (ObjectMapperUtils.isJSONValid(testDefinition.getExpectedResponse().toString())) {
            return contextManager
                    .replaceContextVariables(testContext,
                            ObjectMapperUtils.convertToMap(testDefinition.getExpectedResponse().toString()));
        }

        return contextManager
                .replaceContextVariables(testContext,
                        testDefinition.getExpectedResponse());
    }

    /**
     * Creates the {@link RESTRequest} instance based on the values for the
     * given test name.
     *
     * @return {@link RESTRequest} instance corresponding to the given test
     * name.
     */
    private RESTRequest createRequest() {
        return RESTRequest.builder()
                .method(testDefinition.getRequestMethod())
                .endpoint(getRequestURL())
                .headers(getRequestHeaders())
                .requestParams(getRequestQueryParams())
                .requestBody(getRequestBody())
                .build();
    }

    /**
     * Gets the expected headers for the given test name.
     *
     * @return The expected response object.
     */
    private Map<String, Object> getExpectedHeaders() {
        if (CollectionUtils.isEmpty(testDefinition.getExpectedHeader())) {
            return null;
        }
        return contextManager
                .replaceContextVariables(testContext,
                        testDefinition.getExpectedHeader());
    }

    /**
     * Gets the request parameters to be sent for the API, with the variables
     * resolved.
     *
     * @return The request headers map.
     */
    private Map<String, Object> getRequestQueryParams() {
        if (CollectionUtils.isEmpty(testDefinition.getRequestQueryParams())) {
            return testDefinition.getRequestQueryParams();
        }
        return contextManager
                .replaceContextVariables(testContext,
                        testDefinition.getRequestQueryParams());
    }


    /**
     * Gets the headers to be sent for the API, with the variables resolved.
     *
     * @return The request headers map.
     */
    private Map<String, Object> getRequestHeaders() {
        if (CollectionUtils.isEmpty(testDefinition.getRequestHeaders())) {
            return testDefinition.getRequestHeaders();
        }
        return contextManager
                .replaceContextVariables(testContext,
                        testDefinition.getRequestHeaders());
    }


    /**
     * Gets the body to be sent for the API, with the variables resolved.
     *
     * @return The request headers map.
     */
    private Object getRequestBody() {
        if (Objects.isNull(testDefinition.getRequestBodyParams())) {
            return testDefinition.getRequestBodyParams();
        }

        //Check if request body is Json type
        if (ObjectMapperUtils.isJSONValid(testDefinition.getRequestBodyParams().toString())) {
            return contextManager
                    .replaceContextVariables(testContext,
                            ObjectMapperUtils.convertToMap(testDefinition.getRequestBodyParams().toString()));
        }

        return contextManager
                .replaceContextVariables(testContext,
                        testDefinition.getRequestBodyParams());
    }

    /**
     * Gets the request url, with the variables resolved.
     *
     * @return The expected response object.
     */
    private String getRequestURL() {
        return contextManager
                .replaceContextVariables(testContext,
                        testDefinition.getRequestUrl())
                .toString();
    }

    /**
     * Get the list of matchers that does the response body matching.
     *
     * @return List of {@link ResponseComparator} instances for the given test
     * name.
     */
    private List<ResponseComparator> getResponseMatchers() {
        return Collections.singletonList(getMatcher(testDefinition.getExpectedResponseMatcher()));
    }

    /**
     * Gets the matcher with the given name.
     *
     * @param matcherName The matcher name.
     * @return The matcher instance of {@link ResponseComparator} with the given
     * name.
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
     * Executes the given matchers on the give response against the expected
     * response.
     *
     * @param matchers The list of matchers using which the headers of the response
     *                 will be compared against
     * @param object   The part response object form the API call, which has to be taken
     *                 for matching.
     * @return true if the actual response matches as per the definition of
     * 'all' matchers provided. false otherwise.
     */
    private boolean doMatching(List<ResponseComparator> matchers,
                               Object object, Object expectedResponse) {

        if (CollectionUtils.isEmpty(matchers)) {
            return true;
        }

        return matchers.stream().allMatch(m -> m
                .compare(object, expectedResponse));
    }

    /**
     * Gets the list of header matchers as configured in the test.
     *
     * @return The list of matchers for the header as configured in the test.
     */
    private List<ResponseComparator> getHeaderMatchers() {
        return Collections.singletonList(getMatcher(testDefinition.getExpectedHeaderMatcher()));
    }

    /**
     * Populates the given response to the context.
     *
     * @param response The response object to be populated to the context.
     */
    private void populateResponseToContext(RESTResponse response, String testName, String suiteName) {
        if (response.getResponse() != null) {
            if (!Objects.isNull(response.getResponse().getBody())) {
                if (StringUtils.isNotEmpty(response.getResponse().getBody().toString())) {
                    Map<String, Object> responseBody = getResponseBody(response);
                    testContext.addValue(testDefinition.getCaseUniqueName(), responseBody);
                    appendResponseGlobalContext(suiteName, testName, testDefinition.getCaseUniqueName(), responseBody);
                }
            }
        }
    }

    private Map<String, Object> getResponseBody(RESTResponse response) {
        Map<String, Object> responseBody;
        Object body = response.getResponse().getBody();
        if (testContext.resolveValue(testDefinition.getCaseUniqueName()) != null) {
            responseBody = Maps.newHashMap((Map<String, Object>) testContext.resolveValue(testDefinition.getCaseUniqueName()));
            if (ObjectMapperUtils.isJSONValid(body.toString())) {
                responseBody.put(Constants.RESPONSE, ObjectMapperUtils.convertToMap(body.toString()));
            } else {
                responseBody.put(Constants.RESPONSE, body);
            }
        } else {
            if (ObjectMapperUtils.isJSONValid(body.toString())) {
                body = Utils.isArray(body.toString()) ? ObjectMapperUtils.convertToArray(body.toString()) : ObjectMapperUtils.convertToMap(body.toString());
            }
            responseBody = Maps.newHashMap(Map.of(Constants.RESPONSE, body));
        }
        return responseBody;
    }

    /**
     * @param suiteName          Test suite name.
     * @param testName           Test suite execution name.
     * @param testDefinitionName test definition name.
     * @param response           response body
     */
    private void appendResponseGlobalContext(String suiteName, String testName, String
            testDefinitionName, Map<String, Object> response) {
        GlobalContext instance = GlobalContext.getInstance();
        Map<String, Object> testValue = Maps.newHashMap(Map.of(testDefinitionName, response));
        if (Objects.isNull(instance.getContextValues().get(testName))) {
            instance.addValue(testName, new ArrayList<>(Collections.singletonList(testValue)));
        } else {
            // If the testName params are already present for child testDefinition include the parent testDefinition params in to the List.
            List<Map<String, Object>> testRes = (List) instance.getContextValues().get(testName);
            testRes.add(Maps.newHashMap(Map.of(testDefinitionName, response)));
            instance.addValue(testName, testRes);
        }
        if (Objects.isNull(instance.getContextValues().get(suiteName))) {
            instance.addValue(suiteName, new ArrayList<>(Collections.singletonList(Maps.newHashMap(Map.of(testName, instance.getContextValues().get(testName))))));
        } else {
            // If suite param are already present for child testName include additional params from parent testName.
            List<Map<String, Object>> suiteRes = (List) instance.getContextValues().get(suiteName);
            suiteRes.add(Maps.newHashMap(Map.of(testName, instance.getContextValues().get(testName))));
            instance.addValue(suiteName, suiteRes);

        }
    }

    /**
     * Gets the list of middlewares to be executed after the API execution.
     *
     * @return List of {@link ResponseMiddleware} instances configured for the
     * given test.
     */
    private List<ResponseMiddleware> getPostRequestMiddlewares() {
        List<ResponseMiddleware> middlewares = new ArrayList<>();
        if (!Objects.isNull(testDefinition.getRequestPostCallHook())) {
            String path = testDefinition.getRequestPostCallHook().get(Constants.WRITE).toString();
            if (StringUtils.isNotEmpty(path)) {
                middlewares.add(new ResponseWriterMiddleware(path));
            }
        }
        return middlewares;
    }

    /**
     * Gets the list of middlewares to be executed before the API execution.
     *
     * @return List of {@link ResponseMiddleware} instances configured for the
     * given test.
     */
    private List<RequestMiddleware> getPreRequestMiddlewares() {
        List<RequestMiddleware> middlewares = new ArrayList<>();
        if (!Objects.isNull(testDefinition.getRequestPreCallHook())) {
            try {
                JsonNode auth = testDefinition.getRequestPreCallHook().get(HttpHeaders.AUTHORIZATION);
                if (!Objects.isNull(auth)) {
                    JsonNode oauth2 = auth.get(Constants.OAUTH2);
                    if (!Objects.isNull(auth.get(Constants.BASIC_AUTH))) {
                        BasicAuth clientCredentials = ObjectMapperUtils.getMapper().convertValue(auth.get(Constants.BASIC_AUTH), BasicAuth.class);
                        middlewares.add(new BasicAuthMiddleware(clientCredentials));
                    } else if (!Objects.isNull(oauth2)) {
                        if (!Objects.isNull(oauth2.get(Constants.CLIENT_CREDENTIALS))) {
                            ClientCredentials clientCredentials = ObjectMapperUtils.getMapper().convertValue(oauth2.get(Constants.CLIENT_CREDENTIALS), ClientCredentials.class);
                            middlewares.add(new Oauth2ClientCredentialMiddleware(clientCredentials));
                        } else if (!Objects.isNull(oauth2.get(Constants.PASSWORD))) {
                            ResourceOwnerPassword clientCredentials = ObjectMapperUtils.getMapper().convertValue(oauth2.get(Constants.PASSWORD), ResourceOwnerPassword.class);
                            middlewares.add(new Oauth2ResourceOwnerMiddleware(clientCredentials));
                        }
                    }
                }
            } catch (Exception ex) {
                throw new RestelException(ex, "PRE_HOOKS_ERROR", testDefinition.getCaseUniqueName());
            }
        }
        return middlewares;
    }

    /**
     * Populates the request to the context for the given test name
     *
     * @param request The request for the given test.
     */
    private void populateRequestToContext(RESTRequest request, String testName, String suiteName) {
        if (request.getRequestBody() != null) {
            Map<String, Object> reqMap = Map.of(Constants.REQUEST, request.getRequestBody());
            testContext.addValue(testDefinition.getCaseUniqueName(), reqMap);
            appendRequestGlobalContext(suiteName, testName, testDefinition.getCaseUniqueName(), reqMap);
        }
    }

    private void appendRequestGlobalContext(String suiteName, String testName, String
            testDefinitionName, Map<String, Object> request) {
        GlobalContext instance = GlobalContext.getInstance();
        Map<String, Object> value = Maps.newHashMap(Map.of(testDefinitionName, Collections.singletonList(request)));
        if (Objects.isNull(instance.getContextValues().get(testName))) {
            instance.addValue(testName, new ArrayList<>(Collections.singletonList(value)));
        } else {
            // If the testName params are already present for child testDefinition include the parent testDefinition params in to the List.
            List<Map<String, Object>> testReq = (List) instance.getContextValues().get(testName);
            testReq.add(value);
            instance.addValue(testName, testReq);
        }
        if (Objects.isNull(instance.getContextValues().get(suiteName))) {
            instance.addValue(suiteName, new ArrayList<>(Collections.singletonList(Maps.newHashMap(Map.of(testName, value)))));
        } else {
            // If suite param are already present for child testName include additional params from parent testName.
            List<Map<String, Object>> suiteReq = (List) instance.getContextValues().get(suiteName);
            suiteReq.add(Maps.newHashMap(Map.of(testName, instance.getContextValues().get(testName))));
            instance.addValue(suiteName, suiteReq);
        }
    }
}
