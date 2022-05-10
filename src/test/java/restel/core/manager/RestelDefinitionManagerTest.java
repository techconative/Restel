package restel.core.manager;

import static java.lang.String.format;
import static org.testng.AssertJUnit.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.techconative.restel.core.http.RESTResponse;
import com.techconative.restel.core.http.ResponseBody;
import com.techconative.restel.core.managers.RequestManager;
import com.techconative.restel.core.managers.RestelDefinitionManager;
import com.techconative.restel.core.model.GlobalContext;
import com.techconative.restel.core.model.RestelTestApiDefinition;
import com.techconative.restel.core.model.TestContext;
import com.techconative.restel.core.model.comparator.ExactMatchComparator;
import com.techconative.restel.core.model.comparator.NoOPMatcher;
import com.techconative.restel.testng.MatcherFactory;
import com.techconative.restel.utils.Constants;
import com.techconative.restel.utils.ObjectMapperUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RestelDefinitionManagerTest {

  @InjectMocks private RestelDefinitionManager manager;

  @Mock private RequestManager requestManager;
  @Mock private MatcherFactory matcherFactory;

  private String defaultScenarioName = "scenario_name";
  private String defaultApiName = "api_name";

  @Before
  public void initMocks() throws NoSuchFieldException {
    MockitoAnnotations.initMocks(this);
    FieldSetter.setField(
        manager,
        manager.getClass().getDeclaredField("testContext"),
        new TestContext(defaultScenarioName));
  }

  @Test
  public void testExecuteTest() throws NoSuchFieldException {
    FieldSetter.setField(
        manager, manager.getClass().getDeclaredField("testDefinitions"), List.of(createTestDef()));

    RESTResponse restResponse = new RESTResponse();
    restResponse.setResponse(ResponseBody.builder().body("response").build());
    restResponse.setStatus(200);

    Mockito.doReturn(new NoOPMatcher()).when(matcherFactory).getMatcher(Mockito.anyString());
    Mockito.when(requestManager.makeCall(Mockito.any(), Mockito.anyList(), Mockito.anyList()))
        .thenReturn(restResponse);
    Assert.assertTrue(manager.executeTestScenario("Sample", "suite"));
  }

  @Test
  public void testExecuteTestReqBody() throws NoSuchFieldException {
    RestelTestApiDefinition method = createTestDef();
    method.setRequestHeaders(new HashMap<>());
    method.setRequestBodyParams("Body");
    FieldSetter.setField(
        manager, manager.getClass().getDeclaredField("testDefinitions"), List.of(method));

    RESTResponse restResponse = new RESTResponse();
    restResponse.setResponse(ResponseBody.builder().body("response").build());
    restResponse.setStatus(200);

    Mockito.doReturn(new NoOPMatcher()).when(matcherFactory).getMatcher(Mockito.anyString());
    Mockito.when(requestManager.makeCall(Mockito.any(), Mockito.anyList(), Mockito.anyList()))
        .thenReturn(restResponse);
    Assert.assertTrue(manager.executeTestScenario("Sample", "suite"));
  }

  @Test
  public void testExecuteTestResBody() throws NoSuchFieldException {
    RestelTestApiDefinition method = createTestDef();
    method.setRequestHeaders(new HashMap<>());
    method.setExpectedResponse("{\"key\": \"value\"}");
    FieldSetter.setField(
        manager, manager.getClass().getDeclaredField("testDefinitions"), List.of(method));

    RESTResponse restResponse = new RESTResponse();
    restResponse.setResponse(ResponseBody.builder().body("{\"key\": \"value\"}").build());
    restResponse.setStatus(200);

    Mockito.doReturn(new ExactMatchComparator())
        .when(matcherFactory)
        .getMatcher(Mockito.anyString());
    Mockito.when(requestManager.makeCall(Mockito.any(), Mockito.anyList(), Mockito.anyList()))
        .thenReturn(restResponse);
    Assert.assertTrue(manager.executeTestScenario("Sample", "suite"));
  }

  @Test
  public void testExecuteTestOauth2Client() throws NoSuchFieldException {
    RestelTestApiDefinition method = createTestDef();
    method.setRequestHeaders(new HashMap<>());
    method.setExpectedResponse("{\"key\": \"value\"}");
    method.setRequestPreCallHook(clientCredential());
    FieldSetter.setField(
        manager, manager.getClass().getDeclaredField("testDefinitions"), List.of(method));

    RESTResponse restResponse = new RESTResponse();
    restResponse.setResponse(ResponseBody.builder().body("{\"key\": \"value\"}").build());
    restResponse.setStatus(200);

    Mockito.doReturn(new ExactMatchComparator())
        .when(matcherFactory)
        .getMatcher(Mockito.anyString());
    Mockito.when(requestManager.makeCall(Mockito.any(), Mockito.anyList(), Mockito.anyList()))
        .thenReturn(restResponse);
    Assert.assertTrue(manager.executeTestScenario("Sample", "suite"));
  }

  @Test
  public void testExecuteTestOauth2Resource() throws NoSuchFieldException {
    RestelTestApiDefinition method = createTestDef();
    method.setRequestHeaders(new HashMap<>());
    method.setExpectedResponse("{\"key\": \"value\"}");
    method.setRequestPreCallHook(resourceOwner());
    FieldSetter.setField(
        manager, manager.getClass().getDeclaredField("testDefinitions"), List.of(method));

    RESTResponse restResponse = new RESTResponse();
    restResponse.setResponse(ResponseBody.builder().body("{\"key\": \"value\"}").build());
    restResponse.setStatus(200);

    Mockito.doReturn(new ExactMatchComparator())
        .when(matcherFactory)
        .getMatcher(Mockito.anyString());
    Mockito.when(requestManager.makeCall(Mockito.any(), Mockito.anyList(), Mockito.anyList()))
        .thenReturn(restResponse);
    Assert.assertTrue(manager.executeTestScenario("Sample", "suite"));
  }

  @Test
  public void testExecuteTestResBodyJson() throws NoSuchFieldException {
    RestelTestApiDefinition method = createTestDef();
    method.setRequestHeaders(new HashMap<>());
    method.setExpectedResponse(Map.of("key", "value"));
    FieldSetter.setField(
        manager, manager.getClass().getDeclaredField("testDefinitions"), List.of(method));

    RESTResponse restResponse = new RESTResponse();
    restResponse.setResponse(ResponseBody.builder().body(Map.of("key", "value")).build());
    restResponse.setStatus(200);

    Mockito.doReturn(new ExactMatchComparator())
        .when(matcherFactory)
        .getMatcher(Mockito.anyString());
    Mockito.when(requestManager.makeCall(Mockito.any(), Mockito.anyList(), Mockito.anyList()))
        .thenReturn(restResponse);
    Assert.assertTrue(manager.executeTestScenario("Sample", "suite"));
  }

  @Test
  public void testRequestResponseAvailableFromGlobalContext() throws NoSuchFieldException {
    RestelTestApiDefinition method = createTestDef();
    method.setRequestHeaders(new HashMap<>());
    method.setExpectedResponse(Map.of("key", "value"));
    FieldSetter.setField(
        manager, manager.getClass().getDeclaredField("testDefinitions"), List.of(method));

    RESTResponse restResponse = new RESTResponse();
    restResponse.setResponse(ResponseBody.builder().body(Map.of("key", "value")).build());
    restResponse.setStatus(200);

    Mockito.doReturn(new ExactMatchComparator())
        .when(matcherFactory)
        .getMatcher(Mockito.anyString());
    Mockito.when(requestManager.makeCall(Mockito.any(), Mockito.anyList(), Mockito.anyList()))
        .thenReturn(restResponse);
    manager.executeTestScenario(defaultScenarioName, "suite");

    Map<String, Object> requestBodyParams =
        ObjectMapperUtils.convertToMap(method.getRequestBodyParams().toString());
    assertEquals(
        GlobalContext.getInstance()
            .resolveValue(format("%s.%s.request", defaultScenarioName, defaultApiName)),
        requestBodyParams);

    assertEquals(
        GlobalContext.getInstance()
            .resolveValue(format("%s.%s.response", defaultScenarioName, defaultApiName)),
        restResponse.getResponse().getBody());
  }

  @Test(expected = AssertionError.class)
  public void testExecuteTestStatusCodeEmpty() throws NoSuchFieldException {
    RestelTestApiDefinition method = createTestDef();
    method.setRequestHeaders(new HashMap<>());
    method.setRequestBodyParams("Body");
    method.setAcceptedStatusCodes(Arrays.asList("500"));
    FieldSetter.setField(
        manager, manager.getClass().getDeclaredField("testDefinitions"), List.of(method));

    RESTResponse restResponse = new RESTResponse();
    restResponse.setResponse(ResponseBody.builder().body("response").build());
    restResponse.setStatus(200);

    Mockito.when(requestManager.makeCall(Mockito.any(), Mockito.anyList(), Mockito.anyList()))
        .thenReturn(restResponse);
    manager.executeTestScenario("Sample", "suite");
  }

  @Test
  public void testExecuteTestStatusCodeParameter() throws NoSuchFieldException {
    RestelTestApiDefinition method = createTestDef();
    method.setRequestHeaders(new HashMap<>());
    method.setRequestBodyParams("Body");
    method.setAcceptedStatusCodes(Arrays.asList("200", "${accepted_status_code}"));
    FieldSetter.setField(
        manager, manager.getClass().getDeclaredField("testDefinitions"), List.of(method));

    RESTResponse restResponse = new RESTResponse();
    restResponse.setResponse(ResponseBody.builder().body("response").build());
    restResponse.setStatus(418);

    GlobalContext.getInstance().addValue("accepted_status_code", "418");

    Mockito.doReturn(new NoOPMatcher()).when(matcherFactory).getMatcher(Mockito.anyString());
    Mockito.when(requestManager.makeCall(Mockito.any(), Mockito.anyList(), Mockito.anyList()))
        .thenReturn(restResponse);
    Assert.assertTrue(manager.executeTestScenario("Sample", "suite"));
  }

  private RestelTestApiDefinition createTestDef() {
    RestelTestApiDefinition definitions = new RestelTestApiDefinition();
    definitions.setApiUniqueName(defaultApiName);
    definitions.setRequestUrl("/test");
    definitions.setRequestQueryParams(Map.of("k", "v"));
    definitions.setRequestHeaders(
        Map.of(HttpHeaders.CONTENT_TYPE.toLowerCase(), MediaType.APPLICATION_JSON));
    definitions.setRequestBodyParams("{\"k\": \"v\"}");
    definitions.setRequestMethod("POST");
    definitions.setExpectedResponseMatcher("NOOP_MATCHER");
    definitions.setExpectedHeaderMatcher("NOOP_MATCHER");
    definitions.setExpectedResponse("response");
    definitions.setDependentOn(null);
    definitions.setAcceptedStatusCodes(Arrays.asList("200", "404"));
    definitions.setRequestPreCallHook(getBasicAuth());
    definitions.setRequestPostCallHook(
        ObjectMapperUtils.getMapper()
            .createObjectNode()
            .put("write", "src/test/resources/res.txt"));
    return definitions;
  }

  private JsonNode getBasicAuth() {
    return ObjectMapperUtils.getMapper()
        .createObjectNode()
        .set(
            HttpHeaders.AUTHORIZATION,
            ObjectMapperUtils.getMapper()
                .createObjectNode()
                .set(
                    Constants.BASIC_AUTH,
                    ObjectMapperUtils.getMapper()
                        .createObjectNode()
                        .put(Constants.USERNAME, "user")
                        .put(Constants.PASSWORD, "pass")));
  }

  private JsonNode clientCredential() {
    ObjectNode cred =
        ObjectMapperUtils.getMapper()
            .createObjectNode()
            .put("authUrl", "url")
            .put("clientId", "id")
            .put("clientSecret", "secret")
            .put("scope", "scope");
    JsonNode oauth =
        ObjectMapperUtils.getMapper().createObjectNode().set("client_credentials", cred);
    return ObjectMapperUtils.getMapper()
        .createObjectNode()
        .set(
            "Authorization", ObjectMapperUtils.getMapper().createObjectNode().set("oauth2", oauth));
  }

  private JsonNode resourceOwner() {
    ObjectNode cred =
        ObjectMapperUtils.getMapper()
            .createObjectNode()
            .put("authUrl", "url")
            .put("clientId", "id")
            .put("clientSecret", "secret")
            .put("scope", "scope")
            .put("username", "user")
            .put("password", "pwd");
    JsonNode oauth = ObjectMapperUtils.getMapper().createObjectNode().set("password", cred);
    return ObjectMapperUtils.getMapper()
        .createObjectNode()
        .set(
            "Authorization", ObjectMapperUtils.getMapper().createObjectNode().set("oauth2", oauth));
  }
}
