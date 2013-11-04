package com.pushcoin.core.exceptions;

public class MATUnavailableException extends RecoverableException {

	private static final long serialVersionUID = -2387495217427693804L;

	public MATUnavailableException() {
		super("MAT not available");
	}
}