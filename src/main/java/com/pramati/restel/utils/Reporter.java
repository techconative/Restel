package com.pramati.restel.utils;

import com.pramati.restel.core.http.RESTRequest;
import com.pramati.restel.core.http.RESTResponse;
import io.qameta.allure.Allure;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Reporter {
  private Reporter() {}

  public static void attachRequest(RESTRequest restRequest) {
    Allure.addAttachment(
        "Request-Endpoint: ".concat(restRequest.getEndpoint()), restRequest.toString());
  }

  public static void attachResponse(String endpoint, RESTResponse response) {
    Allure.addAttachment("Response-Endpoint: ".concat(endpoint), response.toString());
  }

  public static void conveyCall(
      HttpRequest httpReq, HttpResponse<String> response, Object requestBody) {
    try {
      Allure.step(
          "Request call for API: "
              + httpReq.uri()
              + " for method: "
              + httpReq.method()
              + " has status code: "
              + response.statusCode());
      Allure.step("Request Body: " + ObjectMapperUtils.getMapper().writeValueAsString(requestBody));
    } catch (Exception ex) {
      log.warn("Error in writing to Allure reports");
    }
  }

  public static void conveyAssertion(String name, Object actual) {
    Allure.step("Evaluating with Assertion::" + name + "  with  Actual :- " + actual);
  }

  public static void conveyAssertion(String name, Object actual, Object expect) {
    Allure.step(
        "Evaluating with Assertion::"
            + name
            + "  with  Actual :- "
            + actual
            + " Expected :- "
            + expect);
  }

  public static void conveyAssertionDone(String name) {
    Allure.step(" Done evaluating with Assertion::" + name);
  }
}
