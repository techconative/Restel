package com.techconative.restel.core.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techconative.restel.exception.RestelException;
import com.techconative.restel.utils.Constants;
import com.techconative.restel.utils.MessageUtils;
import com.techconative.restel.utils.ObjectMapperUtils;
import com.techconative.restel.utils.Reporter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Responsible for making the ReST call to the server.
 *
 * @author kannanr
 */
@Slf4j
public class RESTClient {

  private final String baseServerUrl;

  private HttpClient client;

  private List<String> noBodyRequests = Arrays.asList("GET", "DELETE", "TRACE", "OPTIONS", "HEAD");

  public RESTClient(String baseUrl) {
    this.baseServerUrl = baseUrl.replaceAll("/+$", ""); // Remove trailing /
    // if any
    log.info("Instantiating rest client with base url" + baseUrl);
    client = HttpClient.newBuilder().build();
  }

  /**
   * Makes the given rest call
   *
   * @param method The HTTP method.
   * @param endpoint The endpoint to be called. This is expected with leading "/" character that
   *     comes after the configured {@link RESTClient#baseServerUrl}
   * @param headers The headers map.
   * @param requestParams the request parameters
   * @param requestBody The request body
   * @return Response from the server after making the http call.
   * @throws RestelException When there is an error making the API call
   */
  public RESTResponse makeCall(
      String method,
      String endpoint,
      Map<String, Object> headers,
      Map<String, Object> requestParams,
      Object requestBody) {
    try {
      HttpRequest httpReq = asHttpRequest(method, endpoint, headers, requestParams, requestBody);

      HttpResponse<String> response = client.send(httpReq, BodyHandlers.ofString());

      Reporter.conveyCall(httpReq, response, requestBody);

      RESTResponse resp = new RESTResponse();
      resp.setHeaders(flattenValues(response.headers()));
      resp.setResponse(ResponseBody.builder().body(response.body()).build());
      resp.setStatus(response.statusCode());
      return resp;

    } catch (JsonProcessingException | URISyntaxException e) {
      log.error(MessageUtils.getString("REQUEST_ERROR"), e);
      throw new RestelException(e, "REQUEST_ERROR");
    } catch (IOException e) {
      log.error(MessageUtils.getString("CALL_ERROR"), e);
      throw new RestelException(e, "CALL_ERROR");
    } catch (InterruptedException e) {
      log.error(MessageUtils.getString("CALL_THREAD_ERROR"), e);
      Thread.currentThread().interrupt();
      throw new RestelException(e, "CALL_THREAD_ERROR");
    }
  }

  /**
   * Convert the {@link HttpHeaders} to Map.
   *
   * @param headers The header instance.
   * @return The equivalent map
   */
  private Map<String, Object> flattenValues(HttpHeaders headers) {

    // Note: This(https://stackoverflow.com/a/50405667/2046462) gives the
    // rationale of the values being maintained as list

    // NOTE: This above mapping might not work for headers that could
    // contain more than one values such as accept. Need experimentation and
    // fix.

    return headers.map().entrySet().stream()
        .collect(
            Collectors.toMap(
                e -> e.getKey().toLowerCase(),
                e ->
                    e == null || CollectionUtils.isEmpty(e.getValue()) // Check if the
                        // value is an
                        // empty list
                        ? null
                        : e.getValue().get(0)));
  }

  private HttpRequest asHttpRequest(
      String method,
      String endpoint,
      Map<String, Object> headers,
      Map<String, Object> requestParams,
      Object requestBody)
      throws URISyntaxException, IOException {
    BodyPublisher bodyPublisher;
    if (StringUtils.containsIgnoreCase(getContentType(headers), MediaType.MULTIPART_FORM_DATA)
        && requestBody instanceof Map) {
      String boundary = new BigInteger(256, new Random()).toString();
      bodyPublisher = ofMimeMultipartData((Map<String, Object>) requestBody, boundary);
      headers.put(
          javax.ws.rs.core.HttpHeaders.CONTENT_TYPE,
          MediaType.MULTIPART_FORM_DATA.concat(";boundary=" + boundary));
    } else {
      bodyPublisher = getBodyPublisher(method, stringifyBody(requestBody, headers));
      if (requestBody instanceof String) {
        bodyPublisher =
            ((String) requestBody).startsWith("@")
                ? BodyPublishers.ofFile(Paths.get((String) requestBody))
                : bodyPublisher;
      }
    }

    Builder interimBuilder =
        HttpRequest.newBuilder()
            .uri(new URI(getUri(endpoint, requestParams)))
            .method(method, bodyPublisher);

    if (!MapUtils.isEmpty(headers)) {
      interimBuilder.headers(asArray(headers));
    }

    return interimBuilder.build();
  }

  /**
   * Transforms the map to an array in which values followed by their respective key
   *
   * @param map The {@link Map} to be transformed to array.
   * @return The array formed out of the map
   */
  private String[] asArray(Map<String, Object> map) {

    if (MapUtils.isEmpty(map)) {
      return new String[0];
    }

    int size = map.size();

    String[] arrVal = new String[2 * size];

    int i = 0;

    for (Entry<String, Object> e : map.entrySet()) {
      arrVal[i++] = e.getKey();
      // Don't expect any complex object to be available here for headers.
      // Hence toString should do
      arrVal[i++] = e.getValue().toString();
    }
    return arrVal;
  }

  private static String stringifyBody(Object requestBody, Map<String, Object> headers)
      throws JsonProcessingException {
    if (Objects.isNull(requestBody)) {
      return "";
    }
    String contentType = getContentType(headers);

    if (StringUtils.containsIgnoreCase(contentType, MediaType.APPLICATION_FORM_URLENCODED)) {
      return asUrlEncodedString((Map<String, Object>) requestBody);
    } else if (StringUtils.containsIgnoreCase(contentType, MediaType.TEXT_PLAIN)) {
      return requestBody.toString();
    }
    // This even stringifies non-json values such as string.
    return ObjectMapperUtils.getMapper().writeValueAsString(requestBody);
  }

  private static String getContentType(Map<String, Object> headers) {
    if (MapUtils.isEmpty(headers)) {
      return null;
    } else {
      if (!Objects.isNull(headers.get(javax.ws.rs.core.HttpHeaders.CONTENT_TYPE))) {
        return headers.get(javax.ws.rs.core.HttpHeaders.CONTENT_TYPE).toString();
      }
    }
    return null;
  }

  private BodyPublisher getBodyPublisher(String method, String requestBody) {
    if (noBodyRequests.contains(method)) {
      return BodyPublishers.noBody();
    }

    return BodyPublishers.ofString(requestBody);
  }

  /**
   * Gets the complete uri including the server base url.
   *
   * @param endpoint The API endpoint for which the complete uri to be formed
   * @param requestParams
   * @return The complete uri for the endpoint incluing the base url
   */
  private String getUri(String endpoint, Map<String, Object> requestParams) {
    return MapUtils.isEmpty(requestParams)
        ? baseServerUrl + endpoint.replaceAll("^/*", "/")
        : baseServerUrl
            + endpoint.replaceAll("^/*", "/").concat("?").concat(asUrlEncodedString(requestParams));
  }

  /**
   * Converts the map as URL encoded string.
   *
   * @param map The {@link Map} to be converted as url encoded string.
   * @return URL encoded string.
   */
  public static String asUrlEncodedString(Map<String, Object> map) {
    return map.entrySet().stream()
        .map(e -> e.getValue() == null ? e.getKey() : e.getKey() + "=" + e.getValue().toString())
        .collect(Collectors.joining("&"));
  }

  /**
   * @param data form Data with file .
   * @param boundary For Multipart/form-data content-type with boundary part.
   * @return {@link BodyPublisher} with byteArray.
   * @throws IOException
   */
  public static BodyPublisher ofMimeMultipartData(Map<String, Object> data, String boundary)
      throws IOException {
    List<byte[]> byteArrays = new ArrayList<>();
    byte[] separator =
        ("--" + boundary + "\r\nContent-Disposition: form-data; name=")
            .getBytes(StandardCharsets.UTF_8);
    for (Map.Entry<String, Object> entry : data.entrySet()) {
      byteArrays.add(separator);

      if (entry.getValue() instanceof String
          && StringUtils.containsIgnoreCase((String) entry.getValue(), Constants.AT_RATE)) {
        Path path =
            Paths.get(
                StringUtils.removeStartIgnoreCase((String) entry.getValue(), Constants.AT_RATE));
        String mimeType = Files.probeContentType(path);
        byteArrays.add(
            ("\""
                    + entry.getKey()
                    + "\"; filename=\""
                    + path.getFileName()
                    + "\"\r\nContent-Type: "
                    + mimeType
                    + "\r\n\r\n")
                .getBytes(StandardCharsets.UTF_8));
        byteArrays.add(Files.readAllBytes(path));
        byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
      } else {
        byteArrays.add(
            ("\"" + entry.getKey() + "\"\r\n\r\n" + entry.getValue() + "\r\n")
                .getBytes(StandardCharsets.UTF_8));
      }
    }
    byteArrays.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
    return BodyPublishers.ofByteArrays(byteArrays);
  }
}
