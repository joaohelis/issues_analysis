package br.ufrn.deliverydelay.exceptions;

public class NonUniqueCommitByRevisionException extends RuntimeException {
	
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NonUniqueCommitByRevisionException(String message){
		super(message);
	}

}
