package com.pramati.restel.core.resolver;

import com.fasterxml.jackson.databind.JsonNode;
import com.pramati.restel.core.managers.ContextManager;
import com.pramati.restel.core.model.TestContext;
import com.pramati.restel.core.model.assertion.RestelAssertion;
import com.pramati.restel.utils.ObjectMapperUtils;
import io.qameta.allure.Allure;
import org.testng.Assert;
import ro.skyah.comparator.CompareMode;
import ro.skyah.comparator.JSONCompare;

import java.util.List;
import java.util.Map;

public class RestelAssertionResolver {

    private RestelAssertionResolver() {
    }

    /**
     * evaluate the Assertion statements with Context, checks if the expected and actual values are equal from {@link RestelAssertion}.
     *
     * @param context   {@link TestContext}.
     * @param assertion {@link RestelAssertion}
     */
    public static void resolve(TestContext context, RestelAssertion assertion) {
        ContextManager manager = new ContextManager();
        Object actual = manager.replaceContextVariables(context, assertion.getActual());
        Object expect = manager.replaceContextVariables(context, assertion.getExpected());

        if (actual instanceof String && ObjectMapperUtils.isJSONValid(actual.toString())) {
            actual = ObjectMapperUtils.convertToMap(actual.toString());
        }

        if (expect instanceof String && ObjectMapperUtils.isJSONValid(expect.toString())) {
            expect = ObjectMapperUtils.convertToMap(expect.toString());
        }

        if ((actual instanceof Map || actual instanceof List) && (expect instanceof Map || expect instanceof List)) {
            JsonNode expectedOutputNode = ObjectMapperUtils.convertToJsonNode(expect);
            JsonNode actualOutputNode = ObjectMapperUtils.convertToJsonNode(actual);

            Allure.step("Evaluating with Assertion::" + assertion.getName() + "  with  Actual :- " + actualOutputNode.toPrettyString() + " Expected :- " + expectedOutputNode.toPrettyString());
            JSONCompare.assertEquals(expectedOutputNode, actualOutputNode, CompareMode.JSON_OBJECT_NON_EXTENSIBLE, CompareMode.JSON_ARRAY_STRICT_ORDER);
        } else {
            Allure.step("Evaluating with Assertion::" + assertion.getName() + "  with  Actual :- " + actual + " Expected :- " + expect);
            Assert.assertEquals(actual, expect, " Missmatch of Expected vs Actual for assertion: " + assertion.getName());
        }
    }
}
