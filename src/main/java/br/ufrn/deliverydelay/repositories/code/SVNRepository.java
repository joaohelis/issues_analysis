package br.ufrn.deliverydelay.repositories.code;

import java.io.ByteArrayOutputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNFileRevision;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc2.SvnLog;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnRevisionRange;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import br.ufrn.deliverydelay.enuns.RepositoryType;
import br.ufrn.deliverydelay.enuns.Team;
import br.ufrn.deliverydelay.exceptions.NonUniqueCommitByRevisionException;
import br.ufrn.deliverydelay.model.ChangedPath;
import br.ufrn.deliverydelay.model.Commit;
import br.ufrn.deliverydelay.util.CodeRepositoryUtil;

public class SVNRepository extends AbstractCodeRepository implements CodeRepository {

	private org.tmatesoft.svn.core.io.SVNRepository repository;

	public SVNRepository(String username, String password, String url) {
		super(username, password, url);
	}

	public SVNRepository() {
		this(null, null, null);
	}

	@Override
	protected boolean specificConnect() {
		DAVRepositoryFactory.setup();
		try {
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
			repository.setAuthenticationManager(authManager);			
			return true;
		} catch (SVNException e) {
			e.printStackTrace();
		}
		return false;
	}

	public List<Commit> findCommitsByTimeRange(Date startDate, Date endDate, boolean collectChangedPaths,
			List<String> ignoredPaths, List<String> acceptedPaths) {
		return this.findCommitsByTimeRangeAndDevelopers(startDate, endDate, null, collectChangedPaths, ignoredPaths, acceptedPaths);
	}

	public List<Commit> findCommitsByTimeRangeAndDevelopers(Date startDate, Date endDate, List<String> developers,
			boolean collectChangedPaths, List<String> ignoredPaths, List<String> acceptedPaths){

		List<Commit> commits = new ArrayList<Commit>();

		SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
		svnOperationFactory.setAuthenticationManager(repository.getAuthenticationManager());

		try {
			System.out.print("\n>> DELIVERYDELAY is loking for commits in the informed date interval! "+startDate.toString()+" - "+endDate.toString()+" ... ");

			SvnLog log = svnOperationFactory.createLog();
			log.addTarget(SvnTarget.fromURL(SVNURL.parseURIEncoded(url)));
			log.addRange(SvnRevisionRange.create(SVNRevision.create(startDate), SVNRevision.create(endDate)));
			log.setDiscoverChangedPaths(true);
			log.setDepth(SVNDepth.INFINITY);			
			
			ArrayList<SVNLogEntry> svnLogEntries = new ArrayList<SVNLogEntry>();
			log.run(svnLogEntries);

			System.out.println("Done!\n>>> " + svnLogEntries.size() + " commits were founded!");
			
			int processedCommits = 1;
			int totalCommits = svnLogEntries.size();

			for (SVNLogEntry svnLogEntry : svnLogEntries) {
				
				System.out.println("DELIVERYDELAY is processing commit #"+svnLogEntry.getRevision()+
						" ["+(processedCommits++) +" of "+ totalCommits +"]"+"ChangedPaths "+svnLogEntry.getChangedPaths().size());
				
				if (developers == null || developers != null && developers.contains(svnLogEntry.getAuthor())) {
					boolean betweenSearchRange = svnLogEntry.getDate().after(startDate)
							&& svnLogEntry.getDate().before(endDate);
															
					// Check if the file is in an accepted path
					int commitSystem = -1;
					Collection<SVNLogEntryPath> svnLogEntryPaths = svnLogEntry.getChangedPaths().values();
					if(!svnLogEntryPaths.isEmpty() && !acceptedPaths.isEmpty()){
						String path = svnLogEntryPaths.iterator().next().getPath();
						if(path.startsWith("/trunk/SIPAC/"))
							commitSystem = Team.SIPAC;
						else if(path.startsWith("/trunk/SIGRH"))
							commitSystem = Team.SIGRH;
						else if(path.startsWith("/trunk/SIGAA"))
							commitSystem = Team.SIGAA;
						else
							continue;
					}
					
					if (betweenSearchRange) {
						Commit commit = createCommit(svnLogEntry, collectChangedPaths, ignoredPaths);
						commit.setSystem(commitSystem);
						commit.setChangedPaths(findChangedPathsBySVNLogEntry(svnLogEntry, commit, ignoredPaths));						
						commits.add(commit);
					}
				}
			}
			if (developers != null)
				System.out.println(
						">>> " + commits.size() + " of " + svnLogEntries.size() + " belongs to informed developers");
			return commits;
		} catch (SVNException e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<ChangedPath> findChangedPathsBySVNLogEntry(SVNLogEntry svnLogEntry, Commit associatedCommit,
			List<String> ignoredPaths) {
		Collection<SVNLogEntryPath> svnLogEntryPaths = svnLogEntry.getChangedPaths().values();
		ArrayList<ChangedPath> changedPaths = new ArrayList<ChangedPath>();
		for (SVNLogEntryPath svnLogEntryPath : svnLogEntryPaths) {
			ChangedPath changedPath = createChangedPath(svnLogEntryPath, associatedCommit, ignoredPaths);
			changedPaths.add(changedPath);
		}
		return changedPaths;
	}

	private ChangedPath createChangedPath(SVNLogEntryPath svnLogEntryPath, Commit associatedCommit,
			List<String> ignoredPaths) {
		ChangedPath changedPath = null;
		if (!ignoredPaths.contains(svnLogEntryPath.getPath())) {
			changedPath = new ChangedPath();
			changedPath.setChangeType(svnLogEntryPath.getType());
			changedPath.setCommit(associatedCommit);
			changedPath.setPath(svnLogEntryPath.getPath());
			if (!changedPath.getChangeType().equals('D') && changedPath.getPath().endsWith(".java")) {
				changedPath.setContent(getFileContent(changedPath.getPath(), changedPath.getCommit().getRevision()));
			}
		}
		return changedPath;
	}

	private Commit createCommit(SVNLogEntry svnLogEntry, boolean collectChangedPaths, List<String> ignoredPaths) {
		Commit commit = new Commit(RepositoryType.SVN);		
		commit.setAuthor(svnLogEntry.getAuthor());
		commit.setComment(svnLogEntry.getMessage());
		commit.setCreatedAt(svnLogEntry.getDate());
		commit.setRevision(new Long(svnLogEntry.getRevision()).toString());
		if (collectChangedPaths) {
			List<ChangedPath> changedPaths = findChangedPathsBySVNLogEntry(svnLogEntry, commit, ignoredPaths);
			commit.setChangedPaths(changedPaths);
		}
		commit.setTaskId(CodeRepositoryUtil.getTaskNumberFromCommitComment(commit.getComment()));		
		return commit;
	}

	public Commit findCommitByRevision(String revision, boolean collectChangedPaths, List<String> ignoredPaths, List<String> acceptedPaths){
		Commit commit = null;
		try {
			Long svnRevision = new Long(revision);
			SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
			svnOperationFactory.setAuthenticationManager(repository.getAuthenticationManager());
			SvnLog log = svnOperationFactory.createLog();
			log.addTarget(SvnTarget.fromURL(SVNURL.parseURIEncoded(url)));
			log.addRange(SvnRevisionRange.create(SVNRevision.create(svnRevision), SVNRevision.create(svnRevision)));
			log.setDiscoverChangedPaths(true);
			log.setDepth(SVNDepth.INFINITY);
			ArrayList<SVNLogEntry> svnLogEntries = new ArrayList<SVNLogEntry>();
			log.run(svnLogEntries);

			if (svnLogEntries != null && !svnLogEntries.isEmpty()) {
				if (svnLogEntries.size() > 1)
					throw new NonUniqueCommitByRevisionException("More of one commit was found by one revision");

				SVNLogEntry svnLogEntry = svnLogEntries.iterator().next();

				commit = createCommit(svnLogEntry, collectChangedPaths, ignoredPaths);
			}
		} catch (SVNException e) {
			e.printStackTrace();
		}
		return commit;
	}

	public String getFileContent(String path, String revision) {
		try {
			Long svnRevision = new Long(revision);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			repository.getFile(path, svnRevision, new SVNProperties(), output);
			return new String(output.toByteArray());
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	public List<String> getFileRevisions(String path, String startRevision, String endRevision) {
		ArrayList<SVNFileRevision> svnFileRevisions = new ArrayList<SVNFileRevision>();
		ArrayList<String> revisions = new ArrayList<String>();

		if (startRevision == null)
			startRevision = "1";
		try {
			repository.getFileRevisions(path, svnFileRevisions, new Long(startRevision), new Long(endRevision));
			for (SVNFileRevision svnFileRevision : svnFileRevisions) {
				revisions.add(new Long(svnFileRevision.getRevision()).toString());
			}
		} catch (SVNException e) {
			e.printStackTrace();
		}
		return revisions;
	}
}