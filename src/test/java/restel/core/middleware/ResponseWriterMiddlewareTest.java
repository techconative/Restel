package restel.core.middleware;

import com.pramati.restel.core.http.RESTResponse;
import com.pramati.restel.core.http.ResponseBody;
import com.pramati.restel.core.middleware.response.ResponseWriterMiddleware;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

public class ResponseWriterMiddlewareTest {
  String dir = "src/test/resources/files/";

  @Before
  public void before() throws IOException {
    new File(dir).mkdir();
  }

  @Test
  public void testProcess() {
    ResponseWriterMiddleware middleware = new ResponseWriterMiddleware(dir.concat("out.txt"));
    RESTResponse response = new RESTResponse();
    response.setHeaders(Map.of("key", "value"));
    response.setResponse(ResponseBody.builder().body("Response content").build());
    response.setStatus(200);
    Assert.assertNotNull(middleware.process(response));
  }

  @Test
  public void testProcessFailure() {
    ResponseWriterMiddleware middleware = new ResponseWriterMiddleware(dir.concat("tree/out2.txt"));
    Assert.assertNull(middleware.process(null));
  }

  @After
  public void delete() {
    FileSystemUtils.deleteRecursively(new File(dir));
  }
}
