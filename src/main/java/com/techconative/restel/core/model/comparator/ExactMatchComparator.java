package com.techconative.restel.core.model.comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.techconative.restel.core.http.RESTResponse;
import com.techconative.restel.utils.ObjectMapperUtils;
import com.techconative.restel.utils.Utils;
import io.qameta.allure.Allure;
import javax.ws.rs.core.MediaType;
import org.springframework.stereotype.Component;
import org.testng.Assert;
import ro.skyah.comparator.CompareMode;
import ro.skyah.comparator.JSONCompare;

/**
 * Comparator that does the exact comparison based on the expected output.
 *
 * @author kannanr
 */
@Component(value = "EXACT_MATCHER")
public class ExactMatchComparator implements ResponseComparator {

  /**
   * @param restResponse The actual output of an API.
   * @param expectedOutput The configured expected output.
   * @return Boolean
   */
  @Override
  public void compareResponse(RESTResponse restResponse, Object expectedOutput) {
    switch (Utils.getMediaType(restResponse)) {
      case MediaType.APPLICATION_JSON:
        evalJson(restResponse.getResponse().getBody(), expectedOutput);
        break;
      case MediaType.TEXT_PLAIN:
        evalText(restResponse, expectedOutput);
        break;
      case MediaType.APPLICATION_XML:
        break;
      default:
        evalJson(restResponse.getResponse().getBody(), expectedOutput);
    }
  }

  @Override
  public void compareHeader(Object headers, Object expectedHeaders) {
    evalJson(headers, expectedHeaders);
  }

  /**
   * * Compares the response body and the expected output. * 1. Equals -> If the the order of the
   * key value pair is altered. * 2. Equals -> If the response is a nexted structure. * 3. Not Equal
   * -> For case sensitivity. * 4. Not Equal -> For the order in the nest list is altered. * Eg :
   * actual::{'names":['Sam','Tom']} , expected :: {'names': ['Tom','Sam']}. * 5. Not Equal -> If
   * Object Type is different . * Ex : actual:: {'amount': 5.0} ,expected ::{'amount':5} . * In
   * here, one is decimal and another is int type. *
   *
   * @param restResponse
   * @param expectedOutput
   */
  private void evalJson(Object restResponse, Object expectedOutput) {
    JsonNode expectedOutputnode = ObjectMapperUtils.convertToJsonNode(expectedOutput);
    JsonNode actualOutputNode = ObjectMapperUtils.convertToJsonNode(restResponse);

    Allure.step(
        "Evaluating with ExactMatcher:: Actual :- "
            + actualOutputNode.toPrettyString()
            + " Expected :- "
            + expectedOutputnode.toPrettyString());
    JSONCompare.assertEquals(
        expectedOutputnode,
        actualOutputNode,
        CompareMode.JSON_OBJECT_NON_EXTENSIBLE,
        CompareMode.JSON_ARRAY_STRICT_ORDER);
  }

  private void evalText(RESTResponse restResponse, Object expectedOutput) {
    Allure.step(
        "Evaluating with ExactMatcher, Actual : "
            + restResponse.getResponse().getBody()
            + " Expected : "
            + expectedOutput);
    Assert.assertEquals(
        restResponse.getResponse().getBody(), expectedOutput, " Missmatch of Expected vs Actual ");
  }
}
