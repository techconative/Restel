package com.pramati.restel.testng;

import com.pramati.restel.core.AppConfig;
import com.pramati.restel.core.model.comparator.ResponseComparator;

/**
 * Factory that creates the matcher instances. <br>
 * Actual magic happens with the {@link AppConfig#factoryBean()}
 *
 * @author kannanr
 */
public interface MatcherFactory {

  /**
   * Returns the matcher with the given name.
   *
   * @param matcherName The name of the matcher to be returned.
   * @return The {@link ResponseComparator} with the given name.
   */
  public ResponseComparator getMatcher(String matcherName);
}
