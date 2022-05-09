package com.techconative.restel.core.model.comparator;

import ro.skyah.comparator.JsonComparator;

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
