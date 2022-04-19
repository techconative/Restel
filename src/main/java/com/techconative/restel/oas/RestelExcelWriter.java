package com.techconative.restel.oas;

import com.techconative.restel.core.parser.dto.BaseConfig;
import com.techconative.restel.core.parser.dto.TestApiDefinitions;
import com.techconative.restel.core.parser.dto.TestScenarios;
import com.techconative.restel.core.parser.dto.TestSuites;
import com.techconative.restel.exception.RestelException;
import com.techconative.restel.utils.Constants;
import com.techconative.restel.utils.Utils;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** Writer to write the Restel Excel Sheets. */
public class RestelExcelWriter {

  private XSSFWorkbook workbook;
  private XSSFSheet baseConfigSheet;
  private XSSFSheet testSuitesSheet;
  private XSSFSheet testSuiteExecutionSheet;
  private XSSFSheet testDefinitionSheet;

  public RestelExcelWriter() {
    init();
  }

  private void init() {
    // Blank workbook
    workbook = new XSSFWorkbook();

    // Create a baseConfigSheet sheet
    baseConfigSheet = workbook.createSheet(Constants.BASE_CONFIG);
    // Add field names/headers to the sheet.
    createColumnHeaders(baseConfigSheet, getBaseConfigHeaders());

    // Create a testDefinitionSheet sheet
    testDefinitionSheet = workbook.createSheet(Constants.TEST_API_DEFINITIONS);
    // Add field names/headers to the sheet.
    createRowHeaders(testDefinitionSheet, getTestDefinitionHeaders());

    // Create a testSuitesSheet sheet
    testSuitesSheet = workbook.createSheet(Constants.TEST_SUITE);
    // Add field names/headers to the sheet.
    createRowHeaders(testSuitesSheet, getTestSuiteHeaders());

    // Create a testSuiteExecutionSheet sheet
    testSuiteExecutionSheet = workbook.createSheet(Constants.TEST_SCENARIOS);
    // Add field names/headers to the sheet.
    createRowHeaders(testSuiteExecutionSheet, getTestSuiteExecutionHeaders());
  }

  /**
   * writes the test suites,test suites execution, Test definition and Base config sheets into the
   * specified file.
   *
   * @param fileName name of the excel file.
   */
  public void writeToFile(String fileName) {
    try {
      // this Writes the workbook
      FileOutputStream out = new FileOutputStream(fileName);
      workbook.write(out);
      out.close();
    } catch (Exception ex) {
      throw new RestelException(ex, "WRITE_ERROR", fileName);
    }
  }

  /**
   * write the testDefinitions to TestDefinitions sheets of restelExcel
   *
   * @param testApiDefinitions list of {@link TestApiDefinitions}
   */
  public void writeTestDefinitions(List<TestApiDefinitions> testApiDefinitions) {
    AtomicInteger rowNum = new AtomicInteger(1);
    testApiDefinitions.stream()
        .forEach(
            td -> {
              Row row = testDefinitionSheet.createRow(rowNum.getAndIncrement());
              AtomicInteger colNum = new AtomicInteger();

              // The order of adding cell value should not be disturbed.
              addCellValue(row, colNum.getAndIncrement(), td.getApiUniqueName());
              addCellValue(row, colNum.getAndIncrement(), td.getDependsOn());
              addCellValue(row, colNum.getAndIncrement(), td.getApiDescription());
              // replace path param with variable declaration
              addCellValue(row, colNum.getAndIncrement(), td.getRequestUrl().replace("/{", "/${"));
              addCellValue(row, colNum.getAndIncrement(), td.getRequestMethod());
              addCellValue(row, colNum.getAndIncrement(), td.getRequestQueryParams());
              addCellValue(row, colNum.getAndIncrement(), td.getRequestHeaders());
              addCellValue(row, colNum.getAndIncrement(), td.getRequestBodyParams());
              addCellValue(row, colNum.getAndIncrement(), td.getRequestPreCallHook());
              addCellValue(row, colNum.getAndIncrement(), td.getRequestPostCallHook());
              addCellValue(row, colNum.getAndIncrement(), td.getExpectedResponse());
              // add NOOP_MATCHER as default matcher.
              addCellValue(
                  row,
                  colNum.getAndIncrement(),
                  StringUtils.isNotBlank(td.getExpectedResponseMatcher())
                      ? td.getExpectedResponseMatcher()
                      : Constants.NOOP_MATCHER);
              addCellValue(row, colNum.getAndIncrement(), td.getExpectedHeader());
              // add NOOP_MATCHER as default matcher.
              addCellValue(
                  row,
                  colNum.getAndIncrement(),
                  StringUtils.isNotBlank(td.getExpectedHeaderMatcher())
                      ? td.getExpectedHeaderMatcher()
                      : Constants.NOOP_MATCHER);
              // replace default with 200
              addCellValue(
                  row,
                  colNum.getAndIncrement(),
                  td.getAcceptedStatusCodes().stream()
                      .map(
                          code ->
                              StringUtils.equalsAnyIgnoreCase(code, Constants.DEFAULT)
                                  ? String.valueOf(HttpStatus.SC_OK)
                                  : code)
                      .collect(Collectors.joining(", ")));
              addCellValue(row, colNum.getAndIncrement(), String.join(",", td.getTags()));
            });
  }

  /**
   * add the value in the given column number of the row.
   *
   * @param row {@link Row}
   * @param colNum column number of the row.
   * @param value Value to be added into the Cell.
   */
  private void addCellValue(Row row, int colNum, String value) {
    Cell cell = row.createCell(colNum);
    if (StringUtils.isNotEmpty(value)) {
      cell.setCellValue(value);
    }
  }

  /**
   * writes the TestSuites to Test Suite sheet of restel excel sheet.
   *
   * @param testSuites list of {@link TestSuites}
   */
  public void writeTestSuites(List<TestSuites> testSuites) {
    AtomicInteger rowNum = new AtomicInteger(1);
    testSuites.parallelStream()
        .forEach(
            ts -> {
              Row row = testSuitesSheet.createRow(rowNum.getAndIncrement());
              AtomicInteger colNum = new AtomicInteger();

              // The order to adding cell value should not be disturbed.
              addCellValue(row, colNum.getAndIncrement(), ts.getSuiteUniqueName());
              addCellValue(row, colNum.getAndIncrement(), ts.getSuiteDescription());
              addCellValue(row, colNum.getAndIncrement(), Utils.toCsv(ts.getSuiteScenariosList()));
              addCellValue(row, colNum.getAndIncrement(), ts.getDependsOn());
              addCellValue(row, colNum.getAndIncrement(), ts.getSuiteParams());
              addCellValue(row, colNum.getAndIncrement(), ts.getSuiteEnable().toString());
            });
  }

  /**
   * writes the TestSuiteExecutions to Test Suite Execution sheet of restel excel sheet.
   *
   * @param testSuiteExecutions list of {@link TestScenarios}
   */
  public void writeTestSuiteExecution(List<TestScenarios> testSuiteExecutions) {
    AtomicInteger rowNum = new AtomicInteger(1);
    testSuiteExecutions.parallelStream()
        .forEach(
            tse -> {
              Row row = testSuiteExecutionSheet.createRow(rowNum.getAndIncrement());
              AtomicInteger colNum = new AtomicInteger();

              // The order to adding cell value should not be disturbed.
              addCellValue(row, colNum.getAndIncrement(), tse.getScenarioUniqueName());
              addCellValue(row, colNum.getAndIncrement(), Utils.toCsv(tse.getTestApis()));
              addCellValue(row, colNum.getAndIncrement(), tse.getDependsOn());
              addCellValue(row, colNum.getAndIncrement(), tse.getScenarioParams());
              addCellValue(row, colNum.getAndIncrement(), tse.getScenarioEnabled().toString());
            });
  }

  /**
   * writes the BaseConfig to Base config sheet of restel excel sheet.
   *
   * @param baseConfig {@link BaseConfig}
   */
  public void writeBaseConfig(BaseConfig baseConfig) {
    AtomicInteger rowCount = new AtomicInteger();

    // the order of adding cell value should not be disturbed
    addCellValue(baseConfigSheet.getRow(rowCount.getAndIncrement()), 1, baseConfig.getAppName());
    addCellValue(baseConfigSheet.getRow(rowCount.getAndIncrement()), 1, baseConfig.getBaseUrl());
    addCellValue(
        baseConfigSheet.getRow(rowCount.getAndIncrement()), 1, baseConfig.getDefaultHeader());
  }

  /** @return get BaseConfiguration which was written into the sheet */
  public XSSFSheet getBaseConfigSheet() {
    return baseConfigSheet;
  }

  /** @return get TestDefinitions which was written into the sheet */
  public XSSFSheet getTestDefinitionSheet() {
    return testDefinitionSheet;
  }

  /** @return get TestSuiteExecution which was written into the sheet */
  public XSSFSheet getTestSuiteExecutionSheet() {
    return testSuiteExecutionSheet;
  }

  /** @return get TestSuites which was written into the sheet */
  public XSSFSheet getTestSuitesSheet() {
    return testSuitesSheet;
  }

  /**
   * Create/add headers in the zeroth row of the Sheet.
   *
   * @param sheet {@link Sheet}
   * @param headerNames header names to be added into the zeroth row .
   */
  private void createRowHeaders(Sheet sheet, List<String> headerNames) {
    Row headers = sheet.createRow(0);
    AtomicInteger cellNum = new AtomicInteger();
    for (String header : headerNames) {
      Cell cell = headers.createCell(cellNum.getAndIncrement());
      cell.setCellValue(header);
    }
  }

  /**
   * Create/add headers in the zeroth column of the Sheet.
   *
   * @param sheet {@link Sheet}
   * @param headerNames header names to be added into the zeroth column .
   */
  private void createColumnHeaders(Sheet sheet, List<String> headerNames) {
    AtomicInteger rowNum = new AtomicInteger();
    for (String header : headerNames) {
      Row row = sheet.createRow(rowNum.getAndIncrement());
      Cell cell = row.createCell(0);
      cell.setCellValue(header);
    }
  }

  private List<String> getTestDefinitionHeaders() {
    // Order of elements should not be altered
    return Arrays.asList(
        Constants.API_UNIQUE_NAME,
        Constants.DEPENDS_ON,
        Constants.API_DESCRIPTION,
        Constants.REQUEST_URL,
        Constants.REQUEST_METHOD,
        Constants.REQUEST_QUERY_PARAMS,
        Constants.REQUEST_HEADERS,
        Constants.REQUEST_BODY_PARAMS,
        Constants.REQUEST_PRE_CALL_HOOK,
        Constants.REQUEST_POST_CALL_HOOK,
        Constants.EXPECTED_RESPONSE,
        Constants.EXPECTED_RESPONSE_MATCHER,
        Constants.EXPECTED_HEADER,
        Constants.EXPECTED_HEADER_MATCHER,
        Constants.ACCEPTED_STATUS_CODES,
        Constants.TAGS);
  }

  private List<String> getBaseConfigHeaders() {
    // Order of elements should not be altered
    return Arrays.asList(Constants.APP_NAME, Constants.BASE_URL, Constants.DEFAULT_HEADER);
  }

  private List<String> getTestSuiteHeaders() {
    // Order of elements should not be altered
    return Arrays.asList(
        Constants.SUITE_UNIQUE_NAME,
        Constants.SUITE_DESC,
        Constants.SUITE_SCENARIO_LIST,
        Constants.DEPENDS_ON,
        Constants.SUITE_PARAMS,
        Constants.SUITE_ENABLE);
  }

  private List<String> getTestSuiteExecutionHeaders() {
    // Order of elements should not be altered
    return Arrays.asList(
        Constants.SCENARIO_UNIQUE_NAME,
        Constants.TEST_APIS,
        Constants.DEPENDS_ON,
        Constants.SCENARIO_PARAMS,
        Constants.SCENARIO_ENABLED,
        Constants.TEST_ASSERTION,
        Constants.TEST_FUNCTION);
  }
}
