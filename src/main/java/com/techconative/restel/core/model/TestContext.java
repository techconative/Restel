package com.techconative.restel.core.model;

/**
 * Defines the context meant to resolve the variables for the test execution.
 *
 * @author kannanr
 */
public class TestContext extends AbstractContext {

  private String testName;

  public TestContext(String testName) {
    super(GlobalContext.getInstance());
    this.testName = testName;
  }

  public String getTestName() {
    return this.testName;
  }
}
