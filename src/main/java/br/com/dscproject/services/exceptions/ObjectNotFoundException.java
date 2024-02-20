package br.com.dscproject.services.exceptions;

import java.io.Serial;

public class ObjectNotFoundException extends java.lang.RuntimeException {
	@Serial
	private static final long serialVersionUID = 1L;
	public ObjectNotFoundException(String msg) {
		super(msg);
	}
	public ObjectNotFoundException(String msg, Throwable causa) {
		super(msg, causa);
	}

}
