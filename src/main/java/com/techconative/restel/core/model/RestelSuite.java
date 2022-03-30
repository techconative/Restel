package com.techconative.restel.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;

/** Represents the test suite. */
@Data
public class RestelSuite {
  private String suiteName;
  private String suiteDescription;
  private List<RestelSuite> dependsOn;
  private Map<String, Object> suiteParams;
  private boolean suiteEnable;
  private List<String> parentSuites = new ArrayList<>();

  public void addParentSuite(String parentSuite) {
    parentSuites.add(parentSuite);
  }
}
