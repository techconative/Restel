package com.techconative.restel.oas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.techconative.restel.core.parser.dto.BaseConfig;
import com.techconative.restel.core.parser.dto.TestDefinitions;
import com.techconative.restel.utils.Constants;
import com.techconative.restel.utils.ObjectMapperUtils;
import com.techconative.restel.utils.Utils;
import io.swagger.models.*;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.core.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

/** Parse OpenAPI spec 2.0 to Restel Models */
@Slf4j
public class RestelOpenApiSpec2Parser {
  private Swagger parser;

  private static String definitionsRegex = "^#/definitions/(.*)";

  public RestelOpenApiSpec2Parser(Swagger parser) {
    this.parser = parser;
  }

  /**
   * Create {@link BaseConfig} with base_url
   *
   * @return {@link BaseConfig}
   */
  public BaseConfig createBaseConfig() {
    BaseConfig baseConfig = new BaseConfig();
    // add title
    if (!Objects.isNull(parser.getInfo())) {
      baseConfig.setAppName(parser.getInfo().getTitle());
    }
    // add base_url
    if (CollectionUtils.isNotEmpty(parser.getSchemes())) {
      for (Scheme scheme : parser.getSchemes()) {
        if (!Objects.isNull(scheme) && StringUtils.isNotBlank(parser.getHost())) {
          baseConfig.setBaseUrl(
              StringUtils.isNotBlank(parser.getBasePath())
                  ? scheme
                      .toValue()
                      .concat("://")
                      .concat(parser.getHost())
                      .concat(parser.getBasePath())
                  : scheme.toValue().concat("://").concat(parser.getHost()));
          break;
        }
      }
    }
    Map<String, String> defaultHeaders = new HashMap<>();
    if (!CollectionUtils.isEmpty(parser.getConsumes())) {
      defaultHeaders.put(HttpHeaders.CONTENT_TYPE, parser.getConsumes().get(0));
    }
    if (!CollectionUtils.isEmpty(parser.getProduces())) {
      defaultHeaders.put(HttpHeaders.ACCEPT, parser.getProduces().get(0));
    }
    baseConfig.setDefaultHeader(Utils.mapToString(defaultHeaders));
    return baseConfig;
  }

  /**
   * Create TestDefinitions from the open api parser
   *
   * @return List of {@link TestDefinitions}
   */
  public List<TestDefinitions> createTestDefinitions() {
    List<TestDefinitions> testDefinitions = new ArrayList<>();
    if (MapUtils.isNotEmpty(parser.getPaths())) {
      for (String path : parser.getPaths().keySet()) {
        List<Parameter> parameters = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(parser.getPaths().get(path).getParameters())) {
          parameters.addAll(parser.getPaths().get(path).getParameters());
        }
        testDefinitions.addAll(
            parser.getPaths().get(path).getOperationMap().entrySet().stream()
                .map(operationEntry -> createTestDef(path, operationEntry, parameters))
                .collect(Collectors.toList()));
      }
    }
    return testDefinitions;
  }

  /**
   * @param path endpoint name.
   * @param operationEntry Map.entity of endpoint's HttpMethod and {@link Operation} details
   * @param parameters List of endpoint level parameters.
   * @return returns Test Definition.
   */
  private TestDefinitions createTestDef(
      String path, Map.Entry<HttpMethod, Operation> operationEntry, List<Parameter> parameters) {
    TestDefinitions def = new TestDefinitions();
    def.setCaseUniqueName(path.concat(":").concat(operationEntry.getKey().name()));
    def.setCaseDescription(operationEntry.getValue().getDescription());
    def.setRequestUrl(path);
    def.setRequestMethod(operationEntry.getKey().name());
    if (CollectionUtils.isNotEmpty(operationEntry.getValue().getTags())) {
      def.setTags(new HashSet<>(operationEntry.getValue().getTags()));
    }
    def.setAcceptedStatusCodes(new ArrayList<>(operationEntry.getValue().getResponses().keySet()));

    if (CollectionUtils.isNotEmpty(operationEntry.getValue().getParameters())) {
      parameters.addAll(operationEntry.getValue().getParameters());
    }

    def.setRequestQueryParams(
        getParams(
                parameters.stream()
                    .filter(par -> par instanceof QueryParameter)
                    .map(par -> (QueryParameter) par)
                    .collect(Collectors.toList()))
            .toPrettyString());

    ObjectNode headers =
        getParams(
            parameters.stream()
                .filter(par -> par instanceof HeaderParameter)
                .map(par -> (HeaderParameter) par)
                .collect(Collectors.toList()));
    // add consumes and produces into headers.
    if (CollectionUtils.isNotEmpty(operationEntry.getValue().getConsumes())) {
      headers.put(HttpHeaders.CONTENT_TYPE, operationEntry.getValue().getConsumes().get(0));
    }
    if (CollectionUtils.isNotEmpty(operationEntry.getValue().getProduces())) {
      headers.put(HttpHeaders.ACCEPT, operationEntry.getValue().getProduces().get(0));
    }
    def.setRequestHeaders(headers.toPrettyString());

    boolean hasLocalBodyParams = false;
    boolean hasLocalFormParams = false;
    if (CollectionUtils.isNotEmpty(operationEntry.getValue().getParameters())) {
      parameters.addAll(operationEntry.getValue().getParameters());
      hasLocalBodyParams =
          operationEntry.getValue().getParameters().stream()
                  .filter(par -> par instanceof BodyParameter)
                  .count()
              > 0;
      hasLocalFormParams =
          operationEntry.getValue().getParameters().stream()
                  .filter(par -> par instanceof FormParameter)
                  .count()
              > 0;
    }

    ObjectNode formParams =
        getParams(
            parameters.stream()
                .filter(par -> par instanceof FormParameter)
                .map(par -> (FormParameter) par)
                .collect(Collectors.toList()));
    JsonNode bodyParams =
        getBodyParams(
            parameters.stream()
                .filter(par -> par instanceof BodyParameter)
                .map(par -> (BodyParameter) par)
                .collect(Collectors.toList()));

    if (hasLocalBodyParams) {
      // if has local body params add them first
      def.setRequestBodyParams(
          getBodyParams(
                  operationEntry.getValue().getParameters().stream()
                      .filter(par -> par instanceof BodyParameter)
                      .map(par -> (BodyParameter) par)
                      .collect(Collectors.toList()))
              .toPrettyString());
    } else if (hasLocalFormParams) {
      // else if has local Form Data params add both local and global.
      def.setRequestBodyParams(formParams.toPrettyString());
    } else if (!bodyParams.isEmpty()) {
      // else if add global Body params
      def.setRequestBodyParams(bodyParams.toPrettyString());
    } else if (!formParams.isEmpty()) {
      // add global form params
      def.setRequestBodyParams(formParams.toPrettyString());
    }

    // add response body
    def.setExpectedResponse(getResponse(operationEntry.getValue().getResponses()).toPrettyString());

    // add response headers
    def.setExpectedHeader(
        getResponseHeader(operationEntry.getValue().getResponses()).toPrettyString());

    return def;
  }

  /**
   * @param responses Map of status code to {@link Response}
   * @return return response headers of status code 200 if present, else default response headers or
   *     else first entities response headers.
   */
  private ObjectNode getResponseHeader(Map<String, Response> responses) {
    if (!Objects.isNull(responses.get(String.valueOf(HttpStatus.SC_OK)))) {
      return getResponseHeader(responses.get(String.valueOf(HttpStatus.SC_OK)));
    } else if (!Objects.isNull(responses.get(Constants.DEFAULT))) {
      return getResponseHeader(responses.get(Constants.DEFAULT));
    } else {
      for (Map.Entry<String, Response> entry : responses.entrySet()) {
        ObjectNode resp = getResponseHeader(entry.getValue());
        if (!resp.isEmpty()) {
          return resp;
        }
      }
    }
    return ObjectMapperUtils.getMapper().createObjectNode();
  }

  /**
   * @param response {@link Response}
   * @return return an ObjectNode with response headers.
   */
  private ObjectNode getResponseHeader(Response response) {
    ObjectNode node = ObjectMapperUtils.getMapper().createObjectNode();
    if (MapUtils.isNotEmpty(response.getHeaders())) {
      response.getHeaders().forEach((k, p) -> node.putPOJO(k, getProperty(p)));
    }
    return node;
  }

  /**
   * @param responses Map of status code to {@link Response}
   * @return return response model of status code 200 if present, else default response model or
   *     else first entities (response model) in the response map.
   */
  private JsonNode getResponse(Map<String, Response> responses) {
    Object resp = null;
    if (!Objects.isNull(responses.get(String.valueOf(HttpStatus.SC_OK)))) {
      resp = getModel(responses.get(String.valueOf(HttpStatus.SC_OK)).getResponseSchema());
    } else if (!Objects.isNull(responses.get(Constants.DEFAULT))) {
      resp = getModel(responses.get(Constants.DEFAULT).getResponseSchema());
    } else {
      for (Map.Entry<String, Response> media : responses.entrySet()) {
        resp = getModel(media.getValue().getResponseSchema());
        if (!Objects.isNull(resp)) {
          break;
        }
      }
    }
    return eval(resp);
  }

  /**
   * @param parameters List of {@link BodyParameter}
   * @return return the body params.
   */
  private JsonNode getBodyParams(List<BodyParameter> parameters) {
    if (CollectionUtils.isNotEmpty(parameters)) {
      Optional<Object> data =
          parameters.stream()
              .filter(p -> !Objects.isNull(p.getSchema()))
              .map(par -> getModel(par.getSchema()))
              .findFirst();
      if (data.isPresent()) {
        return eval(data.get());
      }
    }
    return ObjectMapperUtils.getMapper().createObjectNode();
  }

  /**
   * @param output should be one of ObjectNode or ArrayNode
   * @return check if the output is ObjectNode or ArrayNode and returns. If non of those values then
   *     it will return empty ObjectNode
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
   * @param parameters list of {@link AbstractSerializableParameter}.
   * @return returns an ObjectNode with the list of parameters.
   */
  private ObjectNode getParams(List<? extends AbstractSerializableParameter> parameters) {
    ObjectNode params = ObjectMapperUtils.getMapper().createObjectNode();
    if (CollectionUtils.isNotEmpty(parameters)) {
      parameters.forEach(
          par -> {
            if (!Objects.isNull(par.getDefaultValue())) {
              params.put(par.getName(), par.getDefaultValue().toString());
            } else if (!Objects.isNull(par.getItems())) {
              params.putPOJO(par.getName(), getProperty(par.getItems()));
            } else {
              params.put(par.getName(), par.getType());
            }
          });
    }
    return params;
  }

  /**
   * Constructs the Json schema from the property. If the property is a primitive type will return
   * only property's type (like string,integer,etc..).
   *
   * @param property {@link Property}
   * @return returns a JsonNode of json schema from property. If the property is primitive type,
   *     returns string value of property type.
   */
  private Object getProperty(Property property) {
    if (property instanceof ObjectProperty) {
      ObjectNode n = ObjectMapperUtils.getMapper().createObjectNode();
      ((ObjectProperty) property)
          .getProperties()
          .forEach((key, value) -> n.putPOJO(key, getProperty(value)));
      return n;
    } else if (property instanceof RefProperty && validRef(((RefProperty) property).get$ref())) {
      return getModel(parser.getDefinitions().get(parseRef(((RefProperty) property).get$ref())));

    } else if (property instanceof ArrayProperty) {
      return ObjectMapperUtils.getMapper()
          .createArrayNode()
          .addPOJO(getProperty(((ArrayProperty) property).getItems()));
    } else if (property instanceof ComposedProperty) {
      return getComposedModelFromList(
          ((ComposedProperty) property)
              .getAllOf().stream().map(this::getProperty).collect(Collectors.toList()));
    } else {
      return property.getType();
    }
  }

  /**
   * get Object node from the model.
   *
   * @param model {@link Model}
   */
  private Object getModel(Model model) {
    if (Objects.isNull(model)) return null;
    if (model instanceof RefModel && validRef(((RefModel) model).get$ref())) {
      return getModel(parser.getDefinitions().get(parseRef(((RefModel) model).get$ref())));
    } else if (model instanceof ArrayModel) {
      return ObjectMapperUtils.getMapper()
          .createArrayNode()
          .addPOJO(getProperty(((ArrayModel) model).getItems()));
    } else if (model instanceof ComposedModel) {
      Object data =
          getComposedModelFromList(
              ((ComposedModel) model)
                  .getAllOf().stream().map(this::getModel).collect(Collectors.toList()));
      if (data instanceof ObjectNode && MapUtils.isNotEmpty(model.getProperties())) {
        model.getProperties().forEach((k, v) -> ((ObjectNode) data).putPOJO(k, getProperty(v)));
      }
      return data;
    } else if (MapUtils.isNotEmpty(model.getProperties())) {
      ObjectNode n = ObjectMapperUtils.getMapper().createObjectNode();
      model.getProperties().forEach((k, v) -> n.putPOJO(k, getProperty(v)));
      return n;
    }
    return null;
  }

  /**
   * regex pattern to capture model's name in definitions.
   *
   * @param ref Reference String format:#/definitions/<name> . eg: #/definitions/Pet
   * @return either returns definition's model name ( from example 'Pet') else return null.
   */
  private String parseRef(String ref) {
    try {
      Matcher matcher = Pattern.compile(definitionsRegex).matcher(ref);
      if (matcher.find()) {
        return matcher.group(1);
      }
    } catch (Exception ex) {
      log.warn("invalid Schema reference format: " + ref);
    }
    return null;
  }

  /**
   * validates if the ref value is valid or not. Should be of format:#/definitions/<name> . eg:
   * #/definitions/Pet. and the model should exist in the open api definitions.
   *
   * @param ref Reference String format:#/definitions/<name> . eg: #/definitions/Pet
   * @return boolean
   */
  private boolean validRef(String ref) {
    return StringUtils.isNotEmpty(ref) && StringUtils.isNotEmpty(parseRef(ref));
  }

  /**
   * Combines all the fields from the composed model into a single ObjectNode if the data is a list
   * of ObjectNode else returns only the first Object from the data.
   *
   * @param data List of models from 'allOf' attribute in Composed Model.
   * @return return combined attributes from the list of model if they are instances of objectNode
   *     else return the first Object from the list of objects.
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
