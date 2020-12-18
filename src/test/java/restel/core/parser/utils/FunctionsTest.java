package restel.core.parser.utils;

import com.pramati.restel.core.model.assertion.AssertType;
import com.pramati.restel.core.parser.util.Functions;
import com.pramati.restel.utils.Constants;
import com.pramati.restel.utils.ObjectMapperUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        Assert.assertTrue(Functions.TO_LIST.apply(cell) instanceof List);
    }

    @Test
    public void testToSetFunction() {
        cell.setCellValue("name,name");
        Assert.assertTrue(Functions.TO_SET.apply(cell) instanceof Set);
        Assert.assertEquals(1, Functions.TO_SET.apply(cell).size());
    }

    @Test
    public void testDoubleFunction() {
        cell.setCellValue(23.22);
        Assert.assertTrue(Functions.DOUBLE_FUNCTION.apply(cell) instanceof Double);
    }

    private Cell getCell() throws IOException {
        FileInputStream inputStream = new FileInputStream(new File("src/test/resources/Sample_Suite_definition.xlsx"));

        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet firstSheet = workbook.getSheet(Constants.TEST_DEFINITIONS);
        for (Row cells : firstSheet) {
            return cells.getCell(0);
        }
        return null;
    }
}
