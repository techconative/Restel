package com.pramati.restel.utils;

public class Constants {
  private Constants() {}

  // general
  public static final String HTTP = "http";

  // Special character
  public static final String OPEN_PARENTHESES = "(";
  public static final String CLOSE_PARENTHESES = ")";
  public static final String COMMA = ",";
  public static final String DOT = ".";
  public static final String AT_RATE = "@";

  // Auth  constants
  public static final String BASIC_AUTH = "basic_auth";
  public static final String OAUTH2 = "oauth2";
  public static final String CLIENT_ID = "client_id";
  public static final String CLIENT_SECRET = "client_secret";
  public static final String GRANT_TYPE = "grant_type";
  public static final String SCOPE = "scope";
  public static final String CLIENT_CREDENTIALS = "client_credentials";
  public static final String PASSWORD = "password";
  public static final String USERNAME = "username";
  public static final String BEARER = "Bearer";
  public static final String BASIC = "Basic";
  public static final String ACCESS_TOKEN = "access_token";
  public static final String WRITE = "write";
  // Parser constants
  // Base Config
  public static final String APP_NAME = "app_name";
  public static final String BASE_URL = "base_url";
  public static final String DEFAULT_HEADER = "default_header";
  public static final String BASE_CONFIG = "base_config";

  // Test suite
  public static final String SUITE_UNIQUE_NAME = "suite_unique_name";
  public static final String SUITE_DESC = "suite_description";
  public static final String DEPENDS_ON = "depends_on";
  public static final String SUITE_PARAMS = "suite_params";
  public static final String SUITE_ENABLE = "suite_enable";
  public static final String TEST_SUITES = "test_suites";

  // Test suite execution
  public static final String TEST_EXEC_UNIQUE_NAME = "test_execution_unique_name";
  public static final String TEST_SUITE = "test_suite";
  public static final String TEST_CASE = "test_case";
  public static final String TEST_EXECUTION_ENABLE = "test_execution_enable";
  public static final String TEST_EXECUTION_PARAMS = "test_execution_params";
  public static final String TEST_SUITE_EXECUTION = "test_suite_execution";
  public static final String TEST_TAG = "test_tag";
  public static final String TEST_ASSERTION = "assertion";
  public static final String TEST_FUNCTION = "function";

  // Test Definition
  public static final String CASE_UNIQUE_NAME = "case_unique_name";
  public static final String CASE_DESCRIPTION = "case_description";
  public static final String REQUEST_URL = "request_url";
  public static final String REQUEST_METHOD = "request_method";
  public static final String REQUEST_HEADERS = "request_headers";
  public static final String REQUEST_PATH_PARAMS = "request_path_params";
  public static final String REQUEST_QUERY_PARAMS = "request_query_params";
  public static final String REQUEST_BODY_PARAMS = "request_body_params";
  public static final String REQUEST_PRE_CALL_HOOK = "request_pre_call_hook";
  public static final String REQUEST_POST_CALL_HOOK = "request_post_call_hook";
  public static final String EXPECTED_RESPONSE = "expected_response";
  public static final String EXPECTED_RESPONSE_MATCHER = "expected_response_matcher";
  public static final String EXPECTED_HEADER = "expected_header";
  public static final String EXPECTED_HEADER_MATCHER = "expected_header_matcher";
  public static final String ACCEPTED_STATUS_CODES = "accepted_status_codes";
  public static final String TAGS = "tags";
  public static final String TEST_DEFINITIONS = "test_definitions";

  // Open API parser
  public static final String HASH_COMPONENT = "#/component";
  public static final String REQUEST_BODIES = "requestBodies";
  public static final String SCHEMA = "schema";
  public static final String PARAMETER = "parameter";
  public static final String HEADER = "header";
  public static final String QUERY = "query";
  public static final String BODY = "body";
  public static final String RESPONSE = "response";
  public static final String FORM_DATA = "formData";
  public static final String REQUEST = "request";
  // Writer
  public static final String NOOP_MATCHER = "NOOP_MATCHER";
  public static final String DEFAULT = "default";

  // Restel Function constants
  public static final String REMOVE = "remove";
  public static final String ADD = "add";
  public static final String OPERATION = "operation";
  public static final String DATA = "data";
  public static final String ARGS = "args";

  // Restel Assertion constants
  public static final String CONDITION = "condition";
  public static final String MESSAGE = "message";

  // patterns
  public static final String VARIABLE_PATTERN = "\\$\\{.*\\}";
  public static final String RESPONSE_PATTERN = "^(.*[^\\.])\\.response.*";
  public static final String REQUEST_PATTERN = "^(.*[^\\.])\\.request.*";
  public static final String NS_SEPARATOR_REGEX = "\\" + DOT;
  public static final String ARRAY_PATTERN = "\\[(?:\\*|[\\d+,*]+)+\\]";
}
