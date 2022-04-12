package com.techconative.restel.core.parser.dto;

import java.util.List;
import lombok.Data;

/** TestScenarios type dto for sheet TEST_SCENARIOS */
@Data
public class TestScenarios {

  private String scenarioUniqueName;

  private String scenarioDescription;

  private String testSuite;

  private List<String> testApis;

  private String dependsOn;

  private String scenarioParams;

  private Boolean scenarioEnabled;

  private String assertion;

  private String function;
}
