package com.techconative.restel.core.parser.util;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;
import java.util.function.Supplier;

public class FunctionUtils {
  public static <T> T getFirstNotNull(Supplier<T>... fns) {
    for (Supplier<T> fn : fns) {
      T val = fn.get();
      if (val != null) {
        return val;
      }
    }
    return null;
  }

  /**
   * Does a null-check on the given object and invokes the given function on the object only if it
   * is non-null
   *
   * <p>Can typically be used for accessing attributes after a null check. For ex, obj == null?
   * null: obj.getAttribute() can be replaced with nullSafe(obj, obj::getAttribute)
   *
   * @param obj The object on which the function to be invoked.
   * @param functionToInvoke The function to be invoked in case of non-null value.
   * @param <S> The type of the object.
   * @param <T> The expected return type
   * @return The return value when the given function when the obj is non-null, null otherwise.
   */
  public static <S, T> T nullSafe(S obj, Function<S, T> functionToInvoke) {
    return nullSafe(obj, functionToInvoke, (Supplier<T>) () -> null);
  }

  /**
   * Does a null-check on the given object and invokes the given function on the object only if it
   * is non-null
   *
   * <p>Can typically be used for accessing attributes after a null check. For ex, obj == null?
   * someMethod(): obj.getAttribute() can be replaced with nullSafe(obj, obj::getAttribute, ()->
   * someMethod())
   *
   * @param obj The object on which the function to be invoked.
   * @param functionToInvoke The function to be invoked in case of non-null value.
   * @param <S> The type of the object.
   * @param <T> The expected return type
   * @return The return value when the given function when the obj is non-null, The value from the
   *     supplier otherwise.
   */
  public static <S, T> T nullSafe(
      S obj, Function<S, T> functionToInvoke, Supplier<T> defaultValueSupplier) {

    if (obj == null) {
      return requireNonNull(defaultValueSupplier).get();
    }
    return requireNonNull(functionToInvoke).apply(obj);
  }

  /**
   * Does a null-check on the given object and invokes the given function on the object only if it
   * is non-null
   *
   * <p>Can typically be used for accessing attributes after a null check. For ex, obj == ""?
   * someMethod(): obj.getAttribute() can be replaced with nullSafe(obj, obj::getAttribute, "")
   *
   * @param obj The object on which the function to be invoked.
   * @param functionToInvoke The function to be invoked in case of non-null value.
   * @param <S> The type of the object.
   * @param <T> The expected return type
   * @return The return value when the given function when the obj is non-null, The default value
   *     provided otherwise.
   */
  public static <S, T> T nullSafe(S obj, Function<S, T> functionToInvoke, T defaultValue) {
    return nullSafe(obj, functionToInvoke, (Supplier<T>) () -> defaultValue);
  }
}
