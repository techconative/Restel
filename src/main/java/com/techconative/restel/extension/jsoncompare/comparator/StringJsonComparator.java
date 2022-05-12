package com.techconative.restel.extension.jsoncompare.comparator;

import ro.skyah.comparator.JsonComparator;

/**
 * String equals check. To override regex based pattern matching used in DefaultJsonComparator
 * implementation.
 */
public class StringJsonComparator implements JsonComparator {
  @Override
  public boolean compareValues(Object expected, Object actual) {
    return expected.equals(actual);
  }

  @Override
  public boolean compareFields(String expected, String actual) {
    return expected.equals(actual);
  }
}
