package br.com.dscproject.services.exceptions;

import java.io.Serial;

public class RuntimeException extends java.lang.RuntimeException {
	@Serial
	private static final long serialVersionUID = 1L;
	public RuntimeException(String msg) {
		super(msg);
	}
	public RuntimeException(String msg, Throwable causa) {
		super(msg, causa);

	}

}
