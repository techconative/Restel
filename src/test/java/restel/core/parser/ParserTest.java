package restel.core.parser;

import com.techconative.restel.core.parser.Parser;
import com.techconative.restel.core.parser.ParserEnums;
import com.techconative.restel.core.parser.config.ParserConfig;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class ParserTest {

  @Test
  public void parserTest() throws Exception {
    Parser par = new Parser(ParserConfig.load());
    Map<String, Object> parsed =
        par.parse(
            Files.newInputStream(Paths.get("src/test/resources/Sample_Suite_definition.xlsx")));
    Assert.assertNotNull(parsed.get(ParserEnums.BASE_CONFIG.toString().toLowerCase()));
    Assert.assertNotNull(parsed.get(ParserEnums.TEST_SCENARIOS.toString().toLowerCase()));
    Assert.assertNotNull(parsed.get(ParserEnums.TEST_SUITES.toString().toLowerCase()));
    Assert.assertNotNull(parsed.get(ParserEnums.TEST_API_DEFINITIONS.toString().toLowerCase()));
  }
}
