package com.techconative.restel.core;

import com.techconative.restel.core.managers.RestelTestManager;
import com.techconative.restel.core.model.RestelSuite;
import com.techconative.restel.core.model.RestelTestApiDefinition;
import com.techconative.restel.core.model.RestelTestScenario;
import com.techconative.restel.testng.TestCase;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * Responsible for programmatically initiate the TestNG execution.
 *
 * @author kannanr
 */
@Slf4j
@Component
public class RestelRunner {

  @Autowired private RestelTestManager testManager;

  @Autowired private SuiteExecutor suiteExecutor;

  /** Runs the tests supplied by the test manager. */
  public void run() {
    suiteExecutor.executeTest(
        testManager.getTestSuites().values().stream()
            .filter(
                suite -> CollectionUtils.isEmpty(suite.getParentSuites()) && suite.isSuiteEnable())
            .map(this::createSuite)
            .collect(Collectors.toList()));
  }

  private XmlSuite createSuite(RestelSuite suite) {
    XmlSuite parentSuite = getSuite(suite.getSuiteName());
    // Add dependent suites
    if (!CollectionUtils.isEmpty(suite.getDependsOn())) {
      suite
          .getDependsOn()
          .forEach(
              dependentSuite -> {
                if (dependentSuite.isSuiteEnable()) {
                  XmlSuite childSuite = createSuite(dependentSuite);
                  childSuite.setParentSuite(parentSuite);
                  parentSuite.getChildSuites().add(childSuite);
                }
              });
    }

    parentSuite.setParallel(XmlSuite.ParallelMode.INSTANCES);
    parentSuite.setThreadCount(2);

    // Since we are creating a test for each method this helps
    Set<XmlTest> tests = new HashSet<>();
    parentSuite.setTests(
        // Added executions of enabledTest with no parent executions and of the current TestSuite
        new ArrayList<>(
            getTestList(
                parentSuite,
                testManager.getScenarios().stream()
                    .filter(RestelTestScenario::isScenarioEnabled)
                    .filter(e -> e.getTestSuiteName().equals(suite.getSuiteName()))
                    .filter(exec -> CollectionUtils.isEmpty(exec.getParentExecutions()))
                    .collect(Collectors.toList()),
                tests)));

    log.info("XML equivalent for " + parentSuite.toXml());
    return parentSuite;
  }

  /**
   * Gets the suite with the given name
   *
   * @param suiteName The name of the suite.
   * @return The {@link XmlSuite} instance
   */
  private static XmlSuite getSuite(String suiteName) {
    XmlSuite xSuite = new XmlSuite();
    // Create suite and set tests
    xSuite.setName(suiteName);
    return xSuite;
  }

  /**
   * Translate the {@link RestelTestApiDefinition} into {@link XmlTest} instances.
   *
   * @param parentSuite The suite instance to which the created {@link XmlTest} instances will fall
   *     under.
   * @param restelExec The {@link RestelTestApiDefinition}s that will be translated.
   * @return List of {@link XmlTest} that has been translated.
   */
  private static Set<XmlTest> getTestList(
      XmlSuite parentSuite, List<RestelTestScenario> restelExec, Set<XmlTest> tests) {
    for (RestelTestScenario exec : restelExec) {
      if (exec.isScenarioEnabled()) {
        XmlTest test = getTest(parentSuite, exec);
        if (!CollectionUtils.isEmpty(exec.getDependsOn())) {
          Set<XmlTest> dependentTest = new HashSet<>();
          exec.getDependsOn()
              .forEach(
                  dep ->
                      dependentTest.addAll(getTestList(parentSuite, exec.getDependsOn(), tests)));
          test.setDependentXmlTest(new ArrayList<>(dependentTest));
        }
        tests.add(test);
      }
    }
    return tests;
  }

  private static XmlTest getTest(XmlSuite parentSuite, RestelTestScenario exec) {
    XmlTest xTest = new XmlTest(parentSuite);
    xTest.setName(exec.getScenarioName());
    xTest.setParameters(Map.of("name", exec.getScenarioName()));

    XmlClass xClass1 = getClass(xTest, exec.getScenarioName());

    xTest.setXmlClasses(Collections.singletonList(xClass1));
    return xTest;
  }

  /**
   * A new {@link XmlClass} instance for the given test. For the standard Test class that we have.
   *
   * @param xTest The {@link XmlTest} instance to which the class belongs to.
   * @param name The name to be set for the to-be-created instance.
   * @return The {@link XmlClass} instance for the given params.
   */
  private static XmlClass getClass(XmlTest xTest, String name) {
    // Create Class and set to test
    XmlClass xClass = new XmlClass(TestCase.class);
    xClass.setClass(TestCase.class);
    xClass.setXmlTest(xTest);
    xClass.setName(name);
    return xClass;
  }
}
