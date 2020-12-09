package restel.core.resolver;

import com.pramati.restel.core.model.TestContext;
import com.pramati.restel.core.model.assertion.RestelAssertion;
import com.pramati.restel.core.resolver.RestelAssertionResolver;
import com.pramati.restel.utils.ObjectMapperUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RestelAssertionResolverTest {

    @Test
    public void testResolveForString() {
        RestelAssertion assertion = new RestelAssertion();
        assertion.setName("Sample");
        assertion.setExpected("${Name}");
        assertion.setActual("Tom");
        TestContext context = new TestContext("Sample");
        context.addValue("Name", "Tom");

        RestelAssertionResolver.resolve(context, assertion);
        Assert.assertEquals("Sample", assertion.getName());
    }

    @Test
    public void testResolveForJson() {
        Map<String, Object> data = new HashMap<>();
        data.put("animals", Arrays.asList("Cat", "Dog", "Rat"));
        RestelAssertion assertion = new RestelAssertion();
        assertion.setName("Sample");
        assertion.setExpected("${Name}");
        assertion.setActual(ObjectMapperUtils.convertToJsonNode(data).toPrettyString());
        TestContext context = new TestContext("Sample");
        context.addValue("Name", data);

        RestelAssertionResolver.resolve(context, assertion);
        Assert.assertEquals("Sample", assertion.getName());
    }

    @Test
    public void testResolveForJsonInExpected() {
        Map<String, Object> data = new HashMap<>();
        data.put("animals", Arrays.asList("Cat", "Dog", "Rat"));
        RestelAssertion assertion = new RestelAssertion();
        assertion.setName("Sample");
        assertion.setActual("${Name}");
        assertion.setExpected(ObjectMapperUtils.convertToJsonNode(data).toPrettyString());
        TestContext context = new TestContext("Sample");
        context.addValue("Name", data);

        RestelAssertionResolver.resolve(context, assertion);
        Assert.assertEquals("Sample", assertion.getName());
    }


    @Test(expected = AssertionError.class)
    public void testResolveForJsonFailure() {
        Map<String, Object> data = new HashMap<>();
        data.put("animals", Arrays.asList("Cat", "Dog", "Rat"));
        RestelAssertion assertion = new RestelAssertion();
        assertion.setName("Sample");
        assertion.setExpected("${Name}");
        assertion.setActual(data.get("animals").toString());
        TestContext context = new TestContext("Sample");
        context.addValue("Name", data);

        RestelAssertionResolver.resolve(context, assertion);
    }

    @Test(expected = AssertionError.class)
    public void testResolveForStringFailure() {
        RestelAssertion assertion = new RestelAssertion();
        assertion.setName("Sample");
        assertion.setExpected("${Name}");
        assertion.setActual("Ram");
        TestContext context = new TestContext("Sample");
        context.addValue("Name", "Krish");

        RestelAssertionResolver.resolve(context, assertion);
    }
}
