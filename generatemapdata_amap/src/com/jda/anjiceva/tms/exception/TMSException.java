/**
 * 
 */
package com.jda.anjiceva.tms.exception;

/**
 * @author j1015278
 *
 */
public class TMSException extends Exception {

	private static final long serialVersionUID = -5195372343599690500L;

	/**
	 * identification of the bussiness exception, user could use the error ID get
	 * the globle message from properties
	 */
	private String errorId;

	public TMSException() {
		super();
	}

	public TMSException(String msg) {
		super(msg);
	}

	/**
	 * 
	 * @param errorId
	 *            identification of the bussiness exception, user could use the
	 *            error ID get the globle message from properties
	 * @param msg
	 *            error message
	 */
	public TMSException(String errorId, String msg) {
		super(msg);
		this.errorId = errorId;
	}

	public TMSException(Throwable e) {
		super(e);
	}

	public TMSException(String msg, Throwable e) {
		super(msg, e);
	}

	/**
	 * 
	 * @param errorId
	 *            identification of the bussiness exception, user could use the
	 *            error ID get the globle message from properties
	 * @param msg
	 *            error message
	 * @param e
	 *            cause by exception
	 */
	public TMSException(String errorId, String msg, Throwable e) {
		super(msg, e);
		this.errorId = errorId;
	}

	public String getErrorId() {
		return errorId;
	}

}
