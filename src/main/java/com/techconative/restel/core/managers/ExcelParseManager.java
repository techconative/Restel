package com.techconative.restel.core.managers;

import com.techconative.restel.core.model.*;
import com.techconative.restel.core.parser.Parser;
import com.techconative.restel.core.parser.ParserEnums;
import com.techconative.restel.core.parser.config.ParserConfig;
import com.techconative.restel.core.parser.dto.*;
import com.techconative.restel.exception.RestelException;
import com.techconative.restel.utils.RestelUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class ExcelParseManager {

  private String filepath;

  private List<RestelTestApiDefinition> testMethods;
  private Map<String, RestelTestApiDefinition> testMethodMap;
  private List<RestelTestApiWrapper> testApiWrappers;
  private List<RestelSuite> suites;
  private List<RestelTestScenario> execGroups;
  private BaseConfiguration baseConfig;

  public ExcelParseManager(String excelfilePath) {
    this.filepath = excelfilePath;
  }

  @PostConstruct
  private void init() {

    log.info("Parsing the file: {}", this.filepath);
    Map<String, Object> excelData;
    try {
      Parser parser = new Parser(ParserConfig.load());
      FileInputStream inputStream = new FileInputStream(new File(this.filepath));
      excelData = parser.parse(inputStream);

    } catch (FileNotFoundException ex) {
      throw new RestelException(ex, "FILE_NOT_FOUND");
    } catch (Exception ex) {
      throw new RestelException(ex, "PARSER_FAILED");
    }

    List<TestApiDefinitions> testDefs =
        (List<TestApiDefinitions>)
            excelData.get(ParserEnums.TEST_API_DEFINITIONS.toString().toLowerCase());
    List<TestApiWrappers> testWrappers =
        (List<TestApiWrappers>)
            excelData.get(ParserEnums.TEST_API_WRAPPERS.toString().toLowerCase());
    List<TestSuites> testSuites =
        (List<TestSuites>) excelData.get(ParserEnums.TEST_SUITES.toString().toLowerCase());
    List<TestScenarios> testSuiteExecutions =
        (List<TestScenarios>) excelData.get(ParserEnums.TEST_SCENARIOS.toString().toLowerCase());
    baseConfig =
        createBaseConfigure(
            (BaseConfig) excelData.get(ParserEnums.BASE_CONFIG.toString().toLowerCase()));

    testMethods = createTestMethod(testDefs, baseConfig);
    if ((testWrappers != null) && !testWrappers.isEmpty()) {
      testApiWrappers = createTestApiWrapper(testWrappers);
    }
    suites = createSuites(testSuites);
    execGroups = createExecGroups(testSuiteExecutions);
  }

  public List<RestelTestApiDefinition> getTestMethods() {
    return testMethods;
  }

  public List<RestelTestApiWrapper> getTestApiWrappers() {
    return testApiWrappers;
  }

  public List<RestelSuite> getSuites() {
    return suites;
  }

  public List<RestelTestScenario> getExecGroups() {
    return execGroups;
  }

  public BaseConfiguration getBaseConfig() {
    return baseConfig;
  }

  private BaseConfiguration createBaseConfigure(BaseConfig config) {
    return RestelUtils.createBaseConfig(config);
  }

  /**
   * creates List of RestelTestMethod from TestDefinitions
   *
   * @param testApiDefinitions List of {@link TestApiDefinitions}
   * @return list of {@link RestelTestApiDefinition}
   */
  private List<RestelTestApiDefinition> createTestMethod(
      List<TestApiDefinitions> testApiDefinitions, BaseConfiguration baseConfig) {
    if (testApiDefinitions.isEmpty()) {
      throw new RestelException("TEST_DEF_EMPTY");
    }

    // Create a Map od case name and its Method definition.
    testMethodMap =
        testApiDefinitions.stream()
            .collect(
                Collectors.toMap(
                    TestApiDefinitions::getApiUniqueName,
                    x -> RestelUtils.createTestMethod(x, baseConfig)));

    return testApiDefinitions.stream()
        .map(
            testDefinition -> {
              RestelTestApiDefinition testMethod =
                  testMethodMap.get(testDefinition.getApiUniqueName());
              if (!StringUtils.isEmpty(testDefinition.getDependsOn())) {
                List<RestelApiDefinition> dependents =
                    Arrays.asList(testDefinition.getDependsOn().split(",")).stream()
                        .map(
                            name -> {
                              if (StringUtils.isNotEmpty(name)
                                  && Objects.isNull(testMethodMap.get(name))) {
                                throw new RestelException("TEST_DEF_NAME_MISSING", name);
                              }
                              return testMethodMap.get(name);
                            })
                        .collect(Collectors.toList());
                testMethod.setDependentOn(dependents);
                // added parent testDefinitions
                dependents.forEach(dep -> dep.addParentTest(testMethod.getApiUniqueName()));
              }
              return testMethod;
            })
        .collect(Collectors.toList());
  }

  private List<RestelTestApiWrapper> createTestApiWrapper(List<TestApiWrappers> testWrappers) {
    return testWrappers.stream()
        .map(x -> RestelUtils.createTestWrapper(x, testMethodMap))
        .collect(Collectors.toList());
  }

  /**
   * creates List of RestelSuites from TestSuites
   *
   * @param testSuites List of {@link TestSuites}
   * @return List of {@link RestelSuite}
   */
  private List<RestelSuite> createSuites(List<TestSuites> testSuites) {
    if (testSuites.isEmpty()) {
      throw new RestelException("TEST_SUITE_EMPTY");
    }
    // Create a Map of suite name and its Method definition.
    Map<String, RestelSuite> suitesMap = new HashMap<>();
    testSuites.forEach(
        test -> suitesMap.put(test.getSuiteUniqueName(), RestelUtils.createSuite(test)));

    return testSuites.stream()
        .map(
            suite -> {
              RestelSuite restSuite = suitesMap.get(suite.getSuiteUniqueName());
              if (!StringUtils.isEmpty(suite.getDependsOn())) {
                List<RestelSuite> dependents =
                    Arrays.asList(suite.getDependsOn().split(",")).stream()
                        .map(
                            name -> {
                              if (StringUtils.isNotEmpty(name)
                                  && Objects.isNull(suitesMap.get(name))) {
                                throw new RestelException("TEST_SUITE_NAME_MISSING");
                              }
                              return suitesMap.get(name);
                            })
                        .collect(Collectors.toList());
                restSuite.setDependsOn(dependents);
                // add parent suites
                dependents.forEach(dep -> dep.addParentSuite(restSuite.getSuiteName()));
              }
              return restSuite;
            })
        .collect(Collectors.toList());
  }

  /**
   * creates List of RestelExecutionGroup from TestSuiteExecutions
   *
   * @param testSuiteExecutions List of {@link TestScenarios}
   * @return list of {@link RestelTestScenario}
   */
  private List<RestelTestScenario> createExecGroups(List<TestScenarios> testSuiteExecutions) {
    if (testSuiteExecutions.isEmpty()) {
      throw new RestelException("TEST_EXEC_MISSING");
    }
    Map<String, RestelTestScenario> execMap = new HashMap<>();
    testSuiteExecutions.forEach(
        test -> execMap.put(test.getScenarioUniqueName(), RestelUtils.createTestScenario(test)));
    return testSuiteExecutions.stream()
        .map(
            execution -> {
              RestelTestScenario restexec = execMap.get(execution.getScenarioUniqueName());
              if (!StringUtils.isEmpty(execution.getDependsOn())) {
                List<RestelTestScenario> dependents =
                    Arrays.stream(execution.getDependsOn().split(","))
                        .map(
                            name -> {
                              if (StringUtils.isNotEmpty(name)
                                  && Objects.isNull(execMap.get(name))) {
                                throw new RestelException("TEST_EXEC_NAME_MISSING");
                              }
                              return execMap.get(name);
                            })
                        .collect(Collectors.toList());
                restexec.setDependsOn(dependents);
                // add parent exec
                dependents.forEach(dep -> dep.addParentExecution(restexec.getScenarioName()));
              }
              return restexec;
            })
        .collect(Collectors.toList());
  }
}
