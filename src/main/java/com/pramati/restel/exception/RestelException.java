package com.pramati.restel.exception;

import com.pramati.restel.utils.MessageUtils;

/**
 * Represents an exception withing the Restel system.
 */
public class RestelException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 7870634955321050307L;

    public RestelException(Throwable rootCause, String message, Object... params) {
        super(MessageUtils.getString(message, params), rootCause);
    }

    public RestelException(String message, Object... params) {
        super(MessageUtils.getString(message, params));
    }

}
