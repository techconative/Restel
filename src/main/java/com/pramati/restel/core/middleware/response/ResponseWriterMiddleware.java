package com.pramati.restel.core.middleware.response;

import com.pramati.restel.core.http.RESTResponse;
import com.pramati.restel.utils.ObjectMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
public class ResponseWriterMiddleware implements ResponseMiddleware {

    private String filePath;

    public ResponseWriterMiddleware(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public RESTResponse process(RESTResponse response) {
        try {
            FileCopyUtils.copy(ObjectMapperUtils.convertToJsonNode(response).toString().getBytes(), new FileOutputStream(new File(filePath)));
        } catch (IOException ex) {
            log.error("Error in writing the response to file: " + filePath, ex);
        }
        return response;
    }
}
