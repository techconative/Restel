package com.pramati.restel.core.resolver.assertion;

import com.fasterxml.jackson.databind.JsonNode;
import com.pramati.restel.core.managers.ContextManager;
import com.pramati.restel.core.model.TestContext;
import com.pramati.restel.core.model.assertion.RestelAssertion;
import com.pramati.restel.exception.RestelException;
import com.pramati.restel.utils.ObjectMapperUtils;
import com.pramati.restel.utils.Reporter;
import io.qameta.allure.Allure;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import ro.skyah.comparator.CompareMode;
import ro.skyah.comparator.JSONCompare;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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

        switch (assertion.getAssertType()) {
            case EQUAL:
                assertEqualsOrNot(assertion.getName(), actual, expect, assertion.getMessage(), Boolean.TRUE);
                break;
            case TRUE:
                assertTrueOrFalse(assertion.getName(), actual, assertion.getMessage(), Boolean.TRUE);
                break;
            case FALSE:
                assertTrueOrFalse(assertion.getName(), actual, assertion.getMessage(), Boolean.FALSE);
                break;
            case NOT_EQUAL:
                assertEqualsOrNot(assertion.getName(), actual, expect, assertion.getMessage(), Boolean.FALSE);
                break;
            case GREATER:
                assertGreaterOrLesser(assertion.getName(), actual, expect, assertion.getMessage(), Boolean.TRUE);
                break;
            case LESSER:
                assertGreaterOrLesser(assertion.getName(), actual, expect, assertion.getMessage(), Boolean.FALSE);
                break;
            case NULL:
                assertNullOrNot(assertion.getName(), actual, assertion.getMessage(), Boolean.TRUE);
                break;
            case NOT_NULL:
                assertNullOrNot(assertion.getName(), actual, assertion.getMessage(), Boolean.FALSE);
                break;
        }
    }

    private static void assertNullOrNot(String name, Object actual, String message, Boolean isNull) {
        //ContextManager returns empty string if null .So convert the empty String to null.
        if (actual instanceof String) {
            actual = StringUtils.isEmpty(actual.toString()) ? null : actual;
        }
        Reporter.conveyAssertion(name, actual);
        if (isNull) {
            Assert.assertNull(actual, message);
        } else {
            Assert.assertNotNull(actual, message);
        }
        Reporter.conveyAssertionDone(name);
    }

    private static void assertGreaterOrLesser(String name, Object actual, Object expect, String message, Boolean isGreater) {

    }

    private static void assertTrueOrFalse(String name, Object actual, String message, Boolean isTrue) {
        boolean bool;
        if (actual instanceof Boolean) {
            bool = (boolean) actual;
        } else if (actual instanceof String) {
            if ((StringUtils.equalsIgnoreCase((CharSequence) actual, "true") || StringUtils.equalsIgnoreCase((CharSequence) actual, "false"))) {
                bool = Boolean.valueOf((String) actual);
            } else {
                throw new RestelException("the actual object:" + actual + " is not boolean type for assertion::" + name);
            }
        } else {
            throw new RestelException("the actual object:" + actual + " is not boolean type for assertion::" + name);
        }
        Reporter.conveyAssertion(name, actual);
        if (isTrue) {
            Assert.assertTrue(bool, message);
        } else {
            Assert.assertFalse(bool, message);
        }
        Reporter.conveyAssertionDone(name);
    }

    private static void assertEqualsOrNot(String name, Object actual, Object expect, String message, Boolean equals) {
        if (actual instanceof String && ObjectMapperUtils.isJSONValid(actual.toString())) {
            actual = ObjectMapperUtils.convertToMap((String) actual);
        }
        if (expect instanceof String && ObjectMapperUtils.isJSONValid(expect.toString())) {
            expect = ObjectMapperUtils.convertToMap((String) expect);
        }
        if ((actual instanceof Map || actual instanceof List) && (expect instanceof Map || expect instanceof List)) {
            JsonNode expectedOutputNode = ObjectMapperUtils.convertToJsonNode(expect);
            JsonNode actualOutputNode = ObjectMapperUtils.convertToJsonNode(actual);
            Reporter.conveyAssertion(name, actualOutputNode.toPrettyString(), expectedOutputNode.toPrettyString());
            if (equals) {
                JSONCompare.assertEquals(message, expectedOutputNode, actualOutputNode, null, CompareMode.JSON_OBJECT_NON_EXTENSIBLE, CompareMode.JSON_ARRAY_STRICT_ORDER);
            } else {
                JSONCompare.assertNotEquals(message, expectedOutputNode, actualOutputNode, null, CompareMode.JSON_OBJECT_NON_EXTENSIBLE, CompareMode.JSON_ARRAY_STRICT_ORDER);
            }
        } else {
            Reporter.conveyAssertion(name, actual, expect);
            if (equals) {
                Assert.assertEquals(actual, expect, message);
            } else {
                Assert.assertNotEquals(actual, equals, message);
            }
        }
        Reporter.conveyAssertionDone(name);
    }
}
