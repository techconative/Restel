package com.techconative.restel.core.parser.dto;

import java.util.List;
import lombok.Data;

/** TestSuites type dto for sheet test_suites */
@Data
public class TestSuites {

  private String suiteUniqueName;

  private String suiteDescription;

  private List<String> suiteScenariosList;

  private String dependsOn;

  private String suiteParams;

  private Boolean suiteEnable;
}
