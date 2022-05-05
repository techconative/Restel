package com.techconative.restel.core.model;

import static com.techconative.restel.core.parser.util.FunctionUtils.getFirstNotNull;
import static com.techconative.restel.core.parser.util.FunctionUtils.nullSafe;
import static com.techconative.restel.utils.Constants.ARRAY_PATTERN;
import static java.lang.System.getProperty;
import static java.lang.System.getenv;

import com.techconative.restel.utils.Constants;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract implementation of the context, can be used to represent the context of a test method, or
 * test execution or test suite.
 *
 * @author kannanr
 */
@Slf4j
public abstract class AbstractContext {

  private Map<String, Object> contextValues;

  private AbstractContext parentContext;

  public AbstractContext(AbstractContext parentContext) {
    this.parentContext = parentContext;
    contextValues = new HashMap<>();
  }

  /**
   * Resolves the value of the given key in the current context. If not available passes it to the
   * parent context for the resolution.
   *
   * @param key The key to be resolved.
   * @return The resolved value if available, otherwise null.
   */
  public Object resolveValue(String key) {
    return getFirstNotNull(
        () -> contextValues.get(key),
        () -> resolveVariableInNS(contextValues, key),
        () -> nullSafe(parentContext, (ct) -> ct.resolveValue(key), (Object) null),
        () -> getenv(key),
        () -> getProperty(key));
  }

  public void addValue(String name, Object value) {
    contextValues.put(name, value);
  }

  public void putAll(Map<String, Object> additional) {
    contextValues.putAll(additional);
  }

  /**
   * Resets the values in context and make it fresh.
   *
   * <p><b>Note:</b> The parent context would still stay the same, hence it's possible that you
   * could still resolve values from the parent context.
   */
  public void reset() {
    contextValues = new HashMap<>();
  }

  public Map<String, Object> getContextValues() {
    return this.contextValues;
  }

  private AbstractContext getParentContext() {
    return this.parentContext;
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
  private static Object resolveVariableInNS(Map<String, Object> context, String variableName) {
    // Don't have to tokenize everything. Just the first one is enough
    String[] tokens = variableName.split(Constants.NS_SEPARATOR_REGEX, 2);
    Object object;
    if (tokens[0].matches(".*" + ARRAY_PATTERN)) {
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

      String[] arrayToken = variable.split(ARRAY_PATTERN);
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
    Matcher m = Pattern.compile(ARRAY_PATTERN).matcher(variable);
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
