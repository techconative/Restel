package com.pramati.restel.exception;

/**
 * Represents an invalid configuration for the execution.
 *
 * @author kannanr
 */
public class InvalidConfigException extends RestelException {

    /**
     *
     */
    private static final long serialVersionUID = 1493714961806892668L;

    public InvalidConfigException(String message, Throwable rootCause, Object... params) {
        super(rootCause, message, params);
    }

    public InvalidConfigException(String message, Object... params) {
        super(message, params);
    }
}