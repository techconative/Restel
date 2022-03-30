package com.techconative.restel.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techconative.restel.exception.RestelException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ObjectMapperUtils {
  private static ObjectMapper mapper;
  private static final String MSG = "MAP_CONVERT_FAIL";

  static {
    mapper = new ObjectMapper();
  }

  private ObjectMapperUtils() {}

  public static ObjectMapper getMapper() {
    return mapper;
  }

  /**
   * Convert String content to Map
   *
   * @param content
   * @return
   */
  public static Map<String, Object> convertToMap(String content) {
    try {
      return mapper.readValue(content, new TypeReference<>() {});
    } catch (Exception ex) {
      throw new RestelException(ex, MSG);
    }
  }

  public static List<Object> convertToArray(String content) {
    try {
      return mapper.readValue(content, new TypeReference<>() {});
    } catch (Exception ex) {
      throw new RestelException(ex, MSG);
    }
  }

  /**
   * Convert String content to Map.
   *
   * @param content
   * @return
   */
  public static Map<String, String> convertToMappedString(String content) {
    try {
      return mapper.readValue(content, new TypeReference<>() {});
    } catch (Exception ex) {
      throw new RestelException(ex, MSG);
    }
  }

  public static JsonNode convertToJsonNode(Object content) {
    try {
      if (content instanceof String) {
        if (Utils.isArray(content.toString())) {
          content = convertToArray(content.toString());
        } else {
          content = convertToMap(content.toString());
        }
      }
      return mapper.convertValue((content), new TypeReference<>() {});
    } catch (Exception ex) {
      throw new RestelException(ex, "NODE_CONVERT_FAIL");
    }
  }

  public static boolean isJSONValid(String jsonInString) {
    try {
      mapper.readTree(jsonInString);
      return true;
    } catch (IOException e) {
      return false;
    }
  }
}
