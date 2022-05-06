package com.techconative.restel.core;

import static java.lang.System.*;

import com.techconative.restel.core.parser.util.FunctionUtils;
import com.techconative.restel.exception.RestelException;
import com.techconative.restel.utils.Constants;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.StringUtils;

/**
 * Main class, entry point for the test execution application.
 *
 * @author kannanr
 */
public class RestelApplication {

  public static void main(String[] args) {
    ensureIfFilePathSet(args);

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

  private static void ensureIfFilePathSet(String[] args) {
    String filePath =
        FunctionUtils.getFirstNotNull(
            () -> getProperty(Constants.EXCEL_PATH_SYSTEM_PROPERTY_NAME),
            () -> getenv(Constants.EXCEL_PATH_ENVIRONMENT_VARIABLE_NAME),
            () -> getFileFromArgs(args));

    if (StringUtils.isEmpty(filePath)) {
      throw new RestelException("MISSING_FILE_PATH");
    } else {
      System.setProperty(Constants.EXCEL_PATH_SYSTEM_PROPERTY_NAME, filePath);
    }
  }

  private static String getFileFromArgs(String[] args) {
    if (args.length > 0) {
      return args[0];
    }
    return null;
  }
}
