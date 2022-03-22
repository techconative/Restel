package com.pramati.restel.core.parser.dto;

import lombok.Data;

/** TestSuites type dto for sheet test_suites */
@Data
public class TestSuites {

  private String suiteUniqueName;

  private String suiteDescription;

  private String dependsOn;

  private String suiteParams;

  private Boolean suiteEnable;
}
