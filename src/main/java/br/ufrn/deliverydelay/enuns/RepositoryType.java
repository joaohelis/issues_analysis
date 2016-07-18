package br.ufrn.deliverydelay.enuns;

public enum RepositoryType{
	
	GIT(1), SVN(2);
	
	private final int type;
	
	RepositoryType(int type) { this.type = type; }
    public int getValue() { return type; }
}
