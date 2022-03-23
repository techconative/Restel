package com.pramati.restel.core.managers;

import com.pramati.restel.core.model.BaseConfiguration;
import com.pramati.restel.core.model.RestelSuite;
import com.pramati.restel.core.model.RestelTestMethod;
import com.pramati.restel.core.model.RestelTestScenario;
import com.pramati.restel.exception.RestelException;
import java.util.*;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Represents the class that manages the Restel tests
 *
 * @author kannanr
 */
@Component
public class RestelTestManager {

  private List<RestelTestMethod> testDefinitions;
  private List<RestelTestScenario> testScenarios;

  private Map<String, RestelTestMethod> indexedTestDefinitions;
  private Map<String, RestelTestScenario> indexedTestScenarios;
  private Map<String, RestelSuite> indexedTestSuites;
  private BaseConfiguration baseConfig;

  @Autowired private ExcelParseManager excelParseManager;

  @PostConstruct
  private void init() {
    configure();
  }

  private void configure() {

    baseConfig = excelParseManager.getBaseConfig();

    testDefinitions = new ArrayList<>();
    testScenarios = new ArrayList<>();
    List<RestelSuite> testSuites = new ArrayList<>();

    indexedTestDefinitions = new HashMap<>();
    indexedTestScenarios = new HashMap<>();
    indexedTestSuites = new HashMap<>();

    for (RestelTestMethod testMethod : excelParseManager.getTestMethods()) {
      indexedTestDefinitions.put(testMethod.getCaseUniqueName(), testMethod);
    }
    for (RestelSuite suite : excelParseManager.getSuites()) {
      indexedTestSuites.put(suite.getSuiteName(), suite);
    }

    for (RestelTestScenario execution : excelParseManager.getExecGroups()) {
      indexedTestScenarios.put(execution.getScenarioName(), execution);
    }

    testDefinitions.addAll(indexedTestDefinitions.values());
    testSuites.addAll(indexedTestSuites.values());
    testScenarios.addAll(indexedTestScenarios.values());
    validateDefinition(testDefinitions);
    validateExecution(testScenarios);
    validateSuite(testSuites);
  }

  /**
   * check if dependency is cyclic. Note: all the restelSuites with dependencies should be Direct
   * Acyclic Graphs.
   *
   * @param restelSuites
   */
  private void validateSuite(List<RestelSuite> restelSuites) {
    restelSuites.forEach(testMethod -> isCyclic(testMethod, testMethod.getDependsOn()));
  }

  /**
   * check if restelSuite has cyclic dependency .
   *
   * @param restelSuite {@link RestelSuite}
   * @param childSuites list of child {@link RestelSuite} for restelSuite.
   */
  private void isCyclic(RestelSuite restelSuite, List<RestelSuite> childSuites) {
    if (!CollectionUtils.isEmpty(childSuites)) {
      childSuites.forEach(
          m -> {
            if (m.getSuiteName().equals(restelSuite.getSuiteName())) {
              throw new RestelException("SUITE_DEPENDENCY_ERROR", restelSuite.getSuiteName());
            } else {
              if (!CollectionUtils.isEmpty(m.getDependsOn())) {
                isCyclic(restelSuite, m.getDependsOn());
              }
            }
          });
    }
  }

  /**
   * check if dependency is cyclic. Note: all the restelExecutionGroups with dependencies should be
   * Direct Acyclic Graphs.
   *
   * @param restelExecutionGroups
   */
  private void validateExecution(List<RestelTestScenario> restelExecutionGroups) {
    restelExecutionGroups.forEach(testMethod -> isCyclic(testMethod, testMethod.getDependsOn()));
  }

  /**
   * check if executionGroup has cyclic dependency .
   *
   * @param executionGroup {@link RestelTestScenario}
   * @param childGroups list of child {@link RestelTestScenario} for executionGroup.
   */
  private void isCyclic(RestelTestScenario executionGroup, List<RestelTestScenario> childGroups) {
    if (!CollectionUtils.isEmpty(childGroups)) {
      childGroups.forEach(
          m -> {
            if (m.getScenarioName().equals(executionGroup.getScenarioName())) {
              throw new RestelException("EXEC_DEPENDENCY_ERROR", executionGroup.getScenarioName());
            } else {
              if (!CollectionUtils.isEmpty(m.getDependsOn())) {
                isCyclic(executionGroup, m.getDependsOn());
              }
            }
          });
    }
  }

  /**
   * check if dependency is cyclic. Note: all the testMethods with dependencies should be Direct
   * Acyclic Graphs.
   *
   * @param testMethods
   */
  private void validateDefinition(List<RestelTestMethod> testMethods) {
    testMethods.forEach(testMethod -> isCyclic(testMethod, testMethod.getDependentOn()));
  }

  /**
   * checks if there is any cyclic dependencies for testMethod.
   *
   * @param testMethod {@link RestelTestMethod}
   * @param childMethods list of child {@link RestelTestMethod} for testMethod.
   */
  private void isCyclic(RestelTestMethod testMethod, List<RestelTestMethod> childMethods) {
    if (!CollectionUtils.isEmpty(childMethods)) {
      childMethods.forEach(
          m -> {
            if (m.getCaseUniqueName().equals(testMethod.getCaseUniqueName())) {
              throw new RestelException("DEF_DEPENDENCY_ERROR", testMethod.getCaseUniqueName());
            } else {
              if (!CollectionUtils.isEmpty(m.getDependentOn())) {
                isCyclic(testMethod, m.getDependentOn());
              }
            }
          });
    }
  }

  public BaseConfiguration getBaseConfig() {
    return baseConfig;
  }

  /**
   * Gets the method with the given name
   *
   * @param methodName The method name for which the {@link RestelTestMethod} to be searched for.
   * @return {@link RestelTestMethod} with the given name
   */
  public RestelTestMethod getTestDefinitions(String methodName) {
    return indexedTestDefinitions.get(methodName);
  }

  /**
   * Get all the test methods available.
   *
   * @return List of test methods
   */
  public List<RestelTestMethod> getTestDefintions() {
    return Collections.unmodifiableList(testDefinitions);
  }

  /**
   * Gets the list of test execution definitions that will be executed.
   *
   * @return List of {@link RestelTestScenario} to be executed.
   */
  public List<RestelTestScenario> getScenarios() {
    return testScenarios;
  }

  /**
   * Gets the scenario definition with the given name.
   *
   * @param executionName The name of the execution.
   * @return {@link RestelTestScenario} instance with the given name.
   */
  public RestelTestScenario getScenario(String executionName) {
    return indexedTestScenarios.get(executionName);
  }

  /**
   * Gets the suite with the given name
   *
   * @param suiteName The name of the suite.
   * @return The {@link RestelSuite} instance with the given unique name.
   */
  public RestelSuite getTestSuite(String suiteName) {
    return indexedTestSuites.get(suiteName);
  }

  public Map<String, RestelSuite> getTestSuites() {
    return indexedTestSuites;
  }
}
