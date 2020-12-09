package restel.core.parser;

import com.pramati.restel.core.parser.Parser;
import com.pramati.restel.core.parser.ParserEnums;
import com.pramati.restel.core.parser.config.ParserConfig;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class ParserTest {

    @Test
    public void parserTest() throws Exception {
        Parser par = new Parser(ParserConfig.load());
        Map<String, Object> parsed = par.parse(Files.newInputStream(Paths.get("src/test/resources/Sample_Suite_definition.xlsx")));
        Assert.assertNotNull(parsed.get(ParserEnums.BASE_CONFIG.toString().toLowerCase()));
        Assert.assertNotNull(parsed.get(ParserEnums.TEST_SUITE_EXECUTION.toString().toLowerCase()));
        Assert.assertNotNull(parsed.get(ParserEnums.TEST_SUITES.toString().toLowerCase()));
        Assert.assertNotNull(parsed.get(ParserEnums.TEST_DEFINITIONS.toString().toLowerCase()));

    }
}
