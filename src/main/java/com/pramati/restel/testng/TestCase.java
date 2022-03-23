package com.pramati.restel.testng;

import static org.testng.Assert.assertTrue;

import io.qameta.allure.Allure;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/** Represents a single test method that will be executed by the TestNG.
 * Equivalent to a test scenario in restel. */
public class TestCase {

  private String scenarioName;

  private TestCaseExecutor testExecutor;

  public TestCase(String caseName) {
    this.scenarioName = caseName;
  }

  public TestCase(String caseName, TestCaseExecutor testExecutor) {
    this(caseName);
    this.testExecutor = testExecutor;
  }

  @Parameters({"name"})
  @Test
  public void executeTest(String name) {
    Allure.step(
        "Start Executing the "
            .concat(scenarioName)
            .concat(" - ")
                // TODO: test run
            .concat(testExecutor.getExecutionGroup().getTestDefinitionName().get(0)));
    assertTrue(
        testExecutor.executeTest(), scenarioName + " failed to pass the assertions for " + name);
    Allure.step(
        "Done Executing the "
            .concat(scenarioName)
            .concat(" - ")
                // TODO: test run
            .concat(testExecutor.getExecutionGroup().getTestDefinitionName().get(0)));
  }
}
