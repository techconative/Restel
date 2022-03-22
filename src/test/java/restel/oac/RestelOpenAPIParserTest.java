package restel.oac;

import com.pramati.restel.exception.InvalidConfigException;
import com.pramati.restel.oas.RestelOpenAPIParser;
import org.junit.Assert;
import org.junit.Test;
import org.testng.collections.CollectionUtils;

public class RestelOpenAPIParserTest {

    @Test
    public void testOACSpec2Paser() {
        RestelOpenAPIParser parser = new RestelOpenAPIParser("src/test/resources/swagger/petstore_2.json");
        Assert.assertNotNull(parser.getBaseConfig());
        Assert.assertTrue(CollectionUtils.hasElements(parser.getTestDefinition()));
    }

    @Test
    public void testOACSpec3Paser() {
        RestelOpenAPIParser parser = new RestelOpenAPIParser("src/test/resources/swagger/petstore_3.json");
        Assert.assertNotNull(parser.getBaseConfig());
        Assert.assertTrue(CollectionUtils.hasElements(parser.getTestDefinition()));
    }

    @Test(expected = InvalidConfigException.class)
    public void testOACSpec3InvalidFile() {
        RestelOpenAPIParser parser = new RestelOpenAPIParser("src/test/resources/application.properties");
        parser.getBaseConfig();
        parser.getTestDefinition();
    }

    @Test(expected = InvalidConfigException.class)
    public void testOACSpec3EmptyFile() {
        RestelOpenAPIParser parser = new RestelOpenAPIParser("");
        parser.getBaseConfig();
    }
}
