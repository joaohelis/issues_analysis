package br.ufrn.deliverydelay;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import br.ufrn.deliverydelay.enuns.Team;
import br.ufrn.deliverydelay.miner.ChurnMiner;
import br.ufrn.deliverydelay.model.Commit;
import br.ufrn.deliverydelay.repositories.code.CodeRepository;
import br.ufrn.deliverydelay.repositories.code.GitRepository;
import br.ufrn.deliverydelay.repositories.code.SVNRepository;
import br.ufrn.deliverydelay.repositories.task.LocalStorage;
import br.ufrn.deliverydelay.util.DateInterval;
import br.ufrn.deliverydelay.util.DateIntervalUtil;
import br.ufrn.deliverydelay.util.PropertiesUtil;

public class App 
{
    public static void main( String[] args ){
    	
    	double startTime = System.currentTimeMillis();
    	
    	Date startDate = Date.valueOf("2005-01-01");
		Date endDate = Date.valueOf("2016-05-01");
		
		Properties localStorageProperties = PropertiesUtil.getProperties("config/localstorage.properties");
		LocalStorage localStorage = new LocalStorage(localStorageProperties.getProperty("url"), 
				localStorageProperties.getProperty("username"), 
				localStorageProperties.getProperty("password"));
		
		Properties gitProperties = PropertiesUtil.getProperties("config/git.properties");
		CodeRepository gitRepository = (CodeRepository) new GitRepository();
		((GitRepository)gitRepository).setSystem(Team.SIGAA);
		gitRepository.setURL(gitProperties.getProperty("url"));
		gitRepository.setUsername(gitProperties.getProperty("username"));
		gitRepository.setPassword(gitProperties.getProperty("password"));
		
		Properties svnProperties = PropertiesUtil.getProperties("config/svn.properties");
		CodeRepository svnRepository = (CodeRepository) new SVNRepository();
		((SVNRepository)svnRepository).setSystem(Team.SIGAA);
		svnRepository.setUsername(svnProperties.getProperty("username"));
		svnRepository.setPassword(svnProperties.getProperty("password"));
		svnRepository.setURL(svnProperties.getProperty("url"));
				
		ArrayList<String> ignoredPaths = new ArrayList<String>();
		
		ArrayList<String> acceptedPaths = new ArrayList<String>(Arrays.asList(new String[]{ "/trunk/SIPAC/", 
																							"/trunk/SIGRH/", 
																							"/trunk/SIGAA/"}));
		
		gitRepository.connect();	
		svnRepository.connect();		
		
		List<DateInterval> dateIntervals = DateIntervalUtil.genenateDateIntervalPerMonth(startDate, endDate);
		
		for(DateInterval dateInterval: dateIntervals){
			
			List<Commit> commits = gitRepository.findCommitsByTimeRange(dateInterval.getStartDate(), 
					dateInterval.getEndDate(), true, ignoredPaths, acceptedPaths);
			
			System.out.printf("Time to load commits: %.2f seconds\n", (System.currentTimeMillis() - startTime)/1000);
			
			ChurnMiner churnMiner = new ChurnMiner(gitRepository, dateInterval.getStartDate(), dateInterval.getEndDate(), 
					null, ignoredPaths, acceptedPaths);
			churnMiner.setSpecificCommitsToMining(commits);
			
			churnMiner.setup();
			churnMiner.execute();
			
			double finishTime = System.currentTimeMillis();
			System.out.printf("Time to process metrics: %.2f seconds\n", (finishTime - startTime)/1000);
			localStorage.saveCommits(commits);
			System.out.printf("Time to persist commit metrics: %.2f segundos\n", (System.currentTimeMillis() - finishTime)/1000);			
		}	
    }
}