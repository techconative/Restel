package restel.core.model;

import com.techconative.restel.core.model.GlobalContext;
import com.techconative.restel.core.model.TestContext;
import org.junit.Assert;
import org.junit.Test;

public class TestContextTest {
  @Test
  public void testTestContext() {
    TestContext context = new TestContext("test-A");
    context.addValue("test-A", "Success");
    GlobalContext.getInstance().addValue("Global", "Global-Value");

    Assert.assertEquals("Global-Value", context.resolveValue("Global"));
    Assert.assertEquals("Success", context.resolveValue("test-A"));
    Assert.assertNull(context.resolveValue("Test"));
  }
}
