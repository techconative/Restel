package restel.core.parser.utils;

import com.techconative.restel.core.parser.util.Functions;
import com.techconative.restel.utils.Constants;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FunctionsTest {

  Cell cell;

  @Before
  public void before() throws IOException {
    cell = getCell();
  }

  @Test
  public void testStringFunction() {
    Assert.assertTrue(StringUtils.isNotBlank(Functions.STRING_FUNCTION.apply(cell)));
  }

  @Test
  public void testCameCaseFunction() {
    Assert.assertEquals("cellName", Functions.TO_CAMEL_CASE.apply("cell_name"));
  }

  @Test
  public void testPascalCaseFunction() {
    Assert.assertEquals("CellName", Functions.TO_PASCAL_CASE.apply("cell_name"));
  }

  @Test
  public void testBooleanFunction() {
    cell.setCellValue("true");
    Assert.assertEquals(Boolean.TRUE, Functions.TO_BOOLEAN.apply(cell));
    cell.setCellValue(true);
    Assert.assertEquals(Boolean.TRUE, Functions.TO_BOOLEAN.apply(cell));
  }

  @Test
  public void testSetterFunction() {
    Assert.assertEquals("setCell", Functions.SETTER.apply("cell"));
  }

  @Test
  public void testIntListFunction() {
    cell.setCellValue("200,100");
    Assert.assertTrue(Functions.TO_INT_LIST.apply(cell) instanceof List);
  }

  @Test
  public void testToListFunction() {
    cell.setCellValue("name,value");
    Assert.assertTrue(Functions.TO_STRING_LIST.apply(cell) instanceof List);
    cell.setCellValue(" create_entry  ,  read_ entry,update_entry,  delete_entry  ");
    Assert.assertEquals("create_entry", Functions.TO_STRING_LIST.apply(cell).get(0));
    Assert.assertEquals("read_ entry", Functions.TO_STRING_LIST.apply(cell).get(1));
    Assert.assertEquals("update_entry", Functions.TO_STRING_LIST.apply(cell).get(2));
    Assert.assertEquals("delete_entry", Functions.TO_STRING_LIST.apply(cell).get(3));
  }

  @Test
  public void testToSetFunction() {
    cell.setCellValue("name,name");
    Assert.assertTrue(Functions.TO_SET.apply(cell) instanceof Set);
    Assert.assertEquals(1, Functions.TO_SET.apply(cell).size());
    cell.setCellValue(" create_entry  ,  read_ entry,update_entry,  delete_entry  ");
    Assert.assertTrue(Functions.TO_SET.apply(cell).contains("create_entry"));
    Assert.assertTrue(Functions.TO_SET.apply(cell).contains("read_ entry"));
    Assert.assertTrue(Functions.TO_SET.apply(cell).contains("update_entry"));
    Assert.assertTrue(Functions.TO_SET.apply(cell).contains("delete_entry"));
  }

  @Test
  public void testDoubleFunction() {
    cell.setCellValue(23.22);
    Assert.assertTrue(Functions.DOUBLE_FUNCTION.apply(cell) instanceof Double);
  }

  private Cell getCell() throws IOException {
    FileInputStream inputStream =
        new FileInputStream(new File("src/test/resources/Sample_Suite_definition.xlsx"));

    Workbook workbook = new XSSFWorkbook(inputStream);
    Sheet firstSheet = workbook.getSheet(Constants.TEST_API_DEFINITIONS);
    for (Row cells : firstSheet) {
      return cells.getCell(0);
    }
    return null;
  }
}
