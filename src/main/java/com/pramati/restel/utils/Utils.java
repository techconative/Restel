package com.pramati.restel.utils;

import com.pramati.restel.core.http.RESTResponse;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import static java.util.stream.Collectors.joining;

/**
 * Contains the utils related restel
 */
public class Utils {

    private Utils() {
    }

    /**
     * Searches the given pattern in the given src string and applies the txr to the matches. <br>
     * <b>Note:</b> Ex, To convert snake case to camel case make the call, <br>
     * <i>findAndApply("some_snake_case", "_[a-z]",(x) -> x.replace("_","").toUpperCase())</i>
     *
     * @param src     The string to be converted
     * @param pattern the pattern for which the transformers to be applied.
     * @param txr     The transformers for the mathed patterns.
     * @return The result after applying the transformation.
     */
    public static String findAndApplyOnString(
            String src, String pattern, Function<String, Object> txr) {
        if (src == null) {
            return null;
        }
        Matcher m = Pattern.compile(pattern).matcher(src);

        StringBuilder sb = new StringBuilder();
        int last = 0;

        while (m.find()) {
            sb.append(src.substring(last, m.start()));
            if (txr.apply(m.group(0)) != null) {
                sb.append(txr.apply(m.group(0)));
            }
            last = m.end();
        }
        sb.append(src.substring(last));
        return sb.toString();
    }

    public static Object findAndApplyOnObject(
            String src, String pattern, Function<String, Object> txr) {
        if (src == null) {
            return null;
        }
        Matcher m = Pattern.compile(pattern).matcher(src);

        StringBuilder sb = new StringBuilder();
        int last = 0;

        while (m.find()) {
            sb.append(src.substring(last, m.start()));
            Object res = txr.apply(m.group(0));
            if (!(res instanceof String) && !Objects.isNull(res)) return res;
            if (res != null) {
                sb.append(res);
            }
            last = m.end();
        }
        sb.append(src.substring(last));
        return sb.toString();
    }

    /**
     * Gets the string representation of the given object. Gets the empty string if the object is
     * null.
     *
     * @param obj The object whose str rep to be returned.
     * @return toString() value of the object if the object is not null. empty string otherwise.
     */
    public static String emptyForNull(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    public static String stringOrNull(Object obj) {
        return obj == null ? null : obj.toString();
    }

    /**
     * if response header is empty return true or else check if the response header has content-type -
     * application/json.
     *
     * @param restResponse
     * @return
     */
    public static String getMediaType(RESTResponse restResponse) {
        if (MapUtils.isEmpty(restResponse.getHeaders())) {
            return MediaType.APPLICATION_JSON;
        }
        if (restResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE.toLowerCase()) == null) {
            return MediaType.APPLICATION_JSON;
        }
        try {
            String content =
                    (String) restResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE.toLowerCase());
            if (StringUtils.containsIgnoreCase(content, MediaType.APPLICATION_JSON)) {
                return MediaType.APPLICATION_JSON;
            } else if (StringUtils.containsIgnoreCase(content, MediaType.TEXT_PLAIN)) {
                return MediaType.TEXT_PLAIN;
            } else {
                return MediaType.APPLICATION_JSON;
            }
        } catch (Exception ex) {
            return MediaType.APPLICATION_JSON;
        }
    }

    public static String mapToString(Map<String, String> map) {
        return map.keySet().stream()
                .map(key -> "\"".concat(key).concat("\"") + ":" + "\"".concat(map.get(key)).concat("\""))
                .collect(joining(", ", "{", "}"));
    }

    /**
     * remove the braces inside the text. Eg: data:- ${Tom.Response.Value} will return
     * Tom.Response.Value .
     *
     * @param data should be of format: ${Tom.Response.Value}
     * @return remove the braces and return the text inside.
     */
    public static String removeBraces(String data) {
        if (data.matches(".*" + Constants.VARIABLE_PATTERN + ".*")) {
            return data.replaceAll("^\\$\\{", "").replaceAll("\\}$", "");
        } else return data;
    }

    public static boolean isArray(String value) {
        return !StringUtils.isBlank(value) && (value.startsWith("[") && value.endsWith("]"));
    }

    public static String toCsv(Collection<?> coll) {
        return coll.stream().map(Object::toString).collect(joining(","));
    }
}
