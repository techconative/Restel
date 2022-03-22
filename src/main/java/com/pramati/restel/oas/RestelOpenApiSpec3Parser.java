package com.pramati.restel.oas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pramati.restel.core.parser.dto.BaseConfig;
import com.pramati.restel.core.parser.dto.TestDefinitions;
import com.pramati.restel.utils.Constants;
import com.pramati.restel.utils.ObjectMapperUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Parse OpenAPI spec 3.0 to Restel Models
 */
@Slf4j
public class RestelOpenApiSpec3Parser {
    private OpenAPI parser;

    private static String componentRegex = "^#/components/(.*)/(.*)";

    private static String bracesRegex = "\\{(.+?)\\}";

    public RestelOpenApiSpec3Parser(OpenAPI parser) {
        this.parser = parser;
    }

    /**
     * create {@link BaseConfig} with base_url
     *
     * @return {@link BaseConfig}
     */
    public BaseConfig createBaseConfig() {
        BaseConfig baseConfig = new BaseConfig();
        // add title
        if (!Objects.isNull(parser.getInfo())) {
            baseConfig.setAppName(parser.getInfo().getTitle());
        }
        //Add baseUrl
        if (CollectionUtils.isNotEmpty(parser.getServers())) {
            for (Server server : parser.getServers()) {
                if (!Objects.isNull(server) && StringUtils.isNotEmpty(server.getUrl())) {
                    baseConfig.setBaseUrl(resolveServerUrl(server));
                    break;
                }
            }
        }
        return baseConfig;
    }

    /**
     * if server url has variable will replace the variable with its value.
     *
     * @param server {@link Server}
     * @return return the ServerUrl after resolving the variables.
     */
    private String resolveServerUrl(Server server) {
        Matcher matcher = Pattern.compile(bracesRegex).matcher(server.getUrl());
        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (matcher.find()) {
            ServerVariable replacement = server.getVariables().get(matcher.group(1));
            builder.append(server.getUrl(), i, matcher.start());
            replace(matcher, replacement, builder);
            i = matcher.end();
        }
        builder.append(server.getUrl().substring(i));
        return builder.toString();
    }

    /**
     * @param matcher     Matcher to verify and replace the serverVariables
     * @param replacement {@link ServerVariable}
     * @param builder     append the replaceable into the builder.
     */
    private void replace(Matcher matcher, ServerVariable replacement, StringBuilder builder) {
        if (Objects.isNull(replacement)) {
            builder.append(matcher.group(0));
        } else {
            if (!Objects.isNull(replacement.getDefault())) {
                builder.append(replacement.getDefault());
            } else if (CollectionUtils.isNotEmpty(replacement.getEnum())) {
                for (String v : replacement.getEnum()) {
                    if (!Objects.isNull(v)) {
                        builder.append(v);
                        break;
                    }
                }
            } else {
                builder.append(matcher.group(0));
            }
        }
    }

    /**
     * Create TestDefinitions from the swagger parser
     *
     * @return List of {@link TestDefinitions}
     */
    public List<TestDefinitions> createTestDefinitions() {
        List<TestDefinitions> testDefinitions = new ArrayList<>();
        if (MapUtils.isNotEmpty(parser.getPaths())) {
            for (String path : parser.getPaths().keySet()) {
                testDefinitions.addAll(
                        parser.getPaths().get(path).readOperationsMap().entrySet().parallelStream().map(entry -> defineTestDefinition(path, entry)
                        ).collect(Collectors.toList())
                );
            }
        }
        return testDefinitions;
    }

    /**
     * @param path  Swagger endpoint name.
     * @param entry Entity of Swagger endpoint method and its operation.
     * @return Converts the endpoint operation into a testDefinition
     */
    private TestDefinitions defineTestDefinition(String path, Map.Entry<PathItem.HttpMethod, Operation> entry) {
        TestDefinitions def = new TestDefinitions();
        def.setCaseDescription(entry.getValue().getDescription());
        def.setRequestUrl(path);
        def.setRequestMethod(entry.getKey().name());
        def.setCaseUniqueName(path.concat(":").concat(entry.getKey().name()));
        def.setTags(new HashSet<>(entry.getValue().getTags()));
        def.setAcceptedStatusCodes(new ArrayList<>(entry.getValue().getResponses().keySet()));
        def.setRequestQueryParams(getParams(parser.getPaths().get(path).getParameters(), entry.getValue().getParameters(), Constants.QUERY).toPrettyString());
        def.setRequestHeaders(getParams(parser.getPaths().get(path).getParameters(), entry.getValue().getParameters(), Constants.HEADER).toPrettyString());
        if (!Objects.isNull(entry.getValue().getRequestBody())) {
            def.setRequestBodyParams(addRequestBody(entry.getValue().getRequestBody()).toPrettyString());
        }

        if (MapUtils.isNotEmpty(entry.getValue().getResponses())) {
            def.setExpectedResponse(addResponseBody(entry.getValue().getResponses()).toPrettyString());
            def.setExpectedHeader(addResponseHeader(entry.getValue().getResponses()).toPrettyString());
        }
        return def;
    }

    /**
     * @param response {@link ApiResponse}
     * @param node     {@link ObjectNode} which stores the response Headers.
     */
    private void addResponseHeader(ApiResponse response, ObjectNode node) {
        if (MapUtils.isNotEmpty(response.getHeaders())) {
            response.getHeaders().forEach((k, h) -> node.putPOJO(k, getHeader(h)));
        }
    }

    /**
     * returns an ObjectNode of response headers from APIResponse with status code 200 first .If not present search for default response,
     * else takes the first element({@link ApiResponse}) from {@link ApiResponses}.
     *
     * @param responses {@link ApiResponses}
     * @return returns an {@link ObjectNode} which stores the Response Headers
     */
    private ObjectNode addResponseHeader(ApiResponses responses) {
        ObjectNode node = ObjectMapperUtils.getMapper().createObjectNode();
        if (!Objects.isNull(responses.get(String.valueOf(HttpStatus.SC_OK)))) {
            addResponseHeader(responses.get(String.valueOf(HttpStatus.SC_OK)), node);
        } else if (!Objects.isNull(responses.getDefault())) {
            addResponseHeader(responses.getDefault(), node);
        } else {
            for (ApiResponse response : responses.values()) {
                addResponseHeader(response, node);
                if (!node.isEmpty()) {
                    break;
                }
            }
        }
        return node;
    }


    /**
     * get responseBody from reference and Content.
     *
     * @param resp {@link ApiResponse}
     */
    private Object getResponseBody(ApiResponse resp) {
        if (validRef(resp.get$ref())) {
            return getModelFromComponent(getRefName(resp.get$ref()), getRefType(resp.get$ref()));
        } else if (!Objects.isNull(resp.getContent())) {
            return getModelFromContent(resp.getContent());
        }
        return null;
    }

    /**
     * returns an ObjectNode from APIResponse with status code 200 first .If not present search for default response,
     * else takes the first element({@link ApiResponse}) from {@link ApiResponses}.
     *
     * @param responses {@link ApiResponses}
     * @return returns an {@link JsonNode} with responseBody model
     */
    private JsonNode addResponseBody(ApiResponses responses) {
        Object out = null;
        if (!Objects.isNull(responses.get(String.valueOf(HttpStatus.SC_OK)))) {
            out = getResponseBody(responses.get(String.valueOf(HttpStatus.SC_OK)));
        } else if (!Objects.isNull(responses.getDefault())) {
            out = getResponseBody(responses.getDefault());
        } else {
            for (ApiResponse response : responses.values()) {
                Object node = getResponseBody(response);
                if (!Objects.isNull(node)) {
                    out = node;
                    break;
                }
            }
        }
        return eval(out);
    }

    /**
     * Constructs the JsonNode of requestBody schema.
     *
     * @param requestBody {@link RequestBody}
     * @return return the JsonNode from the requestBody.
     */
    private Object getRequestBody(RequestBody requestBody) {
        if (StringUtils.isNotEmpty(requestBody.get$ref())) {
            return getModelFromComponent(getRefName(requestBody.get$ref()), getRefType(requestBody.get$ref()));
        } else if (MapUtils.isNotEmpty(requestBody.getContent())) {
            return getModelFromContent(requestBody.getContent());
        }
        return null;
    }

    /**
     * @param requestBody {@link RequestBody}
     * @return return RequestBody Model in {@link JsonNode}
     */
    private JsonNode addRequestBody(RequestBody requestBody) {
        Object output = null;
        if (StringUtils.isNotEmpty(requestBody.get$ref())) {
            output = getModelFromComponent(getRefName(requestBody.get$ref()), getRefType(requestBody.get$ref()));
        } else if (MapUtils.isNotEmpty(requestBody.getContent())) {
            output = getModelFromContent(requestBody.getContent());
        }
        return eval(output);
    }


    /**
     * @param output should be one of ObjectNode or ArrayNode
     * @return check if the output is ObjectNode or ArrayNode and returns.
     * If non of those values then it will return empty ObjectNode
     */
    private JsonNode eval(Object output) {
        if (output instanceof ArrayNode) {
            return (ArrayNode) output;
        } else if (output instanceof ObjectNode) {
            return (ObjectNode) output;
        } else {
            return ObjectMapperUtils.getMapper().createObjectNode();
        }
    }

    /**
     * @param content {@link Content}
     * @return get JsonNode of the Content of MediaType: application/json first if present ,or lese return the first element from the Content Map.
     */
    private Object getModelFromContent(Content content) {
        if (!Objects.isNull(content.get(MediaType.APPLICATION_JSON))) {
            return getModelFromMediaType(content.get(MediaType.APPLICATION_JSON));
        } else {
            for (Map.Entry<String, io.swagger.v3.oas.models.media.MediaType> media : content.entrySet()) {
                Object node = getModelFromMediaType(media.getValue());
                if (!Objects.isNull(node)) {
                    return node;
                }
            }
        }
        return ObjectMapperUtils.getMapper().createObjectNode();
    }

    /**
     * @param media {@link io.swagger.v3.oas.models.media.MediaType}
     * @return return the schema model from the media.
     */
    private Object getModelFromMediaType(io.swagger.v3.oas.models.media.MediaType media) {
        return getModel(media.getSchema());
    }

    /**
     * @param endpointParameters list of {@link Parameter} from endpoint level.
     * @param opParameters       list of {@link Parameter} from Operation level.
     * @param type               one of ['query','header','cookie','path'].
     * @return return the parameter models .
     */
    private ObjectNode getParams(List<Parameter> endpointParameters, List<Parameter> opParameters, String type) {
        ObjectNode params = ObjectMapperUtils.getMapper().createObjectNode();
        if (CollectionUtils.isNotEmpty(endpointParameters)) {
            //resolve endpoint params
            addParametersByType(endpointParameters, type, params);
        }
        if (CollectionUtils.isNotEmpty(opParameters)) {
            // resolve operation params
            addParametersByType(opParameters, type, params);
        }
        return params;
    }

    /**
     * @param parameters list of {@link Parameter}
     * @param type       one of ['query','header','cookie','path'].
     * @param paramsNode {@link ObjectNode} where parameters .
     */
    private void addParametersByType(List<Parameter> parameters, String type, ObjectNode paramsNode) {
        parameters.parallelStream().filter(parameter -> StringUtils.containsIgnoreCase(parameter.getIn(), type)).forEach(
                par -> {
                    if (!Objects.isNull(par.getSchema()) && StringUtils.isNotBlank(par.getSchema().getType())) {
                        paramsNode.putPOJO(par.getName(), getModel(par.getSchema()));
                    } else if (validRef(par.get$ref())) {
                        paramsNode.putPOJO(par.getName(), getModelFromComponent(getRefName(par.get$ref()), getRefType(par.get$ref())));
                    }
                }
        );
    }


    /**
     * regex pattern to capture reference one  of [ref,type,name].
     *
     * @param ref   Reference String format:#/components/<type>/<name> . eg: #/components/schema/Pet
     * @param index Should be either 0, 1 or 2 .
     * @return If index= 0 the ref (from example '#/components/schema/Pet') is returned,
     * else if index=1 then Type ( from example 'schema') is returned,else if index=2 name (from example 'Pet') is returned.For order index values will return null.
     */
    private String parseRef(String ref, int index) {
        try {
            Matcher matcher = Pattern.compile(componentRegex).matcher(ref);
            if (matcher.find()) {
                return matcher.group(index);
            }
        } catch (Exception ex) {
            log.warn("invalid index: {} should be one of [0,1,2] for Schema reference format:{} ", index, ref);
        }
        return null;
    }

    /**
     * @param ref Swagger component format:#/components/<schemaType>/<schemaName>
     * @return retyrn schema type from format:#/components/<schemaType>/<schemaName>
     */
    private String getRefType(String ref) {
        return parseRef(ref, 1);
    }

    /**
     * @param ref Swagger component format:#/components/<schemaType>/<schemaName>
     * @return return the schema name from the format:#/components/<schemaType>/<schemaName>
     */
    private String getRefName(String ref) {
        return parseRef(ref, 2);
    }

    /**
     * Constructs the JsonNode from swagger component
     *
     * @param schemaName schema name from the swagger components format:#/components/<schemaType>/<schemaName>
     * @param schemaType schema Type from the swagger components format:#/components/<schemaType>/<schemaName>
     * @return return the JsonNode from the swagger component's schema name and type
     */
    private Object getModelFromComponent(String schemaName, String schemaType) {
        Components components = parser.getComponents();
        if (!Objects.isNull(components)) {
            // get RequestBodies
            if (StringUtils.containsIgnoreCase(schemaType, Constants.REQUEST_BODIES) && !Objects.isNull(components.getRequestBodies())
                    && !Objects.isNull(components.getRequestBodies().get(schemaName))) {
                RequestBody requestBody = components.getRequestBodies().get(schemaName);
                return getRequestBody(requestBody);
            }
            // get schema
            else if (StringUtils.containsIgnoreCase(schemaType, Constants.SCHEMA) && !Objects.isNull(components.getSchemas().get(schemaName))) {
                return getModel(components.getSchemas().get(schemaName));
            }// get Parameter
            else if (StringUtils.containsIgnoreCase(schemaType, Constants.PARAMETER) && !Objects.isNull(components.getParameters().get(schemaName))) {
                Parameter parameter = components.getParameters().get(schemaName);
                ObjectNode node = ObjectMapperUtils.getMapper().createObjectNode();
                addParametersByType(Collections.singletonList(parameter), parameter.getIn(), node);
                return node;
            }// get Header
            else if (StringUtils.containsIgnoreCase(schemaType, Constants.HEADER) && !Objects.isNull(components.getHeaders().get(schemaName))) {
                return getHeader(components.getHeaders().get(schemaName));
            } else if (StringUtils.containsIgnoreCase(schemaType, Constants.RESPONSE) && !Objects.isNull(components.getResponses().get(schemaName))) {
                return getResponseBody(components.getResponses().get(schemaName));
            } else {
                log.warn("Missed adding model for the component with schemaName:{} ,schemaType:{} ", schemaName, schemaName);
            }
        }
        return null;
    }


    /**
     * Constructs {@link JsonNode} from the header.
     *
     * @param header {@link Header}
     * @return Return JsonNode from the header.
     */
    private Object getHeader(Header header) {
        if (!Objects.isNull(header.get$ref())) {
            return getModelFromComponent(getRefName(header.get$ref()), getRefType(header.get$ref()));
        } else if (!Objects.isNull(header.getSchema())) {
            return getModel(header.getSchema());
        }
        return null;
    }

    /**
     * @param ref reference String, should be of format:#/components/<schemaType>/<schemaName> . eg: #/components/schema/Pet
     * @return validates whether the reference string follow the format #/components/schema/Pet
     */
    private boolean validRef(String ref) {
        return StringUtils.isNotEmpty(ref) &&
                StringUtils.isNotEmpty(getRefType(ref))
                && StringUtils.isNotEmpty(getRefName(ref));
    }

    /**
     * Constructs the JsonNode from the schema.
     *
     * @param schema {@link schema}
     * @return return a JsonNode from the schema. if schema is a primitive type return string value of the schema type (like 'string','integer',etc.,)
     */
    private Object getModel(Schema<?> schema) {
        if (schema instanceof ArraySchema) {
            return ObjectMapperUtils.getMapper().createArrayNode().addPOJO(getModel(((ArraySchema) schema).getItems()));
        } else if (schema instanceof ObjectSchema) {
            ObjectNode n = ObjectMapperUtils.getMapper().createObjectNode();
            schema.getProperties().forEach((k, s) -> n.putPOJO(k, getModel(s)));
            return n;
        } else if (schema instanceof ComposedSchema) {
            return getComposedModel((ComposedSchema) schema);
        } else if (validRef(schema.get$ref())) {
            return getModelFromComponent(getRefName(schema.get$ref()), getRefType(schema.get$ref()));
        } else {
            return schema.getType();
        }
    }

    /**
     * If schema has oneOf or anyOf, will construct the JsonNode with the first element.
     * If the schema has allOf, will combine the attributes from each of the elements and returns JsonNode.
     *
     * @param schema {@link ComposedSchema}
     * @return return the Composed schema as Objectnode.
     */
    private Object getComposedModel(ComposedSchema schema) {
        Object n = null;
        try {
            if (CollectionUtils.isNotEmpty(schema.getAllOf())) {
                List<Object> data = schema.getAllOf().parallelStream().map(this::getModel).filter(a -> !Objects.isNull(a)).collect(Collectors.toList());
                n = getComposedModelFromList(data);
            } else if (CollectionUtils.isNotEmpty(schema.getAnyOf())) {
                Optional<Object> value = schema.getAnyOf().parallelStream().map(this::getModel).filter(a -> !Objects.isNull(a)).findFirst();
                if (value.isPresent()) {
                    n = value.get();
                }
            } else if (CollectionUtils.isNotEmpty(schema.getOneOf())) {
                Optional<Object> value = schema.getOneOf().parallelStream().map(this::getModel).filter(a -> !Objects.isNull(a)).findFirst();
                if (value.isPresent()) {
                    n = value.get();
                }
            }
        } catch (Exception ex) {
            n = null;
        }
        if (MapUtils.isNotEmpty(schema.getProperties())) {
            ObjectNode node = n instanceof ObjectNode ? (ObjectNode) n : ObjectMapperUtils.getMapper().createObjectNode();
            schema.getProperties().forEach((k, sch) -> node.putPOJO(k, getModel(sch)));
            return node;
        } else if (!Objects.isNull(n)) {
            return n;
        }
        return schema.getType();
    }


    /**
     * Combines all the fields from the composed model into a single ObjectNode if the data is a list of ObjectNode
     * else returns only the first Object from the data.
     *
     * @param data List of models from 'allOf' attribute in Composed Model.
     * @return return combined attributes from the list of model if they are instances of objectNode
     * else return the first Object from the list of objects.
     */
    private Object getComposedModelFromList(List<Object> data) {
        ObjectNode node = ObjectMapperUtils.getMapper().createObjectNode();
        data.stream().filter(d -> d instanceof ObjectNode).forEach(d -> node.setAll((ObjectNode) d));
        if (!node.isEmpty()) {
            return node;
        } else {
            Optional<Object> v = data.stream().filter(d -> !Objects.isNull(d)).findFirst();
            return v.orElse(null);
        }
    }
}
