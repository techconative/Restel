package com.techconative.restel.core.parser.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.techconative.restel.utils.ObjectMapperUtils;
import com.techconative.restel.utils.Utils;
import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

/** Contains the list of functions to be used while parsing the excel. */
public class Functions {

  private Functions() {}

  /** Apache POI function fetches string value from cell */
  public static final Function<Cell, String> STRING_FUNCTION = Cell::getStringCellValue;

  /** Apache POI function fetches numeric value from cell */
  public static final Function<Cell, Double> DOUBLE_FUNCTION = Cell::getNumericCellValue;

  /** Input: 'snake_case' string. Output: 'snakeCase' camel case string */
  public static final UnaryOperator<String> TO_CAMEL_CASE =
      y -> Utils.findAndApplyOnString(y, "_[a-zA-Z]", x -> x.replace("_", "").toUpperCase());

  /** Input: 'snake_case' string. Output: 'SnakeCase' pascal case string */
  public static final UnaryOperator<String> TO_PASCAL_CASE =
      x -> {
        String s = TO_CAMEL_CASE.apply(x);
        return s.substring(0, 1).toUpperCase() + s.substring(1);
      };

  /** Input: 'snake_case' string. Output: 'setSnakeCase' string */
  public static final UnaryOperator<String> SETTER = x -> "set" + TO_PASCAL_CASE.apply(x);

  /** Input json string, output Map object */
  public static final Function<Cell, Map<String, Object>> TO_MAP =
      x ->
          ObjectMapperUtils.getMapper()
              .convertValue(x.getStringCellValue(), new TypeReference<Map<String, Object>>() {});

  /** Input csv, output list of strings. */
  public static final Function<Cell, List<String>> TO_STRING_LIST =
      x -> {
        if (x.getCellType() == CellType.NUMERIC) {
          return Collections.singletonList(String.valueOf((int) x.getNumericCellValue()));
        }
        return Arrays.asList(
            Utils.emptyForNull(x.getStringCellValue())
                .replaceAll("(^\\s+)|(\\s+$)", "")
                .split(" *, *"));
      };

  /** Input csv, output list of strings. */
  public static final Function<Cell, List<Integer>> TO_INT_LIST =
      x ->
          Arrays.asList(Utils.emptyForNull(x.getStringCellValue()).split(",")).stream()
              .map(a -> Integer.valueOf(a.replace(" ", "")))
              .collect(Collectors.toList());

  /** Input csv, output set of strings. */
  public static final Function<Cell, Set<String>> TO_SET =
      x ->
          new HashSet<>(
              Arrays.asList(
                  Utils.emptyForNull(x.getStringCellValue())
                      .replaceAll("(^\\s+)|(\\s+$)", "")
                      .split(" *, *")));

  /** Input csv, output Boolean. */
  public static final Function<Cell, Boolean> TO_BOOLEAN =
      x -> x.getCellType() == CellType.BOOLEAN ? x.getBooleanCellValue() : Boolean.TRUE;
}
