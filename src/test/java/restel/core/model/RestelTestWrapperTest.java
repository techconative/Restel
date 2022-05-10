package restel.core.model;

import com.techconative.restel.core.model.RestelTestApiDefinition;
import com.techconative.restel.core.model.RestelTestApiWrapper;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class RestelTestWrapperTest {
  @Test
  public void testRestelTestMethod() {
    RestelTestApiWrapper restelTestWrapper = new RestelTestApiWrapper();

    restelTestWrapper.setTestApiDefinition(new RestelTestApiDefinition());
    Assert.assertEquals(new RestelTestApiDefinition(), restelTestWrapper.getTestApiDefinition());

    restelTestWrapper.setTestApiWrapperName("testApiWrapperName");
    Assert.assertEquals("testApiWrapperName", restelTestWrapper.getCaseUniqueName());

    restelTestWrapper.setTestApiWrapperDescription("a read test that returns 500 status code");
    Assert.assertEquals(
        "a read test that returns 500 status code", restelTestWrapper.getCaseDescription());

    restelTestWrapper.setApiParameters(Map.of("param1", "param2"));
    Assert.assertEquals(Map.of("param1", "param2"), restelTestWrapper.getApiParameters());

    Assert.assertNotEquals(new RestelTestWrapperTest(), restelTestWrapper);
    Assert.assertNotEquals(restelTestWrapper.hashCode(), new RestelTestWrapperTest().hashCode());
    Assert.assertNotNull(restelTestWrapper.toString());
  }
}
