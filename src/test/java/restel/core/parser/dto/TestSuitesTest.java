package restel.core.parser.dto;

import com.pramati.restel.core.parser.dto.TestSuites;
import org.junit.Assert;
import org.junit.Test;

public class TestSuitesTest {

    @Test
    public void testTestSuites() {
        TestSuites suites = new TestSuites();

        suites.setDependsOn("depends");
        Assert.assertEquals("depends", suites.getDependsOn());

        suites.setSuiteDescription("des");
        Assert.assertEquals("des", suites.getSuiteDescription());

        suites.setSuiteParams("params");
        Assert.assertEquals("params", suites.getSuiteParams());

        suites.setSuiteUniqueName("name");
        Assert.assertEquals("name", suites.getSuiteUniqueName());

        suites.setSuiteEnable(true);
        Assert.assertTrue(suites.getSuiteEnable());

        Assert.assertNotEquals(new TestSuites(), suites);
        Assert.assertNotEquals(suites.hashCode(), new TestSuites().hashCode());
        Assert.assertNotNull(suites.toString());

    }
}
