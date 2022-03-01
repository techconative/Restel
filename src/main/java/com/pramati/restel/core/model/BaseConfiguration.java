package com.pramati.restel.core.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * BaseConfig type dto for sheet base_config
 */
@Data
@Builder
public class BaseConfiguration {

  private String baseUrl;

  private Map<String,Object> defaultHeader;

}
