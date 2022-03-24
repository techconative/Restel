package restel.utils;

import com.pramati.restel.core.model.BaseConfiguration;
import com.pramati.restel.core.model.assertion.AssertType;
import com.pramati.restel.core.parser.Parser;
import com.pramati.restel.core.parser.ParserEnums;
import com.pramati.restel.core.parser.config.ParserConfig;
import com.pramati.restel.core.parser.dto.BaseConfig;
import com.pramati.restel.core.parser.dto.TestDefinitions;
import com.pramati.restel.core.parser.dto.TestScenarios;
import com.pramati.restel.core.parser.dto.TestSuites;
import com.pramati.restel.exception.RestelException;
import com.pramati.restel.utils.ObjectMapperUtils;
import com.pramati.restel.utils.RestelUtils;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RestelUtilsTest {

  private Map<String, Object> excelData;

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
              Assert.assertNotNull(RestelUtils.createExecutionGroup(a));
            });
  }

  @Test(expected = RestelException.class)
  public void testCreateSuiteExecutionWithEmptyName() {
    List<TestScenarios> testSuiteExecutions =
        (List<TestScenarios>) excelData.get(ParserEnums.TEST_SCENARIOS.toString().toLowerCase());
    testSuiteExecutions.get(0).setScenarioUniqueName("");
    RestelUtils.createExecutionGroup(testSuiteExecutions.get(0));
  }

  @Test(expected = RestelException.class)
  public void testCreateSuiteExecutionWithEmptySuiteName() {
    List<TestScenarios> testSuiteExecutions =
        (List<TestScenarios>) excelData.get(ParserEnums.TEST_SCENARIOS.toString().toLowerCase());
    testSuiteExecutions.get(0).setTestSuite("");
    RestelUtils.createExecutionGroup(testSuiteExecutions.get(0));
  }

  @Test(expected = RestelException.class)
  public void testCreateSuiteExecutionWithEmptyCaseName() {
    List<TestScenarios> testSuiteExecutions =
        (List<TestScenarios>) excelData.get(ParserEnums.TEST_SCENARIOS.toString().toLowerCase());
    testSuiteExecutions.get(0).setTestCases(List.of(""));
    RestelUtils.createExecutionGroup(testSuiteExecutions.get(0));
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
    Assert.assertNotNull(RestelUtils.createExecutionGroup(testSuiteExecutions.get(0)));
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
    RestelUtils.createExecutionGroup(testSuiteExecutions.get(0));
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
    Assert.assertNotNull(RestelUtils.createExecutionGroup(testSuiteExecutions.get(0)));
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
    RestelUtils.createExecutionGroup(testSuiteExecutions.get(0));
  }

  @Test(expected = RestelException.class)
  public void testCreateSuiteExecutionWithInvalidSyntaxAssertion() {
    List<TestScenarios> testSuiteExecutions =
        (List<TestScenarios>) excelData.get(ParserEnums.TEST_SCENARIOS.toString().toLowerCase());
    testSuiteExecutions.get(0).setAssertion("assert");
    RestelUtils.createExecutionGroup(testSuiteExecutions.get(0));
  }

  @Test(expected = RestelException.class)
  public void testCreateSuiteExecutionWithInvalidFunction() {
    List<TestScenarios> testSuiteExecutions =
        (List<TestScenarios>) excelData.get(ParserEnums.TEST_SCENARIOS.toString().toLowerCase());
    testSuiteExecutions.get(0).setFunction("func");
    RestelUtils.createExecutionGroup(testSuiteExecutions.get(0));
  }

  @Test
  public void testCreateDefinition() {
    List<TestDefinitions> testsuites =
        (List<TestDefinitions>)
            excelData.get(ParserEnums.TEST_DEFINITIONS.toString().toLowerCase());
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

    List<TestDefinitions> testDefs =
        (List<TestDefinitions>)
            excelData.get(ParserEnums.TEST_DEFINITIONS.toString().toLowerCase());
    Assert.assertNotNull(RestelUtils.createTestMethod(testDefs.get(0), con));
  }

  @Test(expected = RestelException.class)
  public void testCreateDefinitionWithNameEmpty() {
    List<TestDefinitions> testSuites =
        (List<TestDefinitions>)
            excelData.get(ParserEnums.TEST_DEFINITIONS.toString().toLowerCase());
    testSuites.get(0).setCaseUniqueName("");
    RestelUtils.createTestMethod(
        testSuites.get(0),
        RestelUtils.createBaseConfig(
            (BaseConfig) excelData.get(ParserEnums.BASE_CONFIG.toString().toLowerCase())));
  }

  @Test(expected = RestelException.class)
  public void testCreateDefinitionWithUrl() {
    List<TestDefinitions> testSuites =
        (List<TestDefinitions>)
            excelData.get(ParserEnums.TEST_DEFINITIONS.toString().toLowerCase());
    testSuites.get(0).setRequestUrl("");
    RestelUtils.createTestMethod(
        testSuites.get(0),
        RestelUtils.createBaseConfig(
            (BaseConfig) excelData.get(ParserEnums.BASE_CONFIG.toString().toLowerCase())));
  }

  @Test(expected = RestelException.class)
  public void testCreateDefinitionWithEmptyMethod() {
    List<TestDefinitions> testSuites =
        (List<TestDefinitions>)
            excelData.get(ParserEnums.TEST_DEFINITIONS.toString().toLowerCase());
    testSuites.get(0).setRequestMethod("");
    RestelUtils.createTestMethod(
        testSuites.get(0),
        RestelUtils.createBaseConfig(
            (BaseConfig) excelData.get(ParserEnums.BASE_CONFIG.toString().toLowerCase())));
  }

  @Test(expected = RestelException.class)
  public void testCreateDefinitionWithEmptyResMatcher() {
    List<TestDefinitions> testSuites =
        (List<TestDefinitions>)
            excelData.get(ParserEnums.TEST_DEFINITIONS.toString().toLowerCase());
    testSuites.get(0).setExpectedResponseMatcher("");
    RestelUtils.createTestMethod(
        testSuites.get(0),
        RestelUtils.createBaseConfig(
            (BaseConfig) excelData.get(ParserEnums.BASE_CONFIG.toString().toLowerCase())));
  }

  @Test(expected = RestelException.class)
  public void testCreateDefinitionWithEmptyHeadMatcher() {
    List<TestDefinitions> testSuites =
        (List<TestDefinitions>)
            excelData.get(ParserEnums.TEST_DEFINITIONS.toString().toLowerCase());
    testSuites.get(0).setExpectedHeaderMatcher("");
    RestelUtils.createTestMethod(
        testSuites.get(0),
        RestelUtils.createBaseConfig(
            (BaseConfig) excelData.get(ParserEnums.BASE_CONFIG.toString().toLowerCase())));
  }

  @Test(expected = RestelException.class)
  public void testCreateDefinitionWithEmptyStatus() {
    List<TestDefinitions> testSuites =
        (List<TestDefinitions>)
            excelData.get(ParserEnums.TEST_DEFINITIONS.toString().toLowerCase());
    testSuites.get(0).setAcceptedStatusCodes(Arrays.asList());
    RestelUtils.createTestMethod(
        testSuites.get(0),
        RestelUtils.createBaseConfig(
            (BaseConfig) excelData.get(ParserEnums.BASE_CONFIG.toString().toLowerCase())));
  }
}
