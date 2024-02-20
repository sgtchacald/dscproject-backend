package br.com.dscproject.services.exceptions;

import java.io.Serial;

public class MailSendException extends java.lang.RuntimeException {
	@Serial
	private static final long serialVersionUID = 1L;
	public MailSendException(String msg) {
		super(msg);
	}
	public MailSendException(String msg, Throwable causa) {
		super(msg, causa);
	}

}
