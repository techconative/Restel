package restel.core.parser.dto;

import com.techconative.restel.core.parser.dto.TestApiWrappers;
import org.junit.Assert;
import org.junit.Test;

public class TestWrappersTest {

  @Test
  public void testTestDefinitions() {
    TestApiWrappers testApiWrappers = new TestApiWrappers();

    testApiWrappers.setTestApiWrapperName("testApiWrapperName");
    Assert.assertEquals("testApiWrapperName", testApiWrappers.getTestApiWrapperName());

    testApiWrappers.setTestApiWrapperDescription("testApiWrapperDescription");
    Assert.assertEquals(
        "testApiWrapperDescription", testApiWrappers.getTestApiWrapperDescription());

    testApiWrappers.setTestApiWrapperParameters(
        "{\"read_expected_response\":{\"message\":\"Cannot read properties of null (reading '_id')\"},\"expected_read_status_code\": \"500\"}\n");
    Assert.assertEquals(
        "{\"read_expected_response\":{\"message\":\"Cannot read properties of null (reading '_id')\"},\"expected_read_status_code\": \"500\"}\n",
        testApiWrappers.getTestApiWrapperParameters());

    testApiWrappers.setTestApiName("read_entry");
    Assert.assertEquals("read_entry", testApiWrappers.getTestApiName());

    Assert.assertNotEquals(testApiWrappers, new TestApiWrappers());
    Assert.assertNotEquals(testApiWrappers.hashCode(), new TestApiWrappers().hashCode());
    Assert.assertNotNull(testApiWrappers.toString());
  }
}
