package com.techconative.restel.core.parser;

public enum ParserEnums {
  TEST_API_DEFINITIONS("TestApiDefinitions"),
  TEST_SUITES("TestSuites"),
  TEST_SCENARIOS("TestScenarios"),
  BASE_CONFIG("BaseConfig");

  private String value;

  ParserEnums(String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }
}
