package br.ufrn.deliverydelay.repositories;

import br.ufrn.deliverydelay.exceptions.MissingParameterException;

public interface Repository {
	boolean connect() throws MissingParameterException;
	String getUsername();
	void setUsername(String username);
	String getPassword();
	void setPassword(String password);
	String getURL();
	void setURL(String url);
}
