package br.ufrn.deliverydelay.connectors;

import br.ufrn.deliverydelay.enuns.ConnectorType;

public abstract class Connector<T> 
{
	protected String url;
	protected String user;
	protected String password;
	protected ConnectorType type;
	protected T encapsulation;
	
	public T getEncapsulation() 
	{
		return encapsulation;
	}
	
	public void setEncapsulation(T connector) 
	{
		this.encapsulation = connector;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}