package restel.core.middleware;


import com.techconative.restel.core.http.RESTResponse;
import com.techconative.restel.core.http.ResponseBody;
import com.techconative.restel.core.middleware.response.ResponseWriterMiddleware;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

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
