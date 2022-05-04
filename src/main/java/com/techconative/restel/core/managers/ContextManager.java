package com.techconative.restel.core.managers;

import com.techconative.restel.core.model.AbstractContext;
import com.techconative.restel.core.model.TestContext;
import com.techconative.restel.utils.Constants;
import com.techconative.restel.utils.Utils;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static com.techconative.restel.core.parser.util.FunctionUtils.nullSafe;

/**
 * Manages the global context and variables, used for resolving the variables.
 *
 * @author kannanr
 */
@Service
@Slf4j
public class ContextManager {

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
  public static Object replaceContextVariables(TestContext additionalContext, Object object) {
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
      if (isArray(v)) {
        return new ArrayList<>(Arrays.asList(v.substring(1, v.length() - 1).split(",")));
      }
    }
    return value;
  }

  private static  boolean isArray(String value) {
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
      TestContext additionalContext, Map<String, Object> map) {

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
      TestContext additionalContext, T coll) {
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
  private static Object resolveContextExpr(TestContext additionalContext, String expr) {
    String varName = Utils.removeBraces(expr);

    // For nested variables, there can be additional characters before and
    // after it

    // TODO: This will fail when there are "}" other than the expressions
    // we use, for ex, a json string.
    if (varName.matches(".*" + Constants.VARIABLE_PATTERN + ".*")) {
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
    if (!(variable instanceof String)) {
      return replaceContextVariables(additionalContext, variable);
    } else {
      return resolveVariableResultWithExp(additionalContext, Utils.stringOrNull(variable));
    }
  }

  /**
   * if variable value is expression then resolve the expression and get it's appropriate value.
   *
   * @param additionalContext Additional context to be included in resolution
   * @param variable The name of the variable
   * @return the value return for the variable if it's an expression else return the variable .
   */
  private static Object resolveVariableResultWithExp(
      TestContext additionalContext, String variable) {
    if (variable == null) {
      return variable;
    }
    return variable.matches(".*" + Constants.VARIABLE_PATTERN + ".*")
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
  private static Object resolveVariableValue(TestContext additionalContext, String variableName) {
    return resolveVariableInNS(
        nullSafe(additionalContext, AbstractContext::getAll, new HashMap<>()), variableName);
  }

  /**
   * Resolve variable in the namespace represented by the given map. If the variable name is
   * namespaced by the {@link Constants#DOT} character. In which case each string that is followed
   * by a {@link Constants#DOT} is expected to contain a map that stores nested variables.
   *
   * @param context Context in which the resolution to be done.
   * @param variableName The variable name to be looked at.
   * @return The value represented the variablename
   */
  public static Object resolveVariableInNS(Map<String, Object> context, String variableName) {
    // Don't have to tokenize everything. Just the first one is enough
    String[] tokens = variableName.split(Constants.NS_SEPARATOR_REGEX, 2);
    Object object;
    if (tokens[0].matches(".*" + arrayPattern)) {
      object = resolveArray(tokens[0], context);
    } else {
      object = context.get(tokens[0]);
    }
    if (tokens.length == 1) {
      return object;
    } else {
      if (object instanceof Map) {
        return resolveVariableInNS((Map) object, tokens[1]);
      } else if (object instanceof List) {
        return resolveVariableArrayInNS((List) object, tokens[1]);
      }
      log.warn("The path " + variableName + " is not available in the context. Returning null");
      return null;
    }
  }

  /**
   * resolve the variable by finding the indexed value of the array. Eg: for context:-
   * {userGroup:[{name: Adam},{name:Sam}]} , the variable userGroup[0] will return the object:-
   * {name:Adam}
   *
   * @param variable The variable name to be looked at. Usually the variables will be of format eg:
   *     userGroup[0]
   * @param context Context in which the resolution to be done.
   * @return The value represented the variable
   */
  private static Object resolveArray(String variable, Map<String, Object> context) {
    try {

      String[] arrayToken = variable.split(arrayPattern);
      Object object = context.get(arrayToken[0]);

      for (List<Integer> fetchArrayIndex : fetchArrayIndexes(variable)) {
        if (fetchArrayIndex.size() > 1) {
          List<Object> indexes = new ArrayList<>();
          Object finalObject = object;
          fetchArrayIndex.forEach(i -> indexes.add(((List<?>) finalObject).get(i)));
          object = indexes;
          break;
        } else {
          object = ((List<?>) object).get(fetchArrayIndex.get(0));
        }
      }

      return object;
    } catch (Exception e) {
      log.warn("Failed to resolve arrayed variable: " + variable);
    }
    return null;
  }

  private static List<List<Integer>> fetchArrayIndexes(String variable) {
    Matcher m = Pattern.compile(arrayPattern).matcher(variable);
    List<List<Integer>> indexes = new ArrayList<>();
    while (m.find()) {
      indexes.add(
          new ArrayList<>(
              Arrays.stream(m.group(1).split(Constants.COMMA))
                  .map(Integer::parseInt)
                  .collect(Collectors.toList())));
    }
    return indexes;
  }

  /**
   * Resolve variable in the namespace represented by the given List. If the variable name is
   * namespaced by the {@link Constants#DOT} character. In which case each string that is followed
   * by a {@link Constants#DOT} is expected to contain a List that stores nested variables.
   *
   * @param context Context in which the resolution to be done.
   * @param variableName The variable name to be looked at.
   * @return The value represented the variablename
   */
  private static Object resolveVariableArrayInNS(List<Object> context, String variableName) {
    for (Object element : context) {
      Object val = null;
      if (element instanceof Map) {
        val = resolveVariableInNS((Map) element, variableName);
      } else if (element instanceof List) {
        val = resolveVariableArrayInNS((List) element, variableName);
      }
      if (!Objects.isNull(val)) {
        return val;
      }
    }
    log.warn("The path " + variableName + "is not available in the context. Returning null");

    return null;
  }
}
