package br.com.dscproject.services.exceptions;

import java.io.Serial;

public class AuthorizationException extends java.lang.RuntimeException {
	@Serial
	private static final long serialVersionUID = 1L;
	public AuthorizationException(String msg) {
		super(msg);
	}
	public AuthorizationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
