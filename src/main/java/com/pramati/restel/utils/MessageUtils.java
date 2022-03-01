package com.pramati.restel.utils;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class MessageUtils {
    private static final ResourceBundle bundle;

    private MessageUtils() {
    }

    static {
        bundle = ResourceBundle.getBundle("message");
    }

    /**
     * This is a method that takes the param to substitute the placeholder
     **/
    public static String getString(String key, Object... params) {
        return MessageFormat.format(bundle.getString(key), params);
    }

    /**
     * Without a param, this will directly delegate to ResourceBundle#getString
     **/
    public static String getString(String key) {
        return bundle.getString(key);
    }

}
