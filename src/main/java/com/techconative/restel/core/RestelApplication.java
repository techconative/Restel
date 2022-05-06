package com.techconative.restel.core;

import static java.lang.System.*;

import com.techconative.restel.core.parser.util.FunctionUtils;
import com.techconative.restel.exception.RestelException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.StringUtils;

/**
 * Main class, entry point for the test execution application.
 *
 * @author kannanr
 */
@Slf4j
public class RestelApplication {

  public static void main(String[] args) {
    validateExcelFile(args);

    RestelApplication app = new RestelApplication();
    app.executeTests();
  }

  public boolean executeTests() {
    AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);

    RestelRunner bean = ctx.getBean(RestelRunner.class);
    boolean isSuccess = bean.run();

    ctx.close();
    ctx.stop();

    return isSuccess;
  }

  public static void validateExcelFile(String[] args) {
    String restelAppFile;
    if (System.getProperty("app.excelFile") == null) {
      //      restelAppFile =
      //          getFirstNotNull(() -> args[0], () -> getenv("RESTEL_APP_FILE"));

      if (args.length > 0) {
        restelAppFile = args[0];
      } else {
        restelAppFile = getenv("RESTEL_APP_FILE");
      }

      if (restelAppFile == null) {
        log.error("Excel file not found.");
        log.error("Usage: java -jar restel-core.jar <xlsx file>");
        exit(1);
      }
      System.setProperty("app.excelFile", restelAppFile);
    }
  }

  private void ensureIfFilePathSet(String[] args) {
    String filePath =
        FunctionUtils.getFirstNotNull(
            () -> getenv("app.excelFile"),
            () -> getenv("RESTEL_APP_FILE"),
            () -> getFileFromArgs(args));

    if (StringUtils.isEmpty(filePath)) {
      throw new RestelException("Spread sheet file required for test execution is not provided");
    } else {
      System.setProperty("app.excelFile", filePath);
    }

  }

  private String getFileFromArgs(String[] args) {
    if (args.length > 0) {
      return args[0];
    }
    return null;
  }
}
