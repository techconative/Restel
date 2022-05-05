package com.techconative.restel.core.model;

import static com.techconative.restel.core.parser.util.FunctionUtils.getFirstNotNull;
import static com.techconative.restel.core.parser.util.FunctionUtils.nullSafe;
import static java.lang.System.getProperty;
import static java.lang.System.getenv;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract implementation of the context, can be used to represent the context of a test method, or
 * test execution or test suite.
 *
 * @author kannanr
 */
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
    if (contextValues.get(key) == null) {
      return getFirstNotNull(
          () -> nullSafe(parentContext, (ct) -> ct.resolveValue(key), (Object) null),
          () -> getenv(key),
          () -> getProperty(key));
    } else {
      return contextValues.get(key);
    }
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

  @Deprecated
  /**
   * Avoid using this. If you need a value for a specific key use AbstractContext#resolveValue. You
   * might probably don't have to know everything inside the context.
   */
  public Map<String, Object> getAll() {
    Map<String, Object> objectMap = new HashMap<>();
    if (!Objects.isNull(this.parentContext)) {
      objectMap.putAll(this.parentContext.getAll());
    }
    objectMap.putAll(this.contextValues);
    return objectMap;
  }

  private AbstractContext getParentContext() {
    return this.parentContext;
  }
}
