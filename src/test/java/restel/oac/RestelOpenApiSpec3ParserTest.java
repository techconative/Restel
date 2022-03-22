package restel.oac;

import com.fasterxml.jackson.databind.JsonNode;
import com.pramati.restel.core.parser.dto.BaseConfig;
import com.pramati.restel.core.parser.dto.TestDefinitions;
import com.pramati.restel.oas.RestelOpenApiSpec3Parser;
import com.pramati.restel.utils.ObjectMapperUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RestelOpenApiSpec3ParserTest {

  private RestelOpenApiSpec3Parser parser;

  @Before
  public void setup() {
    parser = new RestelOpenApiSpec3Parser(getSwagger());
  }

  @Test
  public void testCreateBaseConfig() {
    BaseConfig config = parser.createBaseConfig();
    Assert.assertNotNull(config.getAppName());
    Assert.assertNotNull(config.getBaseUrl());
    Assert.assertNull(config.getDefaultHeader());
  }

  @Test
  public void testCreateBaseConfigWithNullScheme() {
    OpenAPI swagger = getSwagger();
    swagger.setInfo(null);
    swagger.setServers(null);
    parser = new RestelOpenApiSpec3Parser(swagger);
    BaseConfig config = parser.createBaseConfig();
    Assert.assertNull(config.getAppName());
    Assert.assertNull(config.getBaseUrl());
  }

  @Test
  public void testCreateBaseConfigWithScheme() {
    OpenAPI swagger = getSwagger();
    Server ser = new Server();
    ser.setUrl("server_url");
    swagger.setServers(Arrays.asList(null, new Server(), ser));
    parser = new RestelOpenApiSpec3Parser(swagger);
    BaseConfig config = parser.createBaseConfig();
    Assert.assertEquals("server_url", config.getBaseUrl());
  }

  @Test
  public void testCreateBaseConfigWithServerVariable() {
    OpenAPI swagger = getSwagger();
    ServerVariables variables = new ServerVariables();
    ServerVariable userVar = new ServerVariable();
    userVar.setDefault("demo");
    variables.put("username", userVar);

    ServerVariable portVar = new ServerVariable();
    portVar.setEnum(Arrays.asList("8443", "443"));
    variables.put("port", portVar);

    ServerVariable pathVar = new ServerVariable();
    pathVar.setEnum(Arrays.asList(null, "v2"));
    variables.put("basePath", pathVar);

    Server ser = new Server();
    ser.setUrl("https://{username}.gigantic-{username}.com:{port}/{basePath}");
    ser.setVariables(variables);

    swagger.setServers(Arrays.asList(null, new Server(), ser));
    parser = new RestelOpenApiSpec3Parser(swagger);
    BaseConfig config = parser.createBaseConfig();
    Assert.assertEquals("https://demo.gigantic-demo.com:8443/v2", config.getBaseUrl());
  }

  @Test
  public void testCreateTestDefinition() {
    List<TestDefinitions> testDefinitions = parser.createTestDefinitions();
    Assert.assertTrue(CollectionUtils.isNotEmpty(testDefinitions));
  }

  @Test
  public void testTestDefinitionReqBody() {
    List<TestDefinitions> testDefinitions = parser.createTestDefinitions();
    JsonNode resp =
        ObjectMapperUtils.convertToJsonNode(
            testDefinitions.stream()
                .filter(def -> def.getCaseUniqueName().equals("/pet:PUT"))
                .collect(Collectors.toList())
                .get(0)
                .getRequestBodyParams());
    Assert.assertTrue(resp.get("category").isObject());
    Assert.assertTrue(resp.get("refer").isObject());
    Assert.assertTrue(resp.get("tags").isArray());
    Assert.assertTrue(resp.get("photoUrls").isArray());
  }

  @Test
  public void testCreateBaseConfigWithNewSpecs() {
    parser = new RestelOpenApiSpec3Parser(getOpenAPI());
    BaseConfig config = parser.createBaseConfig();
    Assert.assertNotNull(config.getAppName());
    Assert.assertNotNull(config.getBaseUrl());
    Assert.assertNull(config.getDefaultHeader());
  }

  @Test
  public void testCreateTestDefinitionWithNewSpecs() {
    parser = new RestelOpenApiSpec3Parser(getOpenAPI());
    List<TestDefinitions> testDefinitions = parser.createTestDefinitions();
    Assert.assertTrue(CollectionUtils.isNotEmpty(testDefinitions));
  }

  @Test
  public void testTestDefinitionResponse() {
    List<TestDefinitions> testDefinitions = parser.createTestDefinitions();
    TestDefinitions definitions =
        testDefinitions.stream()
            .filter(def -> def.getCaseUniqueName().equals("/pet/{petId}/uploadImage:POST"))
            .collect(Collectors.toList())
            .get(0);
    Assert.assertEquals(
        3, ObjectMapperUtils.convertToMap(definitions.getExpectedResponse()).size());
    Assert.assertEquals(
        "integer",
        ObjectMapperUtils.convertToMap(definitions.getExpectedResponse()).get("code").toString());
    Assert.assertEquals(
        "string",
        ObjectMapperUtils.convertToMap(definitions.getExpectedResponse()).get("type").toString());
    Assert.assertEquals(
        "string",
        ObjectMapperUtils.convertToMap(definitions.getExpectedResponse())
            .get("message")
            .toString());
  }

  @Test
  public void testTestDefinitionResponseNested() {
    parser = new RestelOpenApiSpec3Parser(getOpenAPI());
    List<TestDefinitions> testDefinitions = parser.createTestDefinitions();
    TestDefinitions definitions =
        testDefinitions.stream()
            .filter(def -> def.getCaseUniqueName().equals("/pet/{petId}:GET"))
            .collect(Collectors.toList())
            .get(0);
    Assert.assertEquals(
        8, ObjectMapperUtils.convertToMap(definitions.getExpectedResponse()).size());
    Assert.assertTrue(
        ObjectMapperUtils.convertToJsonNode(definitions.getExpectedResponse())
            .get("animal")
            .isObject());
    Assert.assertTrue(
        ObjectMapperUtils.convertToJsonNode(definitions.getExpectedResponse())
            .get("category")
            .isObject());
    Assert.assertTrue(
        ObjectMapperUtils.convertToJsonNode(definitions.getExpectedResponse())
            .get("group")
            .isArray());
    Assert.assertTrue(
        ObjectMapperUtils.convertToJsonNode(definitions.getExpectedResponse())
            .get("tags")
            .isArray());
    Assert.assertTrue(
        ObjectMapperUtils.convertToJsonNode(definitions.getExpectedResponse())
            .get("photoUrls")
            .isArray());
  }

  @Test
  public void testTestDefinitionRequestNested() {
    parser = new RestelOpenApiSpec3Parser(getOpenAPI());
    List<TestDefinitions> testDefinitions = parser.createTestDefinitions();
    TestDefinitions definitions =
        testDefinitions.stream()
            .filter(def -> def.getCaseUniqueName().equals("/pet:POST"))
            .collect(Collectors.toList())
            .get(0);
    Assert.assertEquals(
        8, ObjectMapperUtils.convertToMap(definitions.getRequestBodyParams()).size());
    Assert.assertTrue(
        ObjectMapperUtils.convertToJsonNode(definitions.getRequestBodyParams())
            .get("animal")
            .isObject());
    Assert.assertTrue(
        ObjectMapperUtils.convertToJsonNode(definitions.getRequestBodyParams())
            .get("category")
            .isObject());
    Assert.assertTrue(
        ObjectMapperUtils.convertToJsonNode(definitions.getRequestBodyParams())
            .get("group")
            .isArray());
    Assert.assertTrue(
        ObjectMapperUtils.convertToJsonNode(definitions.getRequestBodyParams())
            .get("tags")
            .isArray());
    Assert.assertTrue(
        ObjectMapperUtils.convertToJsonNode(definitions.getRequestBodyParams())
            .get("photoUrls")
            .isArray());
  }

  @Test
  public void testTestDefinitionQuery() {
    parser = new RestelOpenApiSpec3Parser(getOpenAPI());
    List<TestDefinitions> testDefinitions = parser.createTestDefinitions();
    TestDefinitions definitions =
        testDefinitions.stream()
            .filter(def -> def.getCaseUniqueName().equals("/pet/{petId}:GET"))
            .collect(Collectors.toList())
            .get(0);
    Assert.assertEquals(
        2, ObjectMapperUtils.convertToMap(definitions.getRequestQueryParams()).size());
    Assert.assertEquals(
        "integer",
        ObjectMapperUtils.convertToJsonNode(definitions.getRequestQueryParams())
            .get("limit")
            .asText());
    Assert.assertEquals(
        "integer",
        ObjectMapperUtils.convertToJsonNode(definitions.getRequestQueryParams())
            .get("offset")
            .asText());
  }

  @Test
  public void testTestDefinitionResponseHeader() {
    parser = new RestelOpenApiSpec3Parser(getOpenAPI());
    List<TestDefinitions> testDefinitions = parser.createTestDefinitions();
    TestDefinitions definitions =
        testDefinitions.stream()
            .filter(def -> def.getCaseUniqueName().equals("/pet/{petId}:DELETE"))
            .collect(Collectors.toList())
            .get(0);
    Assert.assertEquals(2, ObjectMapperUtils.convertToMap(definitions.getExpectedHeader()).size());
    Assert.assertTrue(
        ObjectMapperUtils.convertToJsonNode(definitions.getExpectedHeader())
            .get("X-RateLimit-Limit")
            .isArray());
    Assert.assertEquals(
        3,
        ObjectMapperUtils.convertToJsonNode(definitions.getExpectedHeader())
            .get("X-RateLimit-Limit")
            .get(0)
            .size());
    Assert.assertEquals(
        "integer",
        ObjectMapperUtils.convertToJsonNode(definitions.getExpectedHeader())
            .get("X-RateLimit-Remaining")
            .asText());
  }
  // TODO add more testcases

  private OpenAPI getSwagger() {
    return new OpenAPIV3Parser().read("src/test/resources/swagger/petstore_2.json");
  }

  private OpenAPI getOpenAPI() {
    return new OpenAPIV3Parser().read("src/test/resources/swagger/petstore_3.json");
  }
}
