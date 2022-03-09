package com.techconative.restel.swagger;

import com.techconative.restel.core.parser.dto.BaseConfig;
import com.techconative.restel.core.parser.dto.TestDefinitions;
import com.techconative.restel.exception.InvalidConfigException;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * Restel Swagger Parser to generate Data Models(Base Config,Test Definitions, etc) of Restel Excel sheet.
 */
@Slf4j
public class RestelSwaggerParser {

    private RestelOpenApiSpec3Parser openApi;

    private RestelOpenApiSpec2Parser swagger;

    public RestelSwaggerParser(String swaggerFilePath) {
        init(swaggerFilePath);
    }

    private void init(String swaggerFilePath) {
        Object parser = fetchParser(swaggerFilePath);
        if (parser instanceof OpenAPI) {
            openApi = new RestelOpenApiSpec3Parser((OpenAPI) parser);
        } else if (parser instanceof Swagger) {
            swagger = new RestelOpenApiSpec2Parser((Swagger) parser);
        }
    }

    public BaseConfig getBaseConfig() {
        BaseConfig config = null;
        if (!Objects.isNull(openApi)) {
            config = openApi.createBaseConfig();
        } else if (!Objects.isNull(swagger)) {
            config = swagger.createBaseConfig();
        }
        return config;
    }

    public List<TestDefinitions> getTestDefinition() {
        List<TestDefinitions> testDefinitions = null;

        if (!Objects.isNull(openApi)) {
            testDefinitions = openApi.createTestDefinitions();
        } else if (!Objects.isNull(swagger)) {
            testDefinitions = swagger.createTestDefinitions();
        }
        return testDefinitions;
    }

    /**
     * Return swagger parser object which is either {@link OpenAPI} or {@link Swagger}.
     *
     * @param swaggerFilepath Swagger file path.
     * @return Return either {@link OpenAPI} or {@link Swagger} .
     */
    private Object fetchParser(String swaggerFilepath) {
        if (StringUtils.isEmpty(swaggerFilepath)) {
            throw new InvalidConfigException("INVALID_SWAGGER", swaggerFilepath);
        }

        Object parser = new SwaggerParser().read(swaggerFilepath);
        if (Objects.isNull(parser)) {
            parser = new OpenAPIV3Parser().read(swaggerFilepath);
            if (Objects.isNull(parser)) {
                throw new InvalidConfigException("CHECK_SWAGGER_FORMAT", swaggerFilepath);
            }
        }
        return parser;
    }

}
