package br.com.dscproject.services.exceptions;

import java.io.Serial;


public class DataIntegrityException extends java.lang.RuntimeException {
	@Serial
	private static final long serialVersionUID = 1L;
	public DataIntegrityException(String msg) {
		super(msg);
	}
	public DataIntegrityException(String msg, Throwable cause) {
		super(msg, cause);

	}

}
