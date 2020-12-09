package restel.utils;

import com.pramati.restel.core.parser.Parser;
import com.pramati.restel.core.parser.ParserEnums;
import com.pramati.restel.core.parser.config.ParserConfig;
import com.pramati.restel.core.parser.dto.BaseConfig;
import com.pramati.restel.core.parser.dto.TestDefinitions;
import com.pramati.restel.core.parser.dto.TestSuiteExecution;
import com.pramati.restel.core.parser.dto.TestSuites;
import com.pramati.restel.utils.RestelUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class RestelUtilsTest {

    private Map<String, Object> excelData;

    @Before
    public void before() throws Exception {
        Parser par = new Parser(ParserConfig.load());
        excelData = par.parse(Files.newInputStream(Paths.get("src/test/resources/Sample_Suite_definition.xlsx")));

    }

    @Test
    public void createSuiteTest() {
        List<TestSuites> testSuites = (List<TestSuites>) excelData.get(ParserEnums.TEST_SUITES.toString().toLowerCase());
        testSuites.stream().forEach(a -> {
            Assert.assertNotNull(RestelUtils.createSuite(a));
        });
    }

    @Test
    public void createSuiteExecutionTest() {
        List<TestSuiteExecution> testSuiteExecutions = (List<TestSuiteExecution>) excelData.get(ParserEnums.TEST_SUITE_EXECUTION.toString().toLowerCase());
        testSuiteExecutions.stream().forEach(a -> {
            Assert.assertNotNull(RestelUtils.createExecutionGroup(a));
        });
    }

    @Test
    public void createDefinitionTest() {
        List<TestDefinitions> testsuites = (List<TestDefinitions>) excelData.get(ParserEnums.TEST_DEFINITIONS.toString().toLowerCase());
        testsuites.stream().forEach(a -> {
            Assert.assertNotNull(RestelUtils.createTestMethod(a, RestelUtils.createBaseConfig((BaseConfig) excelData.get(ParserEnums.BASE_CONFIG.toString().toLowerCase()))));
        });
    }
}
