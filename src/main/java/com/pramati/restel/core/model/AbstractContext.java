package com.pramati.restel.core.model;

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
      if (parentContext != null) {
        return parentContext.resolveValue(key);
      }
    } else {
      return contextValues.get(key);
    }
    return null;
  }

  public void addValue(String name, Object value) {
    contextValues.put(name, value);
  }

  public void putAll(Map<String, Object> additional) {
    contextValues.putAll(additional);
  }

  public Map<String, Object> getContextValues() {
    return this.contextValues;
  }

  public Map<String, Object> getAll() {
    Map<String, Object> objectMap = new HashMap<>();
    if (!Objects.isNull(this.parentContext)) {
      objectMap.putAll(this.parentContext.getAll());
    }
    objectMap.putAll(this.contextValues);
    return objectMap;
  }

  public AbstractContext getParentContext() {
    return this.parentContext;
  }
}
