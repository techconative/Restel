package com.techconative.restel.core.model;

/**
 * Defines the context meant to resolve the variables for the test execution.
 *
 * @author kannanr
 */
public class TestContext extends AbstractContext {

  private final String contextName;

  public TestContext(String testName) {
    this(testName, GlobalContext.getInstance());
  }

  public TestContext(String testName, AbstractContext parent) {
    super(parent);
    this.contextName = testName;
    getParentContext().addValue(contextName, this);
  }

  @Override
  protected String getContextName() {
    return contextName;
  }
}
