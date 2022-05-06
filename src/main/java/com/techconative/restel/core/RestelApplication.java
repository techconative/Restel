package com.techconative.restel.core;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * Main class, entry point for the test execution application.
 *
 * @author kannanr
 */
public class RestelApplication {

  public static void main(String[] args) {
    RestelApplication app = new RestelApplication();
    app.executeTests();
  }

  public boolean executeTests() {
    AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);

    RestelRunner bean = ctx.getBean(RestelRunner.class);
    boolean isSuccess = bean.run();

    ctx.close();
    ctx.stop();

    return isSuccess;
  }
}
