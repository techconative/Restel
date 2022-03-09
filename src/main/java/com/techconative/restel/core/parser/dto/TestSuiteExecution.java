package com.techconative.restel.core.parser.dto;

import lombok.Data;

/**
 * TestSuiteExecution type dto for sheet test_suite_execution
 */
@Data
public class TestSuiteExecution {

    private String testExecutionUniqueName;

    private String testSuite;

    private String testCase;

    private String dependsOn;

    private String testExecutionParams;

    private Boolean testExecutionEnable;

    private String assertion;

    private String function;

}
