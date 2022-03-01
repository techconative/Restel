package restel.core.parser.dto;

import com.pramati.restel.core.parser.dto.TestSuiteExecution;
import org.junit.Assert;
import org.junit.Test;

public class TestSuiteExecutionTest {
    @Test
    public void testTestSuoteexecution() {
        TestSuiteExecution suiteExecution = new TestSuiteExecution();

        suiteExecution.setDependsOn("depend");
        Assert.assertEquals("depend", suiteExecution.getDependsOn());

        suiteExecution.setTestCase("test");
        Assert.assertEquals("test", suiteExecution.getTestCase());

        suiteExecution.setTestExecutionEnable(false);
        Assert.assertFalse(suiteExecution.getTestExecutionEnable());

        suiteExecution.setTestExecutionEnable(Boolean.TRUE);
        Assert.assertTrue(suiteExecution.getTestExecutionEnable());

        suiteExecution.setTestExecutionParams("param");
        Assert.assertEquals("param", suiteExecution.getTestExecutionParams());

        suiteExecution.setTestSuite("suite");
        Assert.assertEquals("suite", suiteExecution.getTestSuite());

        suiteExecution.setTestExecutionUniqueName("name");
        Assert.assertEquals("name", suiteExecution.getTestExecutionUniqueName());

        Assert.assertNotEquals(new TestSuiteExecution(), suiteExecution);
        Assert.assertNotEquals(suiteExecution.hashCode(), new TestSuiteExecution().hashCode());
        Assert.assertNotNull(suiteExecution.toString());
    }
}
