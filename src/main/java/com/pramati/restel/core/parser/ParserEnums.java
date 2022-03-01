package com.pramati.restel.core.parser;

public enum ParserEnums {
    TEST_DEFINITIONS("TestDefinitions"), TEST_SUITES("TestSuites"), TEST_SUITE_EXECUTION("TestSuiteExecution"), BASE_CONFIG("BaseConfig");

    private String value;

    ParserEnums(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
