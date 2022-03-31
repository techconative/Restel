package com.techconative.restel.core.middleware.response;

import com.techconative.restel.core.http.RESTResponse;
import com.techconative.restel.utils.ObjectMapperUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileCopyUtils;

@Slf4j
public class ResponseWriterMiddleware implements ResponseMiddleware {

  private String filePath;

  public ResponseWriterMiddleware(String filePath) {
    this.filePath = filePath;
  }

  @Override
  public RESTResponse process(RESTResponse response) {
    try {
      FileCopyUtils.copy(
          ObjectMapperUtils.convertToJsonNode(response).toString().getBytes(),
          new FileOutputStream(new File(filePath)));
    } catch (IOException ex) {
      log.error("Error in writing the response to file: " + filePath, ex);
    }
    return response;
  }
}
