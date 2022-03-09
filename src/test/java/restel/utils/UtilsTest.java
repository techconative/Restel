package restel.utils;

import com.techconative.restel.utils.Utils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;


public class UtilsTest {

    @Test
    public void testFindAndApply() {
        String result = Utils.findAndApplyOnString("some_snake_case", "_[a-z]",
                (x) -> x.replace("_", "").toUpperCase());
        Assert.assertEquals("someSnakeCase", result);
    }

    @Test
    public void testFindAndApplyInvalidPattern() {
        String result = Utils.findAndApplyOnString("some_snake_case", "_[A-Z]",
                (x) -> x.replace("_", "").toUpperCase());
        Assert.assertEquals("some_snake_case", result);
    }

    @Test
    public void testEmptyForNull() {
        String value = Utils.emptyForNull(null);
        Assert.assertEquals("", value);

        String notNullvalue = Utils.emptyForNull(Map.of("name", "Tom"));
        Assert.assertNotNull(notNullvalue);
    }

    @Test
    public void testFindAndApplyForSnakeCase() {
        String value = Utils.findAndApplyOnString("some_snake_case", "_[a-z]", (x) ->
                x.replace("_", "").toUpperCase());

        Assert.assertEquals("someSnakeCase", value);

    }

    @Test
    public void testFindAndApplyForSubstitute() {
        String value = Utils.findAndApplyOnString("${user_name}", "\\$\\{.*\\}", (x) ->
                x.replace("user_name", "name"));

        Assert.assertEquals("${name}", value);

    }


}

