package restel.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.techconative.restel.exception.RestelException;
import com.techconative.restel.utils.ObjectMapperUtils;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class ObjectMapperUtilsTest {
  @Test
  public void testConvertToMap() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode().put("name", "Tom");
    Assert.assertEquals(
        node.get("name").asText(),
        ObjectMapperUtils.convertToMap(node.toPrettyString()).get("name"));
  }

  @Test(expected = RestelException.class)
  public void testConvertToMapFailure() {
    ObjectMapperUtils.convertToMap("name");
  }

  @Test
  public void testConvertToMappedString() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode().put("name", "Tom");
    Assert.assertEquals(
        node.get("name").asText(),
        ObjectMapperUtils.convertToMappedString(node.toPrettyString()).get("name"));
  }

  @Test(expected = RestelException.class)
  public void testConvertToMappedStringFailure() {
    ObjectMapperUtils.convertToMappedString("name");
  }

  @Test
  public void testConvertToJsonNode() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode().put("name", "Tom");
    Assert.assertEquals(
        node.get("name"), ObjectMapperUtils.convertToJsonNode(node.toPrettyString()).get("name"));
    JsonNode no = mapper.createArrayNode().add("name").add("Tom");
    Assert.assertTrue(ObjectMapperUtils.convertToJsonNode(no.toPrettyString()).isArray());
  }

  @Test(expected = RestelException.class)
  public void testConvertToJsonNodeFailure() {
    ObjectMapperUtils.convertToJsonNode("");
  }

  @Test
  public void testIsJSONValid() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode().put("name", "Tom");
    Assert.assertTrue(ObjectMapperUtils.isJSONValid(node.toPrettyString()));
  }

  @Test
  public void testIsJSONInvalid() {
    Assert.assertFalse(ObjectMapperUtils.isJSONValid("node.toPrettyString"));
  }

  @Test
  public void testConvertToArray() {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.createArrayNode().add("name").add("Tom");
    Assert.assertTrue(ObjectMapperUtils.convertToArray(node.toPrettyString()) instanceof List);
  }

  @Test(expected = RestelException.class)
  public void testConvertToArrayFailure() {
    ObjectMapperUtils.convertToArray("node.toPrettyString");
  }
}
