package com.techconative.restel.core.model;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

/** BaseConfig type dto for sheet base_config */
@Data
@Builder
public class BaseConfiguration {

  private String baseUrl;

  private Map<String, Object> defaultHeader;
}
