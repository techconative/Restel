package com.techconative.restel.testng;

import io.qameta.allure.Allure;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * Represents a single test method that will be executed by the TestNG
 */
public class TestCase {

    private String executionName;

    private TestCaseExecutor testExecutor;

    public TestCase(String caseName) {
        this.executionName = caseName;
    }


    public TestCase(String caseName, TestCaseExecutor testExecutor) {
        this(caseName);
        this.testExecutor = testExecutor;
    }


    @Parameters({"name"})
    @Test
    public void executeTest(String name) {
        Allure.step("Start Executing the ".concat(executionName).concat(" - ").concat(testExecutor.getExecutionGroup().getTestDefinitionName()));
        assertTrue(testExecutor.executeTest(),
                executionName + " failed to pass the assertions for " + name);
        Allure.step("Done Executing the ".concat(executionName).concat(" - ").concat(testExecutor.getExecutionGroup().getTestDefinitionName()));
    }
}
