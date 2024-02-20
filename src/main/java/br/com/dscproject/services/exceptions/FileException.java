package br.com.dscproject.services.exceptions;

import java.io.Serial;

public class FileException extends java.lang.RuntimeException {
	@Serial
	private static final long serialVersionUID = 1L;
	public FileException(String msg) {
		super(msg);
	}
	public FileException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
