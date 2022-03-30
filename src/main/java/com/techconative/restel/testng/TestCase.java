package com.techconative.restel.testng;

import static com.techconative.restel.utils.Utils.toCsv;
import static org.testng.Assert.assertTrue;

import io.qameta.allure.Allure;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Represents a single test method that will be executed by the TestNG. Equivalent to a test
 * scenario in restel.
 */
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
            .concat(toCsv(testExecutor.getExecutionGroup().getTestDefinitionNames())));
    assertTrue(
        testExecutor.executeTest(), scenarioName + " failed to pass the assertions for " + name);
    Allure.step(
        "Done Executing the "
            .concat(scenarioName)
            .concat(" - ")
            .concat(toCsv(testExecutor.getExecutionGroup().getTestDefinitionNames())));
  }
}
