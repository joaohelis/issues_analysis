package br.ufrn.deliverydelay.exceptions;

public class MissingParameterException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9065554693183846705L;

	public MissingParameterException(String message) {
		super(message);
	}
	
}
