package com.techconative.restel.core.model;

import com.techconative.restel.core.model.assertion.RestelAssertion;
import com.techconative.restel.core.model.functions.RestelFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * Represent the execution scenario, which will actually be executed, by pulling in the referenced
 * test definition.
 *
 * @author kannanr
 */
@Data
public class RestelTestScenario {
  private String scenarioName;
  private String scenarioDescription;
  private List<String> testApis;
  private String testSuiteName;
  private List<RestelTestScenario> dependsOn;
  private Map<String, Object> executionParams;
  private boolean scenarioEnabled;
  private List<String> parentExecutions = new ArrayList<>();
  private List<RestelAssertion> assertions;
  private Map<String, RestelFunction> functions;

  public void addParentExecution(String parentExec) {
    parentExecutions.add(parentExec);
  }
}
