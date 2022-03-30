package com.techconative.restel.core;

import com.techconative.restel.testng.TestFactory;
import io.qameta.allure.testng.AllureTestNg;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.xml.XmlSuite;

/** Responsible for instantiating the TextNG instances and running the test for the given suites. */
@Service
public class SuiteExecutor {

  @Autowired private TestFactory testFactory;

  /** Initiate the test execution */
  public void executeTest(List<XmlSuite> suites) {
    AllureTestNg tla = new AllureTestNg();

    // Create test executor
    TestNG testng = new TestNG();

    // Set suite to the executor
    testng.setXmlSuites(suites);

    // Set object factory which instantiates the test objects
    testng.setObjectFactory(testFactory);

    // Set Allure lister to take care of the reporting
    testng.addListener((ITestNGListener) tla);

    testng.setVerbose(1);
    testng.run();
  }
}
