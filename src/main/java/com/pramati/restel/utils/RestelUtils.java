package com.pramati.restel.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.pramati.restel.core.model.BaseConfiguration;
import com.pramati.restel.core.model.RestelSuite;
import com.pramati.restel.core.model.RestelTestMethod;
import com.pramati.restel.core.model.RestelTestScenario;
import com.pramati.restel.core.model.assertion.AssertType;
import com.pramati.restel.core.model.assertion.RestelAssertion;
import com.pramati.restel.core.model.functions.FunctionOps;
import com.pramati.restel.core.model.functions.RestelFunction;
import com.pramati.restel.core.parser.dto.BaseConfig;
import com.pramati.restel.core.parser.dto.TestDefinitions;
import com.pramati.restel.core.parser.dto.TestScenarios;
import com.pramati.restel.core.parser.dto.TestSuites;
import com.pramati.restel.exception.RestelException;
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
   * @param testDefinition The {@link TestDefinitions} Object.
   * @return {@link RestelTestMethod}
   */
  public static RestelTestMethod createTestMethod(
      TestDefinitions testDefinition, BaseConfiguration baseConfig) {
    validate(testDefinition);
    RestelTestMethod testMethod = new RestelTestMethod();
    testMethod.setCaseUniqueName(testDefinition.getCaseUniqueName());
    testMethod.setCaseDescription(testDefinition.getCaseDescription());

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
      testMethod.setAcceptedStatusCodes(
          testDefinition.getAcceptedStatusCodes().stream()
              .map(a -> Integer.valueOf(a.replace(" ", "")))
              .collect(Collectors.toList()));
    }
    return testMethod;
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
  public static RestelTestScenario createExecutionGroup(TestScenarios scenarios) {
    validate(scenarios);
    Map<String, Object> params =
        StringUtils.isEmpty(scenarios.getScenarioParams())
            ? null
            : ObjectMapperUtils.convertToMap(scenarios.getScenarioParams());
    Boolean enable =
        scenarios.getScenarioEnabled() == null
            ? Boolean.TRUE
            : scenarios.getScenarioEnabled();
    RestelTestScenario restelExecutionGroup = new RestelTestScenario();
    restelExecutionGroup.setScenarioName(scenarios.getScenarioUniqueName());
    restelExecutionGroup.setTestDefinitionName(scenarios.getTestCases());
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
    return BaseConfiguration.builder()
        .baseUrl(config.getBaseUrl())
        .defaultHeader(defaultHeaders)
        .build();
  }

  private static void validate(TestDefinitions testDefinitions) {
    if (StringUtils.isEmpty(testDefinitions.getCaseUniqueName())) {
      throw new RestelException("DEF_NAME_EMPTY");
    }
    if (StringUtils.isEmpty(testDefinitions.getRequestUrl())) {
      throw new RestelException("DEF_URL_EMPTY", testDefinitions.getCaseUniqueName());
    }
    if (StringUtils.isEmpty(testDefinitions.getRequestMethod())) {
      throw new RestelException("DEF_METHOD_EMPTY", testDefinitions.getCaseUniqueName());
    }
    if (StringUtils.isEmpty(testDefinitions.getExpectedResponseMatcher())) {
      throw new RestelException("DEF_RES_MATCHER_EMPTY", testDefinitions.getCaseUniqueName());
    }
    if (StringUtils.isEmpty(testDefinitions.getExpectedHeaderMatcher())) {
      throw new RestelException("DEF_HEAD_MATCHER_EMPTY", testDefinitions.getCaseUniqueName());
    }
    if (CollectionUtils.isEmpty(testDefinitions.getAcceptedStatusCodes())) {
      throw new RestelException("DEF_STATUS_MATCHER_EMPTY", testDefinitions.getCaseUniqueName());
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
      throw new RestelException(
          "EXEC_SUITE_NAME_EMPTY", testScenarios.getScenarioUniqueName());
    }
    if (CollectionUtils.isEmpty(testScenarios.getTestCases())) {
      throw new RestelException("EXEC_DEF_NAME_EMPTY", testScenarios.getScenarioUniqueName());
    }
  }
}
