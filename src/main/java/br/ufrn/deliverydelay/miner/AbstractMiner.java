package br.ufrn.deliverydelay.miner;

import java.sql.Date;
import java.util.List;

import br.ufrn.deliverydelay.exceptions.MissingParameterException;
import br.ufrn.deliverydelay.repositories.code.CodeRepository;

public abstract class AbstractMiner implements Miner{
	
	protected Date startDate;
	protected Date endDate;
	protected Integer system;
	protected List<String> developers;
	protected CodeRepository codeRepository;
	protected List<String> ignoredPaths;
	protected List<String> acceptedPaths;
	
	public AbstractMiner(CodeRepository codeRepository, Date startDate, 
			Date endDate, List<String> developers, List<String> ignoredPaths, List<String> acceptedPaths) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.developers = developers;
		this.codeRepository = codeRepository;
		this.ignoredPaths = ignoredPaths;
		this.acceptedPaths = acceptedPaths;
	}

	public boolean setup() throws MissingParameterException {	
		
		if(codeRepository == null) {
			throw new MissingParameterException("Missing mandatory parameter: CodeRepository codeRepository");
		}
		if(startDate == null) {
			throw new MissingParameterException("Missing mandatory parameter: Date startDate");
		}
		if(endDate == null) {
			throw new MissingParameterException("Missing mandatory parameter: Date endDate");
		}		
		return this.setupMinerSpecific();
	}
	
	public abstract boolean setupMinerSpecific() throws MissingParameterException; 
	
	public abstract void execute();
}
