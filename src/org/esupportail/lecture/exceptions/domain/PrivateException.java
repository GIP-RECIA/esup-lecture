package org.esupportail.lecture.exceptions.domain;

public class PrivateException extends Exception {
	public PrivateException(Exception e) {
		super(e);
	}
	
	public PrivateException(String message) {
		super(message);
	}
}