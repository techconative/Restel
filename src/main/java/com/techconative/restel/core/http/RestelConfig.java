package com.techconative.restel.core.http;

import lombok.Data;

/**
 * Global config related to the Restel app.
 *
 * @author kannanr
 */
@Data
public class RestelConfig {

  private String baseUrl;

  private String apiToken;
}
