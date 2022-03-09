package restel.core.resolver.assertion;

import com.techconative.restel.core.model.TestContext;
import com.techconative.restel.core.model.assertion.AssertType;
import com.techconative.restel.core.model.assertion.RestelAssertion;
import com.techconative.restel.core.resolver.assertion.RestelAssertionResolver;
import com.techconative.restel.exception.RestelException;
import com.techconative.restel.utils.ObjectMapperUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RestelAssertionResolverTest {

    @Test
    public void testResolveAssertEqualForString() {
        RestelAssertion assertion = createAssertion("Sample", AssertType.EQUAL, "${Name}", "Tom", "Success");
        TestContext context = new TestContext("Sample");
        context.addValue("Name", "Tom");

        RestelAssertionResolver.resolve(context, assertion);
        Assert.assertEquals("Sample", assertion.getName());
    }

    @Test
    public void testResolveAssertNotEqualForString() {
        RestelAssertion assertion = createAssertion("Sample", AssertType.NOT_EQUAL, "${Name}", "Ram", "Success");
        TestContext context = new TestContext("Sample");
        context.addValue("Name", "Tom");

        RestelAssertionResolver.resolve(context, assertion);
        Assert.assertEquals("Sample", assertion.getName());
    }

    @Test
    public void testResolveAssertEqualForJson() {
        Map<String, Object> data = new HashMap<>();
        data.put("animals", Arrays.asList("Cat", "Dog", "Rat"));
        RestelAssertion assertion = createAssertion("Sample", AssertType.EQUAL, "${Name}", ObjectMapperUtils.convertToJsonNode(data).toPrettyString(), "Success");
        TestContext context = new TestContext("Sample");
        context.addValue("Name", data);

        RestelAssertionResolver.resolve(context, assertion);
        Assert.assertEquals("Sample", assertion.getName());
    }


    @Test
    public void testResolveAssertNotEqualForJson() {
        Map<String, Object> data = new HashMap<>();
        data.put("animals", Arrays.asList("Cat", "Dog", "Rat"));
        RestelAssertion assertion = createAssertion("Sample", AssertType.NOT_EQUAL, "${Name}", ObjectMapperUtils.convertToJsonNode(Arrays.asList(data)).toPrettyString(), "Success");
        TestContext context = new TestContext("Sample");
        context.addValue("Name", data);

        RestelAssertionResolver.resolve(context, assertion);
        Assert.assertEquals("Sample", assertion.getName());
    }

    @Test
    public void testResolveAssertEqualForJsonInExpected() {
        Map<String, Object> data = new HashMap<>();
        data.put("animals", Arrays.asList("Cat", "Dog", "Rat"));
        RestelAssertion assertion = createAssertion("Sample", AssertType.EQUAL, ObjectMapperUtils.convertToJsonNode(data).toPrettyString(), "${Name}", null);
        TestContext context = new TestContext("Sample");
        context.addValue("Name", data);

        RestelAssertionResolver.resolve(context, assertion);
        Assert.assertEquals("Sample", assertion.getName());
    }


    @Test(expected = AssertionError.class)
    public void testResolveAssertEqualForJsonFailure() {
        Map<String, Object> data = new HashMap<>();
        data.put("animals", Arrays.asList("Cat", "Dog", "Rat"));
        TestContext context = new TestContext("Sample");
        context.addValue("Name", data);
        RestelAssertionResolver.resolve(context, createAssertion("Sample", AssertType.EQUAL, "${Name}", data.get("animals").toString(), null));
    }

    @Test(expected = AssertionError.class)
    public void testResolveAssertEqualForStringFailure() {
        TestContext context = new TestContext("Sample");
        context.addValue("Name", "Krish");
        RestelAssertionResolver.resolve(context, createAssertion("Sample", AssertType.EQUAL, "${Name}", "Ram", null));
    }

    @Test
    public void testResolveAssertTrue() {
        TestContext context = new TestContext("Sample");
        context.addValue("data", Map.of("name", "Tom", "Admin", "True"));
        RestelAssertion assertion = createAssertion("Sample", AssertType.TRUE, null, "${data.Admin}", null);
        RestelAssertionResolver.resolve(context, assertion);
        Assert.assertEquals("Sample", assertion.getName());
    }

    @Test
    public void testResolveAssertNull() {
        TestContext context = new TestContext("Sample");
        context.addValue("data", Map.of("name", "Tom", "Admin", "True"));
        RestelAssertion assertion = createAssertion("Sample", AssertType.NULL, null, "${data.Admin.date}", null);
        RestelAssertionResolver.resolve(context, assertion);
        Assert.assertEquals("Sample", assertion.getName());

    }

    @Test
    public void testResolveAssertNotNull() {
        TestContext context = new TestContext("Sample");
        context.addValue("data", Map.of("name", "Tom", "Admin", "True"));
        RestelAssertion assertion = createAssertion("Sample", AssertType.NOT_NULL, null, "${data.Admin}", null);
        RestelAssertionResolver.resolve(context, assertion);
        Assert.assertEquals("Sample", assertion.getName());
    }

    @Test
    public void testResolveAssertFalse() {
        TestContext context = new TestContext("Sample");
        Map<String, Object> data = new HashMap<>();
        data.put("Admin", false);
        context.addValue("data", data);
        RestelAssertion assertion = createAssertion("Sample", AssertType.FALSE, null, "${data.Admin}", null);
        RestelAssertionResolver.resolve(context, assertion);
        Assert.assertEquals("Sample", assertion.getName());

    }

    @Test(expected = RestelException.class)
    public void testResolveAssertFalseInvalid() {
        TestContext context = new TestContext("Sample");
        context.addValue("data", Map.of("name", "Tom", "Admin", "False"));
        RestelAssertion assertion = createAssertion("Sample", AssertType.FALSE, null, "${data.name}", null);
        RestelAssertionResolver.resolve(context, assertion);
    }

    @Test(expected = RestelException.class)
    public void testResolveAssertFalseJsonInvalid() {
        TestContext context = new TestContext("Sample");
        context.addValue("data", Map.of("name", "Tom", "Admin", "False"));
        RestelAssertion assertion = createAssertion("Sample", AssertType.FALSE, null, "${data}", null);
        RestelAssertionResolver.resolve(context, assertion);
    }

    @Test
    public void testResolveAssertGreater() {
        TestContext context = new TestContext("Sample");
        context.addValue("data", Map.of("name", "Tom", "PhoneNumber", "9999999999"));
        RestelAssertion assertion = createAssertion("Sample", AssertType.GREATER, "99999990", "${data.PhoneNumber}", null);
        RestelAssertionResolver.resolve(context, assertion);
        Assert.assertEquals("Sample", assertion.getName());

    }

    @Test(expected = AssertionError.class)
    public void testResolveAssertGreaterFailure() {
        TestContext context = new TestContext("Sample");
        context.addValue("data", Map.of("name", "Tom", "PhoneNumber", "9999999999"));
        RestelAssertion assertion = createAssertion("Sample", AssertType.GREATER, "99999999990", "${data.PhoneNumber}", null);
        RestelAssertionResolver.resolve(context, assertion);
    }

    @Test
    public void testResolveAssertLesser() {
        TestContext context = new TestContext("Sample");
        context.addValue("data", Map.of("name", "Tom", "PhoneNumber", "9999999999"));
        RestelAssertion assertion = createAssertion("Sample", AssertType.LESSER, "${data.PhoneNumber}", "99999990", null);
        RestelAssertionResolver.resolve(context, assertion);
        Assert.assertEquals("Sample", assertion.getName());
    }

    @Test(expected = AssertionError.class)
    public void testResolveAssertLesserFailure() {
        TestContext context = new TestContext("Sample");
        context.addValue("data", Map.of("name", "Tom", "PhoneNumber", "9999999999"));
        RestelAssertion assertion = createAssertion("Sample", AssertType.LESSER, "${data.PhoneNumber}", "999999999990", null);
        RestelAssertionResolver.resolve(context, assertion);
    }

    @Test(expected = RestelException.class)
    public void testResolveAssertLesserInvalid() {
        TestContext context = new TestContext("Sample");
        context.addValue("data", Map.of("name", "Tom", "PhoneNumber", "9999999999"));
        RestelAssertion assertion = createAssertion("Sample", AssertType.LESSER, "${data.PhoneNumber}", "99999999r0", null);
        RestelAssertionResolver.resolve(context, assertion);
    }

    @Test(expected = RestelException.class)
    public void testResolveAssertGreaterInvalid() {
        TestContext context = new TestContext("Sample");
        context.addValue("data", Map.of("name", "Tom", "PhoneNumber", "9999999999"));
        RestelAssertion assertion = createAssertion("Sample", AssertType.LESSER, "99999999r0", "${data.PhoneNumber}", null);
        RestelAssertionResolver.resolve(context, assertion);
    }

    private RestelAssertion createAssertion(String name, AssertType type, String expected, String actual, String message) {
        RestelAssertion assertion = new RestelAssertion();
        assertion.setName(name);
        assertion.setAssertType(type);
        assertion.setExpected(expected);
        assertion.setActual(actual);
        return assertion;
    }
}
