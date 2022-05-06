package com.techconative.restel.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.techconative.restel.core.model.*;
import com.techconative.restel.core.model.assertion.AssertType;
import com.techconative.restel.core.model.assertion.RestelAssertion;
import com.techconative.restel.core.model.functions.FunctionOps;
import com.techconative.restel.core.model.functions.RestelFunction;
import com.techconative.restel.core.parser.dto.*;
import com.techconative.restel.core.parser.dto.BaseConfig;
import com.techconative.restel.core.parser.dto.TestApiDefinitions;
import com.techconative.restel.core.parser.dto.TestScenarios;
import com.techconative.restel.core.parser.dto.TestSuites;
import com.techconative.restel.core.utils.ContextUtils;
import com.techconative.restel.exception.RestelException;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

public class RestelUtils {

  private RestelUtils() {}

  /**
   * creates ReselTestMethod from testDefinition.
   *
   * @param testDefinition The {@link TestApiDefinitions} Object.
   * @return {@link RestelTestApiDefinition}
   */
  public static RestelTestApiDefinition createTestMethod(
      TestApiDefinitions testDefinition, BaseConfiguration baseConfig) {
    validate(testDefinition);
    RestelTestApiDefinition testMethod = new RestelTestApiDefinition();
    testMethod.setCaseUniqueName(testDefinition.getApiUniqueName());
    testMethod.setCaseDescription(testDefinition.getApiDescription());

    testMethod.setRequestUrl(testDefinition.getRequestUrl());
    testMethod.setRequestMethod(testDefinition.getRequestMethod());

    Map<String, Object> headers = new HashMap<>();
    if (!MapUtils.isEmpty(baseConfig.getDefaultHeader())) {
      headers.putAll(baseConfig.getDefaultHeader());
    }
    if (StringUtils.isNotEmpty(testDefinition.getRequestHeaders())) {
      headers.putAll(ObjectMapperUtils.convertToMap(testDefinition.getRequestHeaders()));
    }
    testMethod.setRequestHeaders(headers);

    if (StringUtils.isNotEmpty(testDefinition.getRequestQueryParams())) {
      testMethod.setRequestQueryParams(
          ObjectMapperUtils.convertToMap(testDefinition.getRequestQueryParams()));
    }
    if (StringUtils.isNotEmpty(testDefinition.getRequestBodyParams())) {
      testMethod.setRequestBodyParams(testDefinition.getRequestBodyParams());
    }

    if (StringUtils.isNotEmpty(testDefinition.getRequestPreCallHook())) {
      testMethod.setRequestPreCallHook(
          ObjectMapperUtils.convertToJsonNode(testDefinition.getRequestPreCallHook()));
    }
    if (StringUtils.isNotEmpty(testDefinition.getRequestPostCallHook())) {
      testMethod.setRequestPostCallHook(
          ObjectMapperUtils.convertToJsonNode(testDefinition.getRequestPostCallHook()));
    }
    if (StringUtils.isNotEmpty(testDefinition.getExpectedResponse())) {
      testMethod.setExpectedResponse(testDefinition.getExpectedResponse());
    }
    if (StringUtils.isNotEmpty(testDefinition.getExpectedHeader())) {
      testMethod.setExpectedHeader(
          ObjectMapperUtils.convertToMap(testDefinition.getExpectedHeader()));
    }

    testMethod.setExpectedResponseMatcher(testDefinition.getExpectedResponseMatcher());
    testMethod.setExpectedHeaderMatcher(testDefinition.getExpectedHeaderMatcher());
    if (testDefinition.getAcceptedStatusCodes() != null) {
      testMethod.setAcceptedStatusCodes(testDefinition.getAcceptedStatusCodes());
    }
    return testMethod;
  }

  public static RestelTestApiWrapper createTestWrapper(
      TestApiWrappers testWrapper, Map<String, RestelTestApiDefinition> testMethodMap) {
    validate(testWrapper);
    Map<String, Object> params =
        StringUtils.isEmpty(testWrapper.getTestApiWrapperParameters())
            ? null
            : ObjectMapperUtils.convertToMap(testWrapper.getTestApiWrapperParameters());
    RestelTestApiWrapper restelTestWrapper = new RestelTestApiWrapper();
    restelTestWrapper.setTestApiWrapperName(testWrapper.getTestApiWrapperName());
    restelTestWrapper.setTestApiWrapperDescription(testWrapper.getTestApiWrapperDescription());
    restelTestWrapper.setTestApiDefinition(testMethodMap.get(testWrapper.getTestApiName()));
    restelTestWrapper.setApiParameters(params);
    return restelTestWrapper;
  }

  /**
   * Creates a Restelsuite from testSuite
   *
   * @param suite {@link TestSuites} object
   * @return {@link RestelSuite} object
   */
  public static RestelSuite createSuite(TestSuites suite) {
    validate(suite);
    Map<String, Object> params =
        StringUtils.isEmpty(suite.getSuiteParams())
            ? null
            : ObjectMapperUtils.convertToMap(suite.getSuiteParams());
    Boolean enable = suite.getSuiteEnable() == null ? Boolean.TRUE : suite.getSuiteEnable();
    RestelSuite restelSuite = new RestelSuite();
    restelSuite.setSuiteName(suite.getSuiteUniqueName());
    restelSuite.setSuiteDescription(suite.getSuiteDescription());
    restelSuite.setSuiteParams(params);
    restelSuite.setSuiteEnable(enable);
    return restelSuite;
  }

  /**
   * creates an Restel Test execution from TestSuiteExecution.
   *
   * @param scenarios {@link TestScenarios} objects.
   * @return {@link RestelTestScenario}
   */
  public static RestelTestScenario createTestScenario(TestScenarios scenarios) {
    validate(scenarios);
    Map<String, Object> params =
        StringUtils.isEmpty(scenarios.getScenarioParams())
            ? null
            : ObjectMapperUtils.convertToMap(scenarios.getScenarioParams());
    Boolean enable =
        scenarios.getScenarioEnabled() == null ? Boolean.TRUE : scenarios.getScenarioEnabled();
    RestelTestScenario restelExecutionGroup = new RestelTestScenario();
    restelExecutionGroup.setScenarioName(scenarios.getScenarioUniqueName());
    restelExecutionGroup.setScenarioDescription(scenarios.getScenarioDescription());
    restelExecutionGroup.setTestApis(scenarios.getTestApis());
    restelExecutionGroup.setScenarioEnabled(enable);
    restelExecutionGroup.setTestSuiteName(scenarios.getTestSuite());
    restelExecutionGroup.setExecutionParams(params);
    if (StringUtils.isNotBlank(scenarios.getFunction())) {
      restelExecutionGroup.setFunctions(
          convertFunctions(scenarios.getScenarioUniqueName(), scenarios.getFunction()));
    }
    if (StringUtils.isNotBlank(scenarios.getAssertion())) {
      restelExecutionGroup.setAssertions(
          convertAssertion(scenarios.getScenarioUniqueName(), scenarios.getAssertion()));
    }
    return restelExecutionGroup;
  }

  private static List<RestelAssertion> convertAssertion(
      String testExecutionUniqueName, String assertions) {
    if (ObjectMapperUtils.isJSONValid(assertions)) {
      return ObjectMapperUtils.convertToMap(assertions).entrySet().stream()
          .map(e -> getAssertion(testExecutionUniqueName, e))
          .collect(Collectors.toList());
    } else {
      throw new RestelException("ASSERT_INVALID_SYNTAX", testExecutionUniqueName);
    }
  }

  private static RestelAssertion getAssertion(
      String testExecutionUniqueName, Map.Entry<String, Object> assertion) {
    RestelAssertion restelAssertion = new RestelAssertion();
    JsonNode ass = ObjectMapperUtils.convertToJsonNode(assertion.getValue());
    restelAssertion.setName(assertion.getKey());
    if (!Objects.isNull(ass.get(Constants.CONDITION)) && ass.get(Constants.CONDITION).isArray()) {
      ArrayNode cond = (ArrayNode) ass.get(Constants.CONDITION);
      restelAssertion.setAssertType(AssertType.getType(cond.get(0).asText()));
      restelAssertion.setActual(cond.get(1).asText());
      if (cond.size() > 2) {
        restelAssertion.setExpected(cond.get(2).asText());
      }
    } else {
      throw new RestelException(
          "ASSERT_INVALID_SYNTAX_WITH_KEY", assertion, assertion.getKey(), testExecutionUniqueName);
    }
    if (!Objects.isNull(ass.get(Constants.MESSAGE))) {
      restelAssertion.setMessage(ass.get(Constants.MESSAGE).asText());
    }
    return restelAssertion;
  }

  private static Map<String, RestelFunction> convertFunctions(String name, String functionMap) {
    if (ObjectMapperUtils.isJSONValid(functionMap)) {
      return ObjectMapperUtils.convertToMap(functionMap).entrySet().stream()
          .collect(Collectors.toMap(Map.Entry::getKey, e -> getFunction(name, e.getValue())));
    } else {
      throw new RestelException("INVALID_FUN", name);
    }
  }

  /**
   * @param name Test execution name
   * @param function function in string format
   * @return {@link RestelFunction}
   */
  private static RestelFunction getFunction(String name, Object function) {
    RestelFunction restelFunction = new RestelFunction();
    JsonNode func = ObjectMapperUtils.convertToJsonNode(function);
    restelFunction.setData(func.get(Constants.DATA).asText());
    if (!Objects.isNull(func.get(Constants.ARGS))) {
      restelFunction.setArgs(
          Arrays.asList(func.get(Constants.ARGS).asText().split(Constants.COMMA)));
    }
    if (StringUtils.startsWithIgnoreCase(
        func.get(Constants.OPERATION).asText(), Constants.REMOVE)) {
      restelFunction.setOperation(FunctionOps.REMOVE);
    } else if (StringUtils.startsWithIgnoreCase(
        func.get(Constants.OPERATION).asText(), Constants.ADD)) {
      restelFunction.setOperation(FunctionOps.ADD);
    } else {
      throw new RestelException("INVALID_FUN_SYNTAX", function, name);
    }
    return restelFunction;
  }

  public static BaseConfiguration createBaseConfig(BaseConfig config) {
    Map<String, Object> defaultHeaders =
        StringUtils.isEmpty(config.getDefaultHeader())
            ? null
            : ObjectMapperUtils.convertToMap(config.getDefaultHeader());
    if (StringUtils.isBlank(config.getBaseUrl())) {
      throw new RestelException("BASEURL_EMPTY");
    }
    String replaceContextVariables =
        ContextUtils.replaceContextVariables(GlobalContext.getInstance(), config.getBaseUrl())
            .toString();
    return BaseConfiguration.builder()
        .baseUrl(replaceContextVariables)
        .defaultHeader(defaultHeaders)
        .build();
  }

  private static void validate(TestApiDefinitions testApiDefinitions) {
    if (StringUtils.isEmpty(testApiDefinitions.getApiUniqueName())) {
      throw new RestelException("DEF_NAME_EMPTY");
    }
    if (StringUtils.isEmpty(testApiDefinitions.getRequestUrl())) {
      throw new RestelException("DEF_URL_EMPTY", testApiDefinitions.getApiUniqueName());
    }
    if (StringUtils.isEmpty(testApiDefinitions.getRequestMethod())) {
      throw new RestelException("DEF_METHOD_EMPTY", testApiDefinitions.getApiUniqueName());
    }
    if (StringUtils.isEmpty(testApiDefinitions.getExpectedResponseMatcher())) {
      throw new RestelException("DEF_RES_MATCHER_EMPTY", testApiDefinitions.getApiUniqueName());
    }
    if (StringUtils.isEmpty(testApiDefinitions.getExpectedHeaderMatcher())) {
      throw new RestelException("DEF_HEAD_MATCHER_EMPTY", testApiDefinitions.getApiUniqueName());
    }
    if (CollectionUtils.isEmpty(testApiDefinitions.getAcceptedStatusCodes())) {
      throw new RestelException("DEF_STATUS_MATCHER_EMPTY", testApiDefinitions.getApiUniqueName());
    }
  }

  private static void validate(TestApiWrappers testApiWrappers) {
    if (StringUtils.isEmpty(testApiWrappers.getTestApiName())) {
      throw new RestelException("TEST_API_NAME_EMPTY");
    }
    if (StringUtils.isEmpty(testApiWrappers.getTestApiWrapperName())) {
      throw new RestelException("TEST_API_WRAPPER_NAME_EMPTY");
    }
    if (StringUtils.isEmpty(testApiWrappers.getTestApiWrapperDescription())) {
      throw new RestelException("TEST_API_WRAPPER_DESC_EMPTY");
    }
    if (StringUtils.isEmpty(testApiWrappers.getTestApiWrapperParameters())) {
      throw new RestelException("TEST_API_WRAPPER_PARAM_EMPTY");
    }
  }

  private static void validate(TestSuites testSuites) {
    if (StringUtils.isEmpty(testSuites.getSuiteUniqueName())) {
      throw new RestelException("SUITE_NAME_EMPTY");
    }
  }

  private static void validate(TestScenarios testScenarios) {
    if (StringUtils.isEmpty(testScenarios.getScenarioUniqueName())) {
      throw new RestelException("EXEC_NAME_EMPTY");
    }
    if (StringUtils.isEmpty(testScenarios.getTestSuite())) {
      throw new RestelException("EXEC_SUITE_NAME_EMPTY", testScenarios.getScenarioUniqueName());
    }
    if (CollectionUtils.isEmpty(testScenarios.getTestApis())
        || testScenarios.getTestApis().stream().anyMatch(String::isEmpty)) {
      throw new RestelException("EXEC_DEF_NAME_EMPTY", testScenarios.getScenarioUniqueName());
    }
  }
}
