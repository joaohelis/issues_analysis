package br.ufrn.deliverydelay.repositories.code;


import java.sql.Date;
import java.util.List;

import br.ufrn.deliverydelay.model.Commit;
import br.ufrn.deliverydelay.repositories.Repository;

public interface CodeRepository extends Repository {
	
	List<Commit> findCommitsByTimeRangeAndDevelopers(Date startDate, Date endDate, List<String> developers, boolean collectChangedPaths, List<String> ignoredPaths, List<String> acceptedPaths);
	List<Commit> findCommitsByTimeRange(Date startDate, Date endDate, boolean collectChangedPaths, List<String> ignoredPaths, List<String> acceptedPaths);
	Commit findCommitByRevision(String revision,  boolean collectChangedPaths, List<String> ignoredPaths, List<String> acceptedPaths);
	
	String getFileContent(String path, String revision);
	List<String> getFileRevisions(String path, String startRevision, String endRevision);
	
}
