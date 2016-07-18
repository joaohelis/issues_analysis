package br.ufrn.deliverydelay.miner;

import br.ufrn.deliverydelay.exceptions.MissingParameterException;

public interface Miner {
	
	boolean setup() throws MissingParameterException; 
	void execute();
	
}
