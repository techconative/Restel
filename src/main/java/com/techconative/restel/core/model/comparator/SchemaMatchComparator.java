package com.techconative.restel.core.model.comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.techconative.restel.core.http.RESTResponse;
import com.techconative.restel.exception.RestelException;
import com.techconative.restel.utils.ObjectMapperUtils;
import com.techconative.restel.utils.Utils;
import io.qameta.allure.Allure;
import java.util.Set;
import javax.ws.rs.core.MediaType;
import org.junit.Assert;
import org.springframework.stereotype.Component;

@Component(value = "SCHEMA_MATCHER")
public class SchemaMatchComparator implements ResponseComparator {
  @Override
  public void compareResponse(RESTResponse restResponse, Object expectedOutput) {
    String media = Utils.getMediaType(restResponse);
    switch (Utils.getMediaType(restResponse)) {
      case MediaType.APPLICATION_JSON:
        evalJson(restResponse.getResponse().getBody(), expectedOutput);
        break;
      case MediaType.APPLICATION_XML:
        break;
      default:
        throw new RestelException("MEDIA_NOT_APP_JSON", media);
    }
  }

  private void evalJson(Object restResponse, Object expectedOutput) {
    JsonNode expectedOutputnode = ObjectMapperUtils.convertToJsonNode(expectedOutput);
    JsonNode actualOutputNode = ObjectMapperUtils.convertToJsonNode(restResponse);

    JsonSchemaFactory jsonSchemaFactory =
        JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
    JsonSchema schema = jsonSchemaFactory.getSchema(expectedOutputnode);

    Set<ValidationMessage> errors = schema.validate(actualOutputNode);
    Allure.step(
        "Evaluating with SchemaMatcher:: Actual Response :- "
            + actualOutputNode.toPrettyString()
            + " Schema :- "
            + expectedOutputnode.toPrettyString());

    if (!errors.isEmpty()) {
      Assert.fail("Schema validation failed with errors: " + errors.toString());
    } else {
      Assert.assertTrue(Boolean.TRUE);
    }
  }

  @Override
  public void compareHeader(Object headers, Object expectedHeaders) {
    evalJson(headers, expectedHeaders);
  }
}
