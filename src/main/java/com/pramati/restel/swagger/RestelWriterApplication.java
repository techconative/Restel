package com.pramati.restel.swagger;

import com.pramati.restel.exception.InvalidConfigException;
import com.pramati.restel.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class RestelWriterApplication {
    public static void main(String[] args) {
        if (args.length < 1) {
            throw new InvalidConfigException("MISSING_SWAGGER");
        }
        RestelSwaggerParser parser = new RestelSwaggerParser(args[0]);

        RestelExcelWriter writer = new RestelExcelWriter();
        //write base config and Test definitions
        writer.writeBaseConfig(parser.getBaseConfig());
        writer.writeTestDefinitions(parser.getTestDefinition());

        if (args.length > 1 && StringUtils.isNotEmpty(args[1])) {
            log.info("swager2excel writing to the file: " + args[1]);
            writer.writeToFile(args[1]);
        } else if (StringUtils.containsIgnoreCase(args[0], Constants.HTTP)) {
            writer.writeToFile(FilenameUtils.removeExtension(FilenameUtils.getBaseName(args[0])).concat(".xlsx"));
        } else {
            writer.writeToFile(FilenameUtils.removeExtension(args[0]).concat(".xlsx"));
        }

    }
}
