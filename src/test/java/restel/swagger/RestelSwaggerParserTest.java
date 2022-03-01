package restel.swagger;

import com.pramati.restel.exception.InvalidConfigException;
import com.pramati.restel.swagger.RestelSwaggerParser;
import org.junit.Assert;
import org.junit.Test;
import org.testng.collections.CollectionUtils;

public class RestelSwaggerParserTest {

    @Test
    public void testSwaggerPaser() {
        RestelSwaggerParser parser = new RestelSwaggerParser("src/test/resources/swagger/petstore_2.json");
        Assert.assertNotNull(parser.getBaseConfig());
        Assert.assertTrue(CollectionUtils.hasElements(parser.getTestDefinition()));
    }

    @Test
    public void testOpenAPIPaser() {
        RestelSwaggerParser parser = new RestelSwaggerParser("src/test/resources/swagger/petstore_3.json");
        Assert.assertNotNull(parser.getBaseConfig());
        Assert.assertTrue(CollectionUtils.hasElements(parser.getTestDefinition()));
    }

    @Test(expected = InvalidConfigException.class)
    public void testOpenAPIInvalidFile() {
        RestelSwaggerParser parser = new RestelSwaggerParser("src/test/resources/application.properties");
        parser.getBaseConfig();
        parser.getTestDefinition();
    }

    @Test(expected = InvalidConfigException.class)
    public void testOpenAPIEmptyFile() {
        RestelSwaggerParser parser = new RestelSwaggerParser("");
        parser.getBaseConfig();
    }
}
