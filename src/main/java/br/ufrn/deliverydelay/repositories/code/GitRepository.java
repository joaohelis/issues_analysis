package br.ufrn.deliverydelay.repositories.code;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import br.ufrn.deliverydelay.enuns.RepositoryType;
import br.ufrn.deliverydelay.model.ChangedPath;
import br.ufrn.deliverydelay.model.Commit;
import br.ufrn.deliverydelay.repositories.code.gitapi.GITLogChange;
import br.ufrn.deliverydelay.repositories.code.gitapi.GITLogEntry;
import br.ufrn.deliverydelay.repositories.code.gitapi.GitAPIException;
import br.ufrn.deliverydelay.repositories.code.gitapi.GitHandleImpl;
import br.ufrn.deliverydelay.util.CodeRepositoryUtil;


public class GitRepository extends AbstractCodeRepository{
	
	private GitHandleImpl gitRepositoryHandle;
	
	public GitRepository(){
		this(null, null, null);
	}

	public GitRepository(String username, String password, String url) {
		super(username, password, url);
	}
	
	@Override
	protected boolean specificConnect() {
		
		System.out.println("> Connecting to GIT REPOSITORY: "+url);
		
		gitRepositoryHandle = new GitHandleImpl(url, username, password);
		
		if(!gitRepositoryHandle.wasClonedRepository()){
			try {
				System.out.print(">>> DELIVERYDELAY is cloning GIT REPOSITORY in local storage ... ");
				gitRepositoryHandle.cloneRepository();
				System.out.println("Done!");
				return true;
			} catch (GitAPIException e) {
				System.out.println("Failed!");
			}
		}else{
			try {
				System.out.print(">>> DELIVERYDELAY is updating GIT REPOSITORY in local storage ... ");
				gitRepositoryHandle.pull();
				System.out.println("Done!");
				return true;
			} catch (GitAPIException e) {
				System.out.println("Failed!");
			}
		}
		return false;
	}

	public List<Commit> findCommitsByTimeRangeAndDevelopers(Date startDate,
			Date endDate, List<String> developers, boolean collectChangedPaths,
			List<String> ignoredPaths, List<String> acceptedPaths) {
		
		System.out.println("\n>> DELIVERYDELAY is loking for commits in the informed date interval! "+startDate.toString()+" - "+endDate.toString()+" ... ");
		
		List<GITLogEntry> logs = gitRepositoryHandle.findCommitsByTimeRangeAndDevelopers(startDate, endDate, developers, collectChangedPaths, ignoredPaths);
		
		List<Commit> commits = new ArrayList<Commit>();
		
		for(GITLogEntry log: logs){
			
			List<GITLogChange> changedPaths = log.getChangedPaths();
			if(!changedPaths.isEmpty() && !acceptedPaths.isEmpty()){
				if(!isAcceptedChangedPath(changedPaths.iterator().next().getPath(), acceptedPaths))
					continue;
			}			
			
			Commit commit = this.GITLogEntryToCommit(log);
			commits.add(commit);
		}
		System.out.println("Done!\n>>> "+commits.size()+" commits were founded!");
		return commits;
	}

	public List<Commit> findCommitsByTimeRange(Date startDate, Date endDate,
			boolean collectChangedPaths, List<String> ignoredPaths, List<String> acceptedPaths) {
		return findCommitsByTimeRangeAndDevelopers(startDate, endDate, null, collectChangedPaths, ignoredPaths, acceptedPaths);
	}

	public Commit findCommitByRevision(String revision,
			boolean collectChangedPaths, List<String> ignoredPaths, List<String> acceptedPaths) {
		try {
			GITLogEntry log = this.gitRepositoryHandle.getCommitInformations(revision);
			return GITLogEntryToCommit(log);
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Commit GITLogEntryToCommit(GITLogEntry log){
		Commit commit = null;
		if(log != null){			
			commit = new Commit(log.getRevision(), log.getComment(), log.getCreatedAt(), log.getBranch(), log.getAuthorEmail(), RepositoryType.GIT);
			
			List<GITLogChange> logChanges = log.getChangedPaths();
			List<ChangedPath> changedPaths = new ArrayList<ChangedPath>();
			
			for(GITLogChange logChange: logChanges){
				ChangedPath changedPath = new ChangedPath(logChange.getPath(), logChange.getChangeType(), commit, logChange.getContent());
				changedPaths.add(changedPath);
			}
			commit.setChangedPaths(changedPaths);
			commit.setTaskId(CodeRepositoryUtil.getTaskNumberFromCommitComment(commit.getComment()));
			commit.setSystem(system);
		}
		return commit;
	}

	public String getFileContent(String path, String revision) {
		try {
			return this.gitRepositoryHandle.getChangeContent(revision, path);
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<String> getFileRevisions(String path, String startRevision,
			String endRevision) {
		try {
			List<String> fileRevisions = this.gitRepositoryHandle.getFileRevisions(path, startRevision, endRevision);
			return fileRevisions;
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		return new ArrayList<String>();
	}	
}