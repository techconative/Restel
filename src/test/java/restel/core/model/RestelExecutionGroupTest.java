package restel.core.model;

import com.pramati.restel.core.model.RestelExecutionGroup;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class RestelExecutionGroupTest {
    @Test
    public void testRestelExecutionGroup() {
        RestelExecutionGroup exec = new RestelExecutionGroup();
        exec.setDependsOn(null);
        exec.setExecutionGroupName("name");
        exec.setExecutionParams(Map.of("k", "v"));
        exec.setTestDefinitionName("def");
        exec.setTestSuiteName("suite");

        Assert.assertNull(exec.getDependsOn());
        Assert.assertEquals(Map.of("k", "v"), exec.getExecutionParams());
        Assert.assertEquals("name", exec.getExecutionGroupName());
        Assert.assertEquals("def", exec.getTestDefinitionName());
        Assert.assertEquals("suite", exec.getTestSuiteName());

        Assert.assertNotEquals(new RestelExecutionGroup(), exec);
        Assert.assertNotEquals(exec.hashCode(), new RestelExecutionGroup().hashCode());
        Assert.assertNotNull(exec.toString());

    }
}
