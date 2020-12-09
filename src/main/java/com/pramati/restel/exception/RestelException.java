package com.pramati.restel.exception;

/**
 * Represents an exception withing the Restel system.
 * 
 * @author kannanr
 *
 */
public class RestelException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7870634955321050307L;

	public RestelException(String message, Throwable rootCause) {
		super(message, rootCause);
	}
	
	public RestelException(String message) {
		super(message);
	}

}
