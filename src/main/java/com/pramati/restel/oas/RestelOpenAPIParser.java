package com.pramati.restel.oas;

import com.pramati.restel.core.parser.dto.BaseConfig;
import com.pramati.restel.core.parser.dto.TestDefinitions;
import com.pramati.restel.exception.InvalidConfigException;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Restel OpenAPI Parser to generate Data Models(Base Config,Test Definitions, etc) of Restel Excel
 * sheet.
 */
@Slf4j
public class RestelOpenAPIParser {

  private RestelOpenApiSpec3Parser spec3Parser;

  private RestelOpenApiSpec2Parser spec2Parser;

  public RestelOpenAPIParser(String oacFilePath) {
    init(oacFilePath);
  }

  private void init(String oacFilePath) {
    Object parser = fetchParser(oacFilePath);
    if (parser instanceof OpenAPI) {
      spec3Parser = new RestelOpenApiSpec3Parser((OpenAPI) parser);
    } else if (parser instanceof Swagger) {
      spec2Parser = new RestelOpenApiSpec2Parser((Swagger) parser);
    }
  }

  public BaseConfig getBaseConfig() {
    BaseConfig config = null;
    if (!Objects.isNull(spec3Parser)) {
      config = spec3Parser.createBaseConfig();
    } else if (!Objects.isNull(spec2Parser)) {
      config = spec2Parser.createBaseConfig();
    }
    return config;
  }

  public List<TestDefinitions> getTestDefinition() {
    List<TestDefinitions> testDefinitions = null;

    if (!Objects.isNull(spec3Parser)) {
      testDefinitions = spec3Parser.createTestDefinitions();
    } else if (!Objects.isNull(spec2Parser)) {
      testDefinitions = spec2Parser.createTestDefinitions();
    }
    return testDefinitions;
  }

  /**
   * Return open api file parser object which is either {@link OpenAPI} or {@link Swagger}.
   *
   * @param openAPIFilepath open api file path.
   * @return Return either {@link OpenAPI} or {@link Swagger} .
   */
  private Object fetchParser(String openAPIFilepath) {
    if (StringUtils.isEmpty(openAPIFilepath)) {
      throw new InvalidConfigException("INVALID_OAS_FILE", openAPIFilepath);
    }

    Object parser = new SwaggerParser().read(openAPIFilepath);
    if (Objects.isNull(parser)) {
      parser = new OpenAPIV3Parser().read(openAPIFilepath);
      if (Objects.isNull(parser)) {
        throw new InvalidConfigException("CHECK_OAS_FILE_FORMAT", openAPIFilepath);
      }
    }
    return parser;
  }
}
