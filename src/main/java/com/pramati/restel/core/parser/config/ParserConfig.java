package com.pramati.restel.core.parser.config;

import com.pramati.restel.core.parser.ParserEnums;
import com.pramati.restel.core.parser.util.Functions;
import com.pramati.restel.utils.Constants;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.apache.poi.ss.usermodel.Cell;

/**
 * Master configuration for the excel parser. Configuration includes: 1) sheetMap: <sheetname,
 * sheetconfig>. sheetname: name of the sheet in excel sheetconfig includes a) sheettype: enum with
 * values ROW, COLUMN b) fieldMap: <fieldname, cellFunction>. fieldname: name of field in the sheet
 * cellFunction: value fetch function to get cell value based on cell type 2) beanClassNameFunction:
 * function which returns corresponding bean class name string for input string. Eg. Input:
 * "base_config" Output: "BaseConfig" 3) BEAN_PACKAGE_PATH: package path string where dtos are
 * located. Eg. "com.restel.parser.dto"
 */
public class ParserConfig {

  Map<String, SheetConfig> sheetMap = new HashMap<>();

  Map<String, String> sheetNameToClassMap =
      Map.of(
          ParserEnums.TEST_DEFINITIONS.toString(),
          ParserEnums.TEST_DEFINITIONS.getValue(),
          ParserEnums.TEST_SUITE_EXECUTION.toString(),
          ParserEnums.TEST_SUITE_EXECUTION.getValue(),
          ParserEnums.TEST_SUITES.toString(),
          ParserEnums.TEST_SUITES.getValue(),
          ParserEnums.BASE_CONFIG.toString(),
          ParserEnums.BASE_CONFIG.getValue());

  UnaryOperator<String> beanClassNameFunction =
      sheetName -> sheetNameToClassMap.get(sheetName.toUpperCase());

  public static final String BEAN_PACKAGE_PATH = "com.pramati.restel.core.parser.dto";

  /**
   * Instantiate a new ParserConfig with default values in init()
   *
   * @return instance of ParserConfig
   */
  public static ParserConfig load() {
    return new ParserConfig();
  }

  public ParserConfig() {
    init();
  }

  /** Initialize ParserConfig instance with default values */
  private void init() {
    Map<String, Function<Cell, ?>> fieldMap;
    /*
    fieldMap configuration for each sheet
    configuration contains sheet name, sheet type, field names in the sheet and their corresponding value fetch functions
    Note: Please make changes here for sheet/field changes in excel
     */

    // Base_Config sheet - start
    fieldMap = new HashMap<>();
    fieldMap.put(Constants.APP_NAME, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.BASE_URL, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.DEFAULT_HEADER, Functions.STRING_FUNCTION);
    this.sheetMap.put(Constants.BASE_CONFIG, new SheetConfig(SheetType.ROW, fieldMap));
    // Base_Config sheet - end

    // Test_Suites sheet - start
    fieldMap = new HashMap<>();
    fieldMap.put(Constants.SUITE_UNIQUE_NAME, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.SUITE_DESC, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.DEPENDS_ON, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.SUITE_PARAMS, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.SUITE_ENABLE, Functions.TO_BOOLEAN);
    this.sheetMap.put(Constants.TEST_SUITES, new SheetConfig(SheetType.COLUMN, fieldMap));
    // Test_Suites sheet - end

    // Test_Suite_Execution sheet - start
    fieldMap = new HashMap<>();
    fieldMap.put(Constants.TEST_EXEC_UNIQUE_NAME, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.TEST_SUITE, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.TEST_CASE, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.DEPENDS_ON, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.TEST_TAG, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.TEST_EXECUTION_ENABLE, Functions.TO_BOOLEAN);
    fieldMap.put(Constants.TEST_EXECUTION_PARAMS, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.TEST_ASSERTION, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.TEST_FUNCTION, Functions.STRING_FUNCTION);

    this.sheetMap.put(Constants.TEST_SUITE_EXECUTION, new SheetConfig(SheetType.COLUMN, fieldMap));
    // Test_Suite_Execution sheet - end

    // Test_Definitions sheet - start
    fieldMap = new HashMap<>();
    fieldMap.put(Constants.CASE_UNIQUE_NAME, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.DEPENDS_ON, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.CASE_DESCRIPTION, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.REQUEST_URL, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.REQUEST_METHOD, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.REQUEST_HEADERS, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.REQUEST_PATH_PARAMS, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.REQUEST_QUERY_PARAMS, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.REQUEST_BODY_PARAMS, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.REQUEST_PRE_CALL_HOOK, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.REQUEST_POST_CALL_HOOK, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.EXPECTED_RESPONSE, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.EXPECTED_RESPONSE_MATCHER, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.EXPECTED_HEADER, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.EXPECTED_HEADER_MATCHER, Functions.STRING_FUNCTION);
    fieldMap.put(Constants.ACCEPTED_STATUS_CODES, Functions.TO_LIST);
    fieldMap.put(Constants.TAGS, Functions.TO_SET);
    this.sheetMap.put(Constants.TEST_DEFINITIONS, new SheetConfig(SheetType.COLUMN, fieldMap));
    // Test_Definitions sheet - end

  }

  /**
   * Gets the sheet map from ParserConfig instance
   *
   * @return the sheet map
   */
  public Map<String, SheetConfig> getSheetMap() {
    return sheetMap;
  }

  /**
   * Gets bean class name function from ParserConfig instance
   *
   * @return the bean class name function
   */
  public Function<String, String> getBeanClassNameFunction() {
    return beanClassNameFunction;
  }

  /**
   * SheetType enum ROW: header field values are available in first cell of each row COLUMN: header
   * field values are available in first cell of each column
   */
  public enum SheetType {
    ROW,
    COLUMN
  }

  /** SheetConfig type */
  public class SheetConfig {

    private SheetType sheetType;

    private Map<String, Function<Cell, ?>> fieldMap;

    /**
     * Instantiates a new SheetConfig with input values
     *
     * @param sheetType SheetType enum
     * @param fieldMap the field map
     */
    public SheetConfig(SheetType sheetType, Map<String, Function<Cell, ?>> fieldMap) {
      this.sheetType = sheetType;
      this.fieldMap = fieldMap;
    }

    /**
     * Gets fieldMap
     *
     * @return the field map
     */
    public Map<String, Function<Cell, ?>> getFieldMap() {
      return fieldMap;
    }

    /**
     * Gets sheetType
     *
     * @return the sheet type
     */
    public SheetType getSheetType() {
      return sheetType;
    }
  }
}
