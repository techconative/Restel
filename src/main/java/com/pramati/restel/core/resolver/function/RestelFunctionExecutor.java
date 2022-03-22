package com.pramati.restel.core.resolver.function;

import static com.pramati.restel.utils.Constants.*;

import com.pramati.restel.core.managers.ContextManager;
import com.pramati.restel.core.model.GlobalContext;
import com.pramati.restel.core.model.TestContext;
import com.pramati.restel.core.model.functions.RestelFunction;
import com.pramati.restel.exception.RestelException;
import com.pramati.restel.utils.Constants;
import com.pramati.restel.utils.ObjectMapperUtils;
import com.pramati.restel.utils.Utils;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class RestelFunctionExecutor {

  private String executionName;

  private static final String INVALID_PATTERN = "INVALID_PATTERN_IN_EXECUTION";

  public RestelFunctionExecutor(String executionGroupName) {
    this.executionName = executionGroupName;
  }

  /**
   * Executes the {@link com.pramati.restel.core.model.functions.FunctionOps#REMOVE} operation of
   * Restel Function from the {@link TestContext} data and return the results.
   *
   * @param function {@link RestelFunction}
   * @return execute the remove operation from {@link
   *     com.pramati.restel.core.model.functions.FunctionOps} and return the results.
   */
  public Object execAddFunction(RestelFunction function) {
    if (function.getData().matches(".*" + Constants.VARIABLE_PATTERN + ".*")) {
      function.setData(Utils.removeBraces(function.getData()));
      Map<String, Object> payload;
      List<Object> elements = getElements(function.getArgs());
      if (function.getData().matches(REQUEST_PATTERN)) {
        payload = getPayload(function.getData(), REQUEST_PATTERN);
        function.setData(
            StringUtils.removeStartIgnoreCase(
                function.getData(), function.getData().split(REQUEST)[0]));
        return ((Map<String, Object>)
                RestelFunctionResolver.resolveAddOperation(payload, function.getData(), elements))
            .get(REQUEST);

      } else if (function.getData().matches(RESPONSE_PATTERN)) {
        payload = getPayload(function.getData(), RESPONSE_PATTERN);
        function.setData(
            StringUtils.removeStartIgnoreCase(
                function.getData(), function.getData().split(RESPONSE)[0]));
        return ((Map<String, Object>)
                RestelFunctionResolver.resolveAddOperation(payload, function.getData(), elements))
            .get(RESPONSE);

      } else {
        throw new RestelException(INVALID_PATTERN, function.getData(), executionName);
      }

    } else {
      return RestelFunctionResolver.resolveRemoveOperation(
          GlobalContext.getInstance().getAll(), function.getData(), function.getArgs());
    }
  }

  /**
   * @param args
   * @return
   */
  private List<Object> getElements(List<String> args) {
    return args.parallelStream()
        .map(
            a -> {
              if (a.matches(".*" + Constants.VARIABLE_PATTERN + ".*")) {
                ContextManager manager = new ContextManager();
                return manager.resolveVariableInNS(
                    GlobalContext.getInstance().getAll(), Utils.removeBraces(a));
              } else if (ObjectMapperUtils.isJSONValid(a)) {
                return Utils.isArray(a)
                    ? ObjectMapperUtils.convertToArray(a)
                    : ObjectMapperUtils.convertToMap(a);
              } else {
                return a;
              }
            })
        .collect(Collectors.toList());
  }

  /**
   * Executes the {@link com.pramati.restel.core.model.functions.FunctionOps#REMOVE} operation of
   * Restel Function from the {@link TestContext} data and return the results.
   *
   * @param function {@link RestelFunction}
   * @return execute the remove operation from {@link
   *     com.pramati.restel.core.model.functions.FunctionOps} and return the results.
   */
  public Object execRemoveFunction(RestelFunction function) {
    if (function.getData().matches(".*" + Constants.VARIABLE_PATTERN + ".*")) {
      function.setData(Utils.removeBraces(function.getData()));
      Map<String, Object> payload;
      if (function.getData().matches(REQUEST_PATTERN)) {
        payload = getPayload(function.getData(), REQUEST_PATTERN);
        function.setData(
            StringUtils.removeStartIgnoreCase(
                function.getData(), function.getData().split(REQUEST)[0]));
        return ((Map<String, Object>)
                RestelFunctionResolver.resolveRemoveOperation(
                    payload, function.getData(), function.getArgs()))
            .get(REQUEST);

      } else if (function.getData().matches(RESPONSE_PATTERN)) {
        payload = getPayload(function.getData(), RESPONSE_PATTERN);
        function.setData(
            StringUtils.removeStartIgnoreCase(
                function.getData(), function.getData().split(RESPONSE)[0]));
        return ((Map<String, Object>)
                RestelFunctionResolver.resolveRemoveOperation(
                    payload, function.getData(), function.getArgs()))
            .get(RESPONSE);

      } else {
        throw new RestelException(INVALID_PATTERN, function.getData(), executionName);
      }

    } else {
      return RestelFunctionResolver.resolveRemoveOperation(
          GlobalContext.getInstance().getAll(), function.getData(), function.getArgs());
    }
  }

  /**
   * Gets the request or response payload of the Test Suite execution based on the variable . Eg::
   * for variable:- get_user_exec.get_user.response.userGroup and regex of with response parser will
   * return the response payload of 'get_user' test_definition.
   *
   * @param variable pattern of the variable which tells about the test suite or test suite
   *     execution and test definition . Generally should of of format - Eg:
   *     get_user_exec.get_user.response.userGroup or get_user_exec.get_user.request.userGroup.
   * @param regex Regex pattern for parsing the request or response.
   * @return Parse the request or response and returns its payload.
   */
  private Map<String, Object> getPayload(String variable, String regex) {
    ContextManager manager = new ContextManager();
    Matcher m = Pattern.compile(regex).matcher(variable);
    if (m.find()) {
      Object data = manager.resolveVariableInNS(GlobalContext.getInstance().getAll(), m.group(1));
      if (data instanceof Map) {
        return (Map<String, Object>) data;
      }
    }
    throw new RestelException(INVALID_PATTERN, variable, executionName);
  }
}
