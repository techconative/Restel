package com.techconative.restel.core.utils;

import com.techconative.restel.core.model.AbstractContext;
import com.techconative.restel.utils.Constants;
import com.techconative.restel.utils.Utils;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Manages the global context and variables, used for resolving the variables.
 *
 * @author kannanr
 */
@Slf4j
public class ContextUtils {

  private static String arrayPattern = "\\[([\\d+,?]+)+\\]";

  // TODO: With the perspective of seeing the problem of variable resolution
  // as search and replace, we'll be able to do good for just variable
  // resolution.
  // If we have to do with even simple logic, around the variables that we
  // manage,
  // this approach will not good enough. We might have to go to a DSL approach
  // to
  // provide
  // some amount of programmability. For ex, build java equivalent of
  // jexpr(http://sriku.org/blog/2012/04/14/creating-dsls-in-javascript-using-j-expressions/)
  // or nisp(https://github.com/ysmood/nisp) or something similar

  /**
   * Replaces the context variables for the given object. <br>
   * <b>NOTE:</b> If you are passing a stringified version of a JSON, this won't work because of the
   * mechanism used to parse is not intelligent enough to differentiate between the "}" character in
   * json and "}" character in the expr. In such cases, please pass convert the json as Map and pass
   * it
   *
   * @param additionalContext Additional context to be considered along with the existing context
   *     map.
   * @param object The object in which the context variables to be replaced.
   * @return The string value after replacing the context variables.
   */
  public static Object replaceContextVariables(AbstractContext additionalContext, Object object) {
    if (additionalContext == null) {
      // When we don't have any context, we don't try to resolve variables.
      return object;
    }
    if (object instanceof Map) {
      return replaceContextVariables(additionalContext, (Map) object);
    } else if (object instanceof Collection) {
      return replaceContextVariables(additionalContext, (Collection) object);
    }
    Object value =
        Utils.findAndApplyOnObject(
            Utils.stringOrNull(object),
            Constants.VARIABLE_PATTERN,
            s -> resolveContextExpr(additionalContext, s));
    if (value instanceof String) {
      String v = (String) value;
      // TODO: Won't we have other datatypes (say a valid json) coming as string here?
      if (isArray(v)) {
        return new ArrayList<>(Arrays.asList(v.substring(1, v.length() - 1).split(",")));
      }
    }
    return value;
  }

  private static boolean isArray(String value) {
    return !StringUtils.isBlank(value) && (value.startsWith("[") && value.endsWith("]"));
  }

  /**
   * Replaces the keys and values on the given map with the respective context variables.
   *
   * @param additionalContext Additional context to be included in resolution.
   * @param map The object in which the context variables to be replaced.
   * @return The string value after replacing the context variables.
   */
  public static Map<String, Object> replaceContextVariables(
      AbstractContext additionalContext, Map<String, Object> map) {

    return map.keySet().stream()
        .collect(
            HashMap::new,
            (m, v) ->
                m.put(
                    replaceContextVariables(additionalContext, v).toString(),
                    replaceContextVariables(additionalContext, map.get(v))),
            HashMap::putAll);
  }

  /**
   * Replaces the entries in the given collection with the respective context variables.
   *
   * @param additionalContext Additional context to be included in resolution.
   * @param coll The collection in which the context variables to be replaced.
   * @return The collection after all the variables are recursively replaced.
   */
  public static <T extends Collection<Object>> List<Object> replaceContextVariables(
      AbstractContext additionalContext, T coll) {
    return coll.stream()
        .map(e -> replaceContextVariables(additionalContext, e))
        .collect(Collectors.toList());
  }

  /**
   * Resolve context variable recursively.
   *
   * @param additionalContext Additional context to be included in resolution.
   * @param expr The expression containing the variables.
   * @return The value in the context for the given variable.
   */
  private static Object resolveContextExpr(AbstractContext additionalContext, String expr) {
    String varName = Utils.removeBraces(expr);

    // For nested variables, there can be additional characters before and
    // after it

    // TODO: This will fail when there are "}" other than the expressions
    // we use, for ex, a json string.
    if (containsContextVariable(varName)) {
      // The string contains nested variable. Resolve it recursively
      return resolveContextExpr(
          additionalContext,
          Utils.stringOrNull(
              Utils.findAndApplyOnString(
                  varName,
                  Constants.VARIABLE_PATTERN,
                  s -> resolveContextExpr(additionalContext, s))));
    }
    Object variable = resolveVariableValue(additionalContext, varName);
    //    Object variable = additionalContext.resolveValue(varName);
    if (!(variable instanceof String)) {
      // If not a string, then we have to look out for variables within the returned data-structure.
      return replaceContextVariables(additionalContext, variable);
    } else {
      return resolveVariableResultWithExp(additionalContext, Utils.stringOrNull(variable));
    }
  }

  private static boolean containsContextVariable(String varName) {
    return varName.matches(".*" + Constants.VARIABLE_PATTERN + ".*");
  }

  /**
   * if variable value is expression then resolve the expression and get it's appropriate value.
   *
   * @param additionalContext Additional context to be included in resolution
   * @param variable The name of the variable
   * @return the value return for the variable if it's an expression else return the variable .
   */
  private static Object resolveVariableResultWithExp(
      AbstractContext additionalContext, String variable) {
    if (variable == null) {
      return null;
    }
    return containsContextVariable(variable)
        ? resolveContextExpr(additionalContext, variable)
        : variable;
  }

  /**
   * Resolve the value of a variable from the context map.
   *
   * @param additionalContext Additional context to be included in resolution.
   * @param variableName The name of the variable.
   * @return Value referred from the variable.
   */
  private static Object resolveVariableValue(
      AbstractContext additionalContext, String variableName) {
    return additionalContext.resolveValue(variableName);
  }
}
