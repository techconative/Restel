package com.pramati.restel.testng;

import java.lang.reflect.Constructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.testng.internal.ObjectFactoryImpl;

/**
 * Responsible for dynamically creating the tests from the available tests
 *
 * @author kannanr
 */
@Slf4j
@Component
public class TestFactory extends ObjectFactoryImpl {

  @Autowired BeanFactory beanFactory;

  @Override
  public Object newInstance(Constructor constructor, Object... params) {

    if (params == null || params.length == 0) {
      // When no params present. Let the default objectfactory deal with
      // it
      return super.newInstance(constructor, params);
    }

    String caseName = params[0].toString();

    log.info("Instantiating executor for " + caseName);

    TestCaseExecutor executor = beanFactory.getBean(TestCaseExecutor.class, caseName);

    return new TestCase(caseName, executor);
  }
}
