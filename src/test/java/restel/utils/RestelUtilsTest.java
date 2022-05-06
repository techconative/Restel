package restel.utils;

import com.techconative.restel.core.model.BaseConfiguration;
import com.techconative.restel.core.model.assertion.AssertType;
import com.techconative.restel.core.parser.Parser;
import com.techconative.restel.core.parser.ParserEnums;
import com.techconative.restel.core.parser.config.ParserConfig;
import com.techconative.restel.core.parser.dto.BaseConfig;
import com.techconative.restel.core.parser.dto.TestApiDefinitions;
import com.techconative.restel.core.parser.dto.TestScenarios;
import com.techconative.restel.core.parser.dto.TestSuites;
import com.techconative.restel.exception.RestelException;
import com.techconative.restel.utils.ObjectMapperUtils;
import com.techconative.restel.utils.RestelUtils;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class RestelUtilsTest {

  private Map<String, Object> excelData;

  @ClassRule
  public static final EnvironmentVariables environmentVariables =
      new EnvironmentVariables().set("PORT", "123");

  @Before
  public void before() throws Exception {
    Parser par = new Parser(ParserConfig.load());
    excelData =
        par.parse(
            Files.newInputStream(Paths.get("src/test/resources/Sample_Suite_definition.xlsx")));
  }

  @Test
  public void testCreateBaseConfig() {
    BaseConfig config = new BaseConfig();
    config.setBaseUrl("Url");
    config.setAppName("name");
    config.setDefaultHeader(
        ObjectMapperUtils.convertToJsonNode(Map.of("content-Type", "application/json")).toString());
    BaseConfiguration con = RestelUtils.createBaseConfig(config);
    Assert.assertEquals(config.getBaseUrl(), con.getBaseUrl());
  }

  @Test
  public void testCreateBaseConfigUrlResolvedFromEnv() {
    BaseConfig config = new BaseConfig();
    config.setBaseUrl("http://localhost:${PORT}/box_a39ff2081ad63dba7ef3");
    config.setAppName("name");
    BaseConfiguration con = RestelUtils.createBaseConfig(config);
    Assert.assertEquals(con.getBaseUrl(), "http://localhost:123/box_a39ff2081ad63dba7ef3");
  }

  @Test(expected = RestelException.class)
  public void testCreateBaseConfigWithEmptyUrl() {
    BaseConfig config = new BaseConfig();
    config.setBaseUrl("");
    config.setAppName("name");
    config.setDefaultHeader(
        ObjectMapperUtils.convertToJsonNode(Map.of("content-Type", "application/json")).toString());
    BaseConfiguration con = RestelUtils.createBaseConfig(config);
  }

  @Test
  public void testCreateSuite() {
    List<TestSuites> testSuites =
        (List<TestSuites>) excelData.get(ParserEnums.TEST_SUITES.toString().toLowerCase());
    testSuites.stream()
        .forEach(
            a -> {
              Assert.assertNotNull(RestelUtils.createSuite(a));
            });
  }

  @Test(expected = RestelException.class)
  public void testCreateSuiteWithEmptyName() {
    List<TestSuites> testSuites =
        (List<TestSuites>) excelData.get(ParserEnums.TEST_SUITES.toString().toLowerCase());
    testSuites.get(0).setSuiteUniqueName("");
    RestelUtils.createSuite(testSuites.get(0));
  }

  @Test
  public void testCreateSuiteExecution() {
    List<TestScenarios> testSuiteExecutions =
        (List<TestScenarios>) excelData.get(ParserEnums.TEST_SCENARIOS.toString().toLowerCase());
    testSuiteExecutions.stream()
        .forEach(
            a -> {
              Assert.assertNotNull(RestelUtils.createTestScenario(a));
            });
  }

  @Test(expected = RestelException.class)
  public void testCreateSuiteExecutionWithEmptyName() {
    List<TestScenarios> testSuiteExecutions =
        (List<TestScenarios>) excelData.get(ParserEnums.TEST_SCENARIOS.toString().toLowerCase());
    testSuiteExecutions.get(0).setScenarioUniqueName("");
    RestelUtils.createTestScenario(testSuiteExecutions.get(0));
  }

  @Test(expected = RestelException.class)
  public void testCreateSuiteExecutionWithEmptySuiteName() {
    List<TestScenarios> testSuiteExecutions =
        (List<TestScenarios>) excelData.get(ParserEnums.TEST_SCENARIOS.toString().toLowerCase());
    testSuiteExecutions.get(0).setTestSuite("");
    RestelUtils.createTestScenario(testSuiteExecutions.get(0));
  }

  @Test(expected = RestelException.class)
  public void testCreateSuiteExecutionWithEmptyCaseName() {
    List<TestScenarios> testSuiteExecutions =
        (List<TestScenarios>) excelData.get(ParserEnums.TEST_SCENARIOS.toString().toLowerCase());
    testSuiteExecutions.get(0).setTestApis(List.of(""));
    RestelUtils.createTestScenario(testSuiteExecutions.get(0));
  }

  @Test
  public void testCreateSuiteExecutionWithFunction() {
    List<TestScenarios> testSuiteExecutions =
        (List<TestScenarios>) excelData.get(ParserEnums.TEST_SCENARIOS.toString().toLowerCase());
    testSuiteExecutions
        .get(0)
        .setFunction(
            ObjectMapperUtils.convertToJsonNode(
                    Map.of("val", Map.of("operation", "add", "data", "data", "args", "args")))
                .toString());
    Assert.assertNotNull(RestelUtils.createTestScenario(testSuiteExecutions.get(0)));
  }

  @Test(expected = RestelException.class)
  public void testCreateSuiteExecutionWithFunctionInvalid() {
    List<TestScenarios> testSuiteExecutions =
        (List<TestScenarios>) excelData.get(ParserEnums.TEST_SCENARIOS.toString().toLowerCase());
    testSuiteExecutions
        .get(0)
        .setFunction(
            ObjectMapperUtils.convertToJsonNode(
                    Map.of("val", Map.of("operation", "shift", "data", "data", "args", "args")))
                .toString());
    RestelUtils.createTestScenario(testSuiteExecutions.get(0));
  }

  @Test
  public void testCreateSuiteExecutionWithAssertion() {
    List<TestScenarios> testSuiteExecutions =
        (List<TestScenarios>) excelData.get(ParserEnums.TEST_SCENARIOS.toString().toLowerCase());
    testSuiteExecutions
        .get(0)
        .setAssertion(
            ObjectMapperUtils.convertToJsonNode(
                    Map.of(
                        "assert",
                        Map.of(
                            "message",
                            "message",
                            "condition",
                            Arrays.asList(AssertType.EQUAL.toString(), "name", "name")),
                        "assert2",
                        Map.of(
                            "condition",
                            Arrays.asList(AssertType.EQUAL.toString(), "name", "name"))))
                .toString());
    Assert.assertNotNull(RestelUtils.createTestScenario(testSuiteExecutions.get(0)));
  }

  @Test(expected = RestelException.class)
  public void testCreateSuiteExecutionWithInvalidAssertion() {
    List<TestScenarios> testSuiteExecutions =
        (List<TestScenarios>) excelData.get(ParserEnums.TEST_SCENARIOS.toString().toLowerCase());
    testSuiteExecutions
        .get(0)
        .setAssertion(
            ObjectMapperUtils.convertToJsonNode(
                    Map.of("assert", Map.of("message", "message", "condition", "cond")))
                .toString());
    RestelUtils.createTestScenario(testSuiteExecutions.get(0));
  }

  @Test(expected = RestelException.class)
  public void testCreateSuiteExecutionWithInvalidSyntaxAssertion() {
    List<TestScenarios> testSuiteExecutions =
        (List<TestScenarios>) excelData.get(ParserEnums.TEST_SCENARIOS.toString().toLowerCase());
    testSuiteExecutions.get(0).setAssertion("assert");
    RestelUtils.createTestScenario(testSuiteExecutions.get(0));
  }

  @Test(expected = RestelException.class)
  public void testCreateSuiteExecutionWithInvalidFunction() {
    List<TestScenarios> testSuiteExecutions =
        (List<TestScenarios>) excelData.get(ParserEnums.TEST_SCENARIOS.toString().toLowerCase());
    testSuiteExecutions.get(0).setFunction("func");
    RestelUtils.createTestScenario(testSuiteExecutions.get(0));
  }

  @Test
  public void testCreateDefinition() {
    List<TestApiDefinitions> testsuites =
        (List<TestApiDefinitions>)
            excelData.get(ParserEnums.TEST_API_DEFINITIONS.toString().toLowerCase());
    testsuites.stream()
        .forEach(
            a -> {
              Assert.assertNotNull(
                  RestelUtils.createTestMethod(
                      a,
                      RestelUtils.createBaseConfig(
                          (BaseConfig)
                              excelData.get(ParserEnums.BASE_CONFIG.toString().toLowerCase()))));
            });
  }

  @Test
  public void testCreateDefinitionWithDefaultHeaders() {
    BaseConfig config = new BaseConfig();
    config.setBaseUrl("Url");
    config.setAppName("name");
    config.setDefaultHeader(
        ObjectMapperUtils.convertToJsonNode(Map.of("content-Type", "application/json")).toString());
    BaseConfiguration con = RestelUtils.createBaseConfig(config);

    List<TestApiDefinitions> testDefs =
        (List<TestApiDefinitions>)
            excelData.get(ParserEnums.TEST_API_DEFINITIONS.toString().toLowerCase());
    Assert.assertNotNull(RestelUtils.createTestMethod(testDefs.get(0), con));
  }

  @Test(expected = RestelException.class)
  public void testCreateDefinitionWithNameEmpty() {
    List<TestApiDefinitions> testSuites =
        (List<TestApiDefinitions>)
            excelData.get(ParserEnums.TEST_API_DEFINITIONS.toString().toLowerCase());
    testSuites.get(0).setApiUniqueName("");
    RestelUtils.createTestMethod(
        testSuites.get(0),
        RestelUtils.createBaseConfig(
            (BaseConfig) excelData.get(ParserEnums.BASE_CONFIG.toString().toLowerCase())));
  }

  @Test(expected = RestelException.class)
  public void testCreateDefinitionWithUrl() {
    List<TestApiDefinitions> testSuites =
        (List<TestApiDefinitions>)
            excelData.get(ParserEnums.TEST_API_DEFINITIONS.toString().toLowerCase());
    testSuites.get(0).setRequestUrl("");
    RestelUtils.createTestMethod(
        testSuites.get(0),
        RestelUtils.createBaseConfig(
            (BaseConfig) excelData.get(ParserEnums.BASE_CONFIG.toString().toLowerCase())));
  }

  @Test(expected = RestelException.class)
  public void testCreateDefinitionWithEmptyMethod() {
    List<TestApiDefinitions> testSuites =
        (List<TestApiDefinitions>)
            excelData.get(ParserEnums.TEST_API_DEFINITIONS.toString().toLowerCase());
    testSuites.get(0).setRequestMethod("");
    RestelUtils.createTestMethod(
        testSuites.get(0),
        RestelUtils.createBaseConfig(
            (BaseConfig) excelData.get(ParserEnums.BASE_CONFIG.toString().toLowerCase())));
  }

  @Test(expected = RestelException.class)
  public void testCreateDefinitionWithEmptyResMatcher() {
    List<TestApiDefinitions> testSuites =
        (List<TestApiDefinitions>)
            excelData.get(ParserEnums.TEST_API_DEFINITIONS.toString().toLowerCase());
    testSuites.get(0).setExpectedResponseMatcher("");
    RestelUtils.createTestMethod(
        testSuites.get(0),
        RestelUtils.createBaseConfig(
            (BaseConfig) excelData.get(ParserEnums.BASE_CONFIG.toString().toLowerCase())));
  }

  @Test(expected = RestelException.class)
  public void testCreateDefinitionWithEmptyHeadMatcher() {
    List<TestApiDefinitions> testSuites =
        (List<TestApiDefinitions>)
            excelData.get(ParserEnums.TEST_API_DEFINITIONS.toString().toLowerCase());
    testSuites.get(0).setExpectedHeaderMatcher("");
    RestelUtils.createTestMethod(
        testSuites.get(0),
        RestelUtils.createBaseConfig(
            (BaseConfig) excelData.get(ParserEnums.BASE_CONFIG.toString().toLowerCase())));
  }

  @Test(expected = RestelException.class)
  public void testCreateDefinitionWithEmptyStatus() {
    List<TestApiDefinitions> testSuites =
        (List<TestApiDefinitions>)
            excelData.get(ParserEnums.TEST_API_DEFINITIONS.toString().toLowerCase());
    testSuites.get(0).setAcceptedStatusCodes(Arrays.asList());
    RestelUtils.createTestMethod(
        testSuites.get(0),
        RestelUtils.createBaseConfig(
            (BaseConfig) excelData.get(ParserEnums.BASE_CONFIG.toString().toLowerCase())));
  }
}
