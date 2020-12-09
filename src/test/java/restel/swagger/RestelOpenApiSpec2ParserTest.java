package restel.swagger;

import com.fasterxml.jackson.databind.JsonNode;
import com.pramati.restel.core.parser.dto.BaseConfig;
import com.pramati.restel.core.parser.dto.TestDefinitions;
import com.pramati.restel.swagger.RestelOpenApiSpec2Parser;
import com.pramati.restel.utils.ObjectMapperUtils;
import io.swagger.models.*;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ComposedProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RestelOpenApiSpec2ParserTest {

    private RestelOpenApiSpec2Parser parser;

    @Before
    public void setup() {
        parser = new RestelOpenApiSpec2Parser(getSwagger());
    }

    @Test
    public void testCreateBaseConfig() {
        BaseConfig config = parser.createBaseConfig();
        Assert.assertNotNull(config.getAppName());
        Assert.assertNotNull(config.getBaseUrl());
        Assert.assertTrue(MapUtils.isEmpty(ObjectMapperUtils.convertToMap(config.getDefaultHeader())));
    }

    @Test
    public void testCreateBaseConfigWithNullScheme() {
        Swagger swagger = getSwagger();
        swagger.setInfo(null);
        swagger.setSchemes(null);
        parser = new RestelOpenApiSpec2Parser(swagger);
        BaseConfig config = parser.createBaseConfig();
        Assert.assertNull(config.getAppName());
        Assert.assertNull(config.getBaseUrl());
    }

    @Test
    public void testCreateBaseConfigWithNullBasepath() {
        Swagger swagger = getSwagger();
        swagger.setSchemes(Arrays.asList(null, Scheme.HTTP));
        swagger.setBasePath(null);
        parser = new RestelOpenApiSpec2Parser(swagger);
        BaseConfig config = parser.createBaseConfig();
        Assert.assertEquals(config.getBaseUrl(), Scheme.HTTP.toValue().concat("://").concat(swagger.getHost()));
    }

    @Test
    public void testCreateBaseConfigWithMediaType() {
        Swagger swagger = getSwagger();
        swagger.consumes(MediaType.APPLICATION_JSON).produces(MediaType.APPLICATION_JSON);
        parser = new RestelOpenApiSpec2Parser(swagger);
        BaseConfig config = parser.createBaseConfig();
        Assert.assertEquals(ObjectMapperUtils.convertToMap(config.getDefaultHeader()).get(HttpHeaders.CONTENT_TYPE), swagger.getConsumes().get(0));
        Assert.assertEquals(ObjectMapperUtils.convertToMap(config.getDefaultHeader()).get(HttpHeaders.ACCEPT), swagger.getProduces().get(0));
    }


    @Test
    public void testCreateTestDefinition() {
        List<TestDefinitions> testDefinitions = parser.createTestDefinitions();
        Assert.assertTrue(CollectionUtils.isNotEmpty(testDefinitions));
    }

    @Test
    public void testCreateTestDefinitionWithNoEndpoints() {
        Swagger swagger = getSwagger();
        swagger.setPaths(new HashMap<>());

        parser = new RestelOpenApiSpec2Parser(swagger);
        List<TestDefinitions> testDefinitions = parser.createTestDefinitions();
        Assert.assertTrue(CollectionUtils.isEmpty(testDefinitions));

        swagger.setPaths(null);
        parser = new RestelOpenApiSpec2Parser(swagger);
        List<TestDefinitions> testDefs = parser.createTestDefinitions();
        Assert.assertTrue(CollectionUtils.isEmpty(testDefs));
    }

    @Test
    public void testCreateTestDefinitionWithSingleEndpoints() {
        Swagger swagger = getSwagger();
        HeaderParameter header = new HeaderParameter();
        header.setName("session_id");
        header.setDefault(23);

        FormParameter form = new FormParameter();
        form.setName("meta");
        form.setDefaultValue("string");

        QueryParameter query = new QueryParameter();
        query.setName("query");
        ObjectProperty obj = new ObjectProperty();
        ArrayProperty arr = new ArrayProperty();
        arr.setItems(new ComposedProperty());
        obj.setProperties(Map.of("name", arr));
        query.setItems(obj);

        Path path = new Path().post(
                new Operation().consumes("application/json").produces("application/json")
                        .defaultResponse(new Response().header(HttpHeaders.CONTENT_TYPE, new StringProperty()
                                ._default("application/json"))));
        path.setParameters(Arrays.asList(header, form, query));
        swagger.setPaths(Map.of("/user", path));

        parser = new RestelOpenApiSpec2Parser(swagger);
        List<TestDefinitions> testDefinitions = parser.createTestDefinitions();

        Assert.assertEquals("/user:POST", testDefinitions.get(0).getCaseUniqueName());
        Assert.assertEquals("application/json", ObjectMapperUtils.convertToMap(testDefinitions.get(0).getRequestHeaders()).get(HttpHeaders.CONTENT_TYPE));
        Assert.assertEquals("application/json", ObjectMapperUtils.convertToMap(testDefinitions.get(0).getRequestHeaders()).get(HttpHeaders.ACCEPT));
        Assert.assertEquals("23", ObjectMapperUtils.convertToMap(testDefinitions.get(0).getRequestHeaders()).get("session_id"));
        Assert.assertNotNull(ObjectMapperUtils.convertToMap(testDefinitions.get(0).getExpectedHeader()).get(HttpHeaders.CONTENT_TYPE));
        Assert.assertNotNull(ObjectMapperUtils.convertToMap(testDefinitions.get(0).getRequestBodyParams()).get("meta"));
        Assert.assertNotNull(ObjectMapperUtils.convertToMap(testDefinitions.get(0).getRequestQueryParams()).get("query"));

    }

    @Test
    public void testTestDefinitionResponse() {
        List<TestDefinitions> testDefinitions = parser.createTestDefinitions();
        TestDefinitions definitions = testDefinitions.stream().filter(def -> def.getCaseUniqueName().equals("/pet/{petId}/uploadImage:POST")).collect(Collectors.toList()).get(0);
        Assert.assertEquals(3, ObjectMapperUtils.convertToMap(definitions.getExpectedResponse()).size());
        Assert.assertEquals("integer", ObjectMapperUtils.convertToMap(definitions.getExpectedResponse()).get("code").toString());
        Assert.assertEquals("string", ObjectMapperUtils.convertToMap(definitions.getExpectedResponse()).get("type").toString());
        Assert.assertEquals("string", ObjectMapperUtils.convertToMap(definitions.getExpectedResponse()).get("message").toString());

    }

    @Test
    public void testTestDefinitionReqBody() {
        List<TestDefinitions> testDefinitions = parser.createTestDefinitions();
        JsonNode resp = ObjectMapperUtils.convertToJsonNode(testDefinitions.stream().filter(def -> def.getCaseUniqueName().equals("/pet:PUT")).collect(Collectors.toList()).get(0).getRequestBodyParams());
        Assert.assertTrue(resp.get("category").isObject());
        Assert.assertTrue(resp.get("refer").isObject());
        Assert.assertTrue(resp.get("tags").isArray());
        Assert.assertTrue(resp.get("photoUrls").isArray());
    }

    @Test
    public void testTestDefinitionReqBodyNestedSchema() {
        List<TestDefinitions> testDefinitions = parser.createTestDefinitions();
        JsonNode resp = ObjectMapperUtils.convertToJsonNode(testDefinitions.stream().filter(def -> def.getCaseUniqueName().equals("/user:POST")).collect(Collectors.toList()).get(0).getRequestBodyParams());
        Assert.assertTrue(resp.get("value").isArray());
        Assert.assertTrue(resp.get("namedGroup").isArray());
        Assert.assertTrue(resp.get("value").get(0).get("group").isArray());
        Assert.assertTrue(resp.get("namedGroup").get(0).isArray());
        Assert.assertTrue(resp.get("namedGroup").get(0).get(0).get("group").isArray());
    }

    @Test
    public void testTestDefinitionQuery() {
        List<TestDefinitions> testDefinitions = parser.createTestDefinitions();
        TestDefinitions definitions = testDefinitions.stream().filter(def -> def.getCaseUniqueName().equals("/user/login:GET")).collect(Collectors.toList()).get(0);
        Assert.assertEquals(2, ObjectMapperUtils.convertToMap(definitions.getRequestQueryParams()).size());
        Assert.assertEquals("string", ObjectMapperUtils.convertToJsonNode(definitions.getRequestQueryParams()).get("username").asText());
        Assert.assertEquals("string", ObjectMapperUtils.convertToJsonNode(definitions.getRequestQueryParams()).get("password").asText());
    }

    //TODO add more testcases


    private Swagger getSwagger() {
        return new SwaggerParser().read("src/test/resources/swagger/petstore_2.json");
    }
}
