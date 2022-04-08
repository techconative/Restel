package com.techconative.restel.core.parser;

import com.techconative.restel.core.parser.config.ParserConfig;
import com.techconative.restel.core.parser.util.Functions;
import com.techconative.restel.exception.RestelException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Parser type. Parses the excel file based on ParserConfig and returns map of sheet name and bean
 * objects in the sheet
 */
@Slf4j
public class Parser {

  private ParserConfig parserConfig;

  /**
   * Instantiates a new Parser.
   *
   * @param parserConfig the parser config
   */
  public Parser(ParserConfig parserConfig) {
    this.parserConfig = parserConfig;
  }

  /**
   * Parse inputStream of xlsx file and returns map of sheet name and corresponding bean object(s)
   * in the sheet Sample run: FileInputStream inputStream = new FileInputStream(new
   * File("/home/restel/Sample_Suite_definition.xlsx")); Parser parser = new
   * Parser(ParserConfig.load()); Map<String, Object> beanObjectMap = parser.parse(inputStream);
   *
   * @param inputStream the input stream of excel file
   * @return the map of sheet name and corresponding bean object(s) in the sheet
   */
  public Map<String, Object> parse(InputStream inputStream) {
    Map<String, Object> beanObjectMap = new HashMap<>();
    try {
      Workbook workbook = new XSSFWorkbook(inputStream);
      /*
      for each sheet name in sheetMap, parse the corresponding sheet in excel and return bean object(s)
       */
      for (Map.Entry<String, ParserConfig.SheetConfig> entry :
          this.parserConfig.getSheetMap().entrySet()) {
        String sheetName = entry.getKey();
        Sheet sheet = workbook.getSheet(sheetName);
        if (Objects.isNull(sheet)) {
          log.info("Sheet with name " + sheetName + " does not exist");
          continue;
        }
        ParserConfig.SheetConfig sheetConfig = entry.getValue();
        /*
        Parsing a sheet of type SheetType.ROW, returns single beanObject
        Parsing a sheet of type SheetType.COLUMN, returns list of beanObjects
         */
        if (ParserConfig.SheetType.ROW.equals(sheetConfig.getSheetType())) {
          Object beanObject = rowTypeSheetParser(sheet, sheetConfig);
          beanObjectMap.put(sheetName, beanObject);
        } else if (ParserConfig.SheetType.COLUMN.equals(sheetConfig.getSheetType())) {
          List<Object> beanObjects = columnTypeParser(sheet, sheetConfig);
          beanObjectMap.put(sheetName, beanObjects);
        }
      }
    } catch (Exception e) {
      throw new RestelException(e, "PARSER_FAILED");
    }
    return beanObjectMap;
  }

  /*
  Parses sheet of type SheetType.ROW
  Returns single beanObject of second column in the sheet
  Note: First column is considered field name in bean object
   */
  private Object rowTypeSheetParser(Sheet sheet, ParserConfig.SheetConfig sheetConfig)
      throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
          IllegalAccessException, InvocationTargetException {
    // map of field name and its row number
    Map<String, Integer> fieldRowNumMap = new HashMap<>();
    int columnNum = 0;
    int rowNum = 0;
    while (true) {
      if (!hasCellValue(sheet, rowNum, columnNum)) {
        break;
      }
      fieldRowNumMap.put(sheet.getRow(rowNum).getCell(columnNum).getStringCellValue(), rowNum);
      rowNum++;
    }
    // return bean constructed from second column
    return populateBean(sheet, fieldRowNumMap, sheetConfig, null, 1);
  }

  private boolean hasCellValue(Sheet sheet, int rowNum, int columnNum) {
    if (Objects.isNull(sheet.getRow(rowNum))) {
      return false;
    }
    return !isBlank(sheet.getRow(rowNum).getCell(columnNum));
  }

  /*
  Parses sheet of type SheetType.COLUMN
  Returns list of beanObjects of each row in the sheet.
  Note: First row is considered field name in bean object
   */
  private List<Object> columnTypeParser(Sheet sheet, ParserConfig.SheetConfig sheetConfig)
      throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
          IllegalAccessException, InvocationTargetException {
    List<Object> beanObjects = new ArrayList<>();
    // map of field name and its column number
    Map<String, Integer> fieldColumnNumMap = new HashMap<>();
    int columnNum = 0;
    int rowNum = 0;
    while (true) {
      Cell cell = sheet.getRow(rowNum).getCell(columnNum);
      if (isBlank(cell)) {
        break;
      }
      fieldColumnNumMap.put(cell.getStringCellValue(), columnNum);
      columnNum++;
    }
    for (Row row : sheet) {
      // skip first row with headers
      if (row.getRowNum() == 0 || isRowEmpty(row)) {
        continue;
      }
      // populate bean for other rows
      beanObjects.add(populateBean(sheet, fieldColumnNumMap, sheetConfig, row.getRowNum(), null));
    }
    return beanObjects;
  }

  /*
  Returns boolean. Checks if Cell is empty
   */
  private boolean isBlank(Cell cell) {
    return cell == null || CellType.BLANK == cell.getCellType();
  }

  /*
  Initializes and populates the beanObject
  If columnNum is available -> SheetType.ROW. Parse each row in the columnNum
  If rowNum is available -> SheetType.COLUMN. Parse each column in the rowNum
   */
  private Object populateBean(
      Sheet sheet,
      Map<String, Integer> fieldMap,
      ParserConfig.SheetConfig sheetConfig,
      Integer rowNum,
      Integer columnNum)
      throws InvocationTargetException, IllegalAccessException, ClassNotFoundException,
          NoSuchMethodException, InstantiationException {
    Class<?> beanClass =
        Class.forName(
            ParserConfig.BEAN_PACKAGE_PATH
                + "."
                + this.parserConfig.getBeanClassNameFunction().apply(sheet.getSheetName()));
    Method[] methods = beanClass.getMethods();
    Constructor<?> constructor = beanClass.getConstructor();
    Object beanObject = constructor.newInstance();
    // SheetType = ROW
    if (Objects.isNull(rowNum) && !Objects.isNull(columnNum)) {
      populateSheet(
          sheet, fieldMap, sheetConfig, columnNum, methods, beanObject, ParserConfig.SheetType.ROW);
    }
    // SheetType = COLUMN
    else if (!Objects.isNull(rowNum) && Objects.isNull(columnNum)) {
      populateSheet(
          sheet, fieldMap, sheetConfig, rowNum, methods, beanObject, ParserConfig.SheetType.COLUMN);
    } else {
      beanObject = null;
    }
    return beanObject;
  }

  private void populateSheet(
      Sheet sheet,
      Map<String, Integer> fieldMap,
      ParserConfig.SheetConfig sheetConfig,
      Integer sheetTypeNum,
      Method[] methods,
      Object beanObject,
      ParserConfig.SheetType sheetType)
      throws InvocationTargetException, IllegalAccessException {
    for (Map.Entry<String, Function<Cell, ?>> e : sheetConfig.getFieldMap().entrySet()) {
      String fieldName = e.getKey();
      if (fieldMap.containsKey(fieldName)) {
        Cell cell =
            sheetType == ParserConfig.SheetType.ROW
                ? sheet.getRow(fieldMap.get(fieldName)).getCell(sheetTypeNum)
                : sheet.getRow(sheetTypeNum).getCell(fieldMap.get(fieldName));
        if (!isBlank(cell)) {
          Optional<Method> methodOptional =
              Arrays.stream(methods)
                  .filter(m -> Functions.SETTER.apply(fieldName).equals(m.getName()))
                  .findFirst();
          if (methodOptional.isPresent()) {
            methodOptional.get().invoke(beanObject, e.getValue().apply(cell));
          }
        }
      }
    }
  }

  /*
  Returns boolean. Checks if Row is empty
   */
  private static boolean isRowEmpty(Row row) {
    if (row == null || row.getLastCellNum() <= 0) {
      return true;
    }
    boolean isEmpty = true;

    for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
      Cell cell = row.getCell(cellNum);
      if (cell != null && cell.getCellType() != CellType.BLANK) {
        isEmpty = false;
      }
    }
    return isEmpty;
  }
}
