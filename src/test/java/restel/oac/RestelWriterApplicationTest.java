package restel.oac;

import com.techconative.restel.core.managers.ExcelParseManager;
import com.techconative.restel.exception.InvalidConfigException;
import com.techconative.restel.oas.RestelWriterApplication;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class RestelWriterApplicationTest {

  @Test
  public void testApplicationToPath() throws IOException {
    String writepath = "src/test/resources/swagger/petstore.xlsx";
    RestelWriterApplication.main(
        new String[] {"src/test/resources/swagger/petstore_3.json", writepath});
    ExcelParseManager manager = new ExcelParseManager(writepath);
    Assert.assertNotNull(manager);
    remove(writepath);
  }

  @Test
  public void testApplicationFromUrl() throws IOException {
    String writepath = "swagger.xlsx";
    RestelWriterApplication.main(new String[] {"https://petstore.swagger.io/v2/swagger.json"});
    ExcelParseManager manager = new ExcelParseManager(writepath);
    Assert.assertNotNull(manager);
    remove(writepath);
  }

  @Test
  public void testApplication() throws IOException {
    String writepath = "src/test/resources/swagger/petstore_3.xlsx";
    RestelWriterApplication.main(new String[] {"src/test/resources/swagger/petstore_3.json"});
    ExcelParseManager manager = new ExcelParseManager(writepath);
    Assert.assertNotNull(manager);
    remove(writepath);
  }

  @Test(expected = InvalidConfigException.class)
  public void testApplicationVoid() {
    RestelWriterApplication.main(new String[] {});
    ExcelParseManager manager = new ExcelParseManager("src/test/resources/swagger/petstore_3.xlsx");
  }

  public void remove(String filepath) throws IOException {
    FileUtils.forceDelete(new File(filepath));
  }
}
