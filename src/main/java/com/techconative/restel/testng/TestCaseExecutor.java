package com.techconative.restel.testng;

import static java.util.stream.Collectors.toList;

import com.techconative.restel.core.managers.RequestManager;
import com.techconative.restel.core.managers.RestelDefinitionManager;
import com.techconative.restel.core.managers.RestelTestManager;
import com.techconative.restel.core.model.*;
import com.techconative.restel.core.model.functions.RestelFunction;
import com.techconative.restel.core.resolver.assertion.RestelAssertionResolver;
import com.techconative.restel.core.resolver.function.RestelFunctionExecutor;
import com.techconative.restel.exception.InvalidConfigException;
import com.techconative.restel.exception.RestelException;
import com.techconative.restel.utils.Constants;
import com.techconative.restel.utils.Utils;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * Executor takes care of resolving the variables, making API call along with the configured
 * middlewares and does the testing as configured in the {@link RestelTestApiDefinition}.
 */
@Slf4j
public class TestCaseExecutor {
  @Autowired private RequestManager requestManager;

  @Autowired private RestelTestManager testManager;

  @Autowired private MatcherFactory matcherFactory;

  private List<RestelApiDefinition> testDefinition;

  private RestelTestScenario testExecutionDefinition;

  private String executionName;

  private TestContext testContext;

  public TestCaseExecutor(String executionName) {
    this.executionName = executionName;
    testContext = new TestContext(executionName);
  }

  @PostConstruct
  private void init() {

    if (!StringUtils.isEmpty(testManager.getBaseConfig().getBaseUrl())) {
      requestManager = new RequestManager(testManager.getBaseConfig().getBaseUrl());
    } else {
      throw new InvalidConfigException("BASEURL_INVALID", executionName);
    }

    testExecutionDefinition = testManager.getScenario(executionName);

    if (Objects.isNull(testExecutionDefinition)) {
      throw new InvalidConfigException("INVALID_EXEC_NAME", executionName);
    }

    String suiteName = testExecutionDefinition.getTestSuiteName();

    if (Objects.isNull(testExecutionDefinition.getTestApis())) {
      throw new InvalidConfigException(
          "INVALID_DEF_NAME", testExecutionDefinition.getScenarioName());
    }

    testDefinition =
        testExecutionDefinition.getTestApis().stream()
            .map((ts) -> testManager.getTestMethod(ts))
            .collect(toList());

    RestelSuite testSuite = testManager.getTestSuite(suiteName);

    if (testSuite == null) {
      throw new InvalidConfigException("INVALID_SUITE_NAME", suiteName);
    }

    append(testContext, testSuite.getSuiteParams());
    if (MapUtils.isNotEmpty(testExecutionDefinition.getExecutionParams())
        && MapUtils.isNotEmpty(testSuite.getSuiteParams())) {
      // validate if same param name exists in both test suite and test suite execution
      testExecutionDefinition
          .getExecutionParams()
          .keySet()
          .forEach(
              key -> {
                if (testSuite.getSuiteParams().keySet().contains(key)) {
                  throw new RestelException(
                      "SAME_NAME_IN_SUITE_EXEC",
                      key,
                      testExecutionDefinition.getScenarioName(),
                      testSuite.getSuiteName());
                }
              });
    }
    append(testContext, testExecutionDefinition.getExecutionParams());
  }

  public RestelTestScenario getExecutionGroup() {
    return testExecutionDefinition;
  }

  /**
   * Appends the second map to the first one if the second one is not null.
   *
   * @param srcMap The src map to which the other map to be added.
   * @param mapToAppend The map to be appended
   */
  private void append(TestContext srcMap, Map<String, Object> mapToAppend) {
    if (!CollectionUtils.isEmpty(mapToAppend)) {
      srcMap.putAll(mapToAppend);
    }
  }

  /**
   * Make the API call and execute the test corresponding to the given test name.
   *
   * @return true when the test passes. False otherwise
   */
  public boolean executeTest() {

    // TODO : Conceptualize functions and assertions at scenario level.
    //    executeFunctions();
    //    if (!CollectionUtils.isEmpty(testExecutionDefinition.getAssertions())) {
    //      executeAssertions();
    //    }
    RestelDefinitionManager manager =
        new RestelDefinitionManager(testDefinition, requestManager, matcherFactory, testContext);
    return manager.executeTestScenario(
        testExecutionDefinition.getScenarioName(), testExecutionDefinition.getTestSuiteName());
  }

  private void executeAssertions() {
    testExecutionDefinition
        .getAssertions()
        .forEach(
            a -> {
              if (a.getActual().matches(".*" + Constants.VARIABLE_PATTERN + ".*")) {
                validateFunctionDataPattern(Utils.removeBraces(a.getActual()));
              }
              if (StringUtils.isNotEmpty(a.getExpected())
                  && a.getExpected().matches(".*" + Constants.VARIABLE_PATTERN + ".*")) {
                validateFunctionDataPattern(Utils.removeBraces(a.getExpected()));
              }
              RestelAssertionResolver.resolve(testContext, a);
            });
  }

  /** Execute the {@link RestelFunction} and append the variables to the testContext. */
  private void executeFunctions() {
    if (MapUtils.isNotEmpty(testExecutionDefinition.getFunctions())) {
      Map<String, Object> data =
          testExecutionDefinition.getFunctions().entrySet().stream()
              .collect(Collectors.toMap(Map.Entry::getKey, e -> execFunction(e.getValue())));
      data.keySet()
          .forEach(
              name -> {
                if (testContext.getContextValues().containsKey(name)) {
                  throw new RestelException("FUN_NAME_TAKEN", name);
                }
              });
      append(testContext, data);
    }
  }

  /**
   * Executes the Restel Functions from the {@link TestContext} data and return the results.
   *
   * @param function {@link RestelFunction}
   * @return Executes the function and return the results.
   */
  private Object execFunction(RestelFunction function) {
    if (function.getData().matches(".*" + Constants.VARIABLE_PATTERN + ".*")) {
      validateFunctionDataPattern(Utils.removeBraces(function.getData()));
    }
    RestelFunctionExecutor functionExecutor = new RestelFunctionExecutor(executionName);
    switch (function.getOperation()) {
      case ADD:
        return new RestelException("TO_BE_IMP");
      case REMOVE:
        return functionExecutor.execRemoveFunction(function);
      default:
        throw new RestelException(
            "INVALID_FUN_OP", function.getOperation(), testExecutionDefinition.getScenarioName());
    }
  }

  /**
   * Validates if follows the pattern of format :- Eg: get_user_exec.get_user.response.userGroup or
   * get_user_exec.get_user.request.userGroup. else throws an Exception.
   *
   * @param data is a pattern for validates if its of the format:- Eg:
   *     get_user_exec.get_user.response.userGroup or get_user_exec.get_user.request.userGroup .
   */
  private void validateFunctionDataPattern(String data) {
    String[] variables = data.split(Constants.NS_SEPARATOR_REGEX, 2);
    String msg = "INVALID_PAYLOAD_SYNTAX";
    // Check if the execution name exists.
    if (!hasExecutionName(testExecutionDefinition.getDependsOn(), variables[0])) {
      throw new RestelException(msg, data, variables[0], testExecutionDefinition.getScenarioName());
    }
    // Check if the test definition exists
    String[] tokens = variables[1].split(Constants.NS_SEPARATOR_REGEX, 2);
    if (!hasDefinitionName(
        testManager.getTestMethod(
            // TODO: Testrun
            testManager.getScenario(variables[0]).getTestApis().get(0)),
        tokens[0])) {
      throw new RestelException(msg, data, tokens[0], testExecutionDefinition.getScenarioName());
    }

    // check the corresponding token has request or response
    if (!StringUtils.startsWithIgnoreCase(tokens[1], Constants.RESPONSE)
        && !StringUtils.startsWithIgnoreCase(tokens[1], Constants.REQUEST)) {
      throw new RestelException(msg, data, tokens[0], testExecutionDefinition.getScenarioName());
    }
  }

  /**
   * @param testDefinitions {@link RestelTestApiDefinition}
   * @param definitionName name of the test definition which needs to be checked if its belongs to
   *     the given test definition or its child test definition
   * @return Check whether the definitionName is equals to given tesDefinitions or its child
   *     testDefinitions,
   */
  private boolean hasDefinitionName(RestelApiDefinition testDefinitions, String definitionName) {
    if (StringUtils.equals(testDefinitions.getCaseUniqueName(), definitionName)) {
      return true;
    } else {
      for (RestelApiDefinition testMethod : testDefinitions.getDependentOn()) {
        if (hasDefinitionName(testMethod, definitionName)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * @param executionGroups List of {@link RestelTestScenario}
   * @param executionName name of the test suite execution.
   * @return validates if the executionName is present in the executionGroups or its child
   *     executionGroups (depends on elements)
   */
  private boolean hasExecutionName(List<RestelTestScenario> executionGroups, String executionName) {
    if (CollectionUtils.isEmpty(executionGroups)) {
      return false;
    } else {
      // Check if the variable exists in the dependent list.
      boolean hasValue =
          executionGroups.stream()
                  .filter(
                      restelExecutionGroup ->
                          StringUtils.equals(restelExecutionGroup.getScenarioName(), executionName))
                  .count()
              > 0;
      if (!hasValue) {
        for (RestelTestScenario executionGroup : executionGroups) {
          // Check inside the child executions.
          if (hasExecutionName(executionGroup.getDependsOn(), executionName)) {
            return true;
          }
        }
      } else {
        return true;
      }
    }
    return false;
  }
}
