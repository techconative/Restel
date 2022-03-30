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
    AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);

    RestelRunner bean = ctx.getBean(RestelRunner.class);
    bean.run();

    ctx.close();
    ctx.stop();
  }
}
