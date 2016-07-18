package br.ufrn.deliverydelay.miner;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.sql.Date;
import java.util.List;

import br.ufrn.deliverydelay.exceptions.MissingParameterException;
import br.ufrn.deliverydelay.model.ChangedPath;
import br.ufrn.deliverydelay.model.Commit;
import br.ufrn.deliverydelay.repositories.code.CodeRepository;
import br.ufrn.deliverydelay.util.CodeRepositoryUtil;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

public class ChurnMiner extends AbstractMiner {

	private List<Commit> specificCommitsToMining;

	public ChurnMiner(CodeRepository codeRepository, Date startDate, Date endDate,
			List<String> developers, List<String> ignoredPaths, List<String> acceptedPaths) {
		super(codeRepository, startDate, endDate, developers, ignoredPaths, acceptedPaths);
	}

	@Override
	public boolean setupMinerSpecific() throws MissingParameterException {

		System.out.println("=========================================================");
		System.out.println("---------------------------------------------------------");

		return codeRepository.connect();
	}

	@Override
	public void execute() {

		List<Commit> commits = null;

		System.out.println("\n---------------------------------------------------------");
		System.out.println("DELIVERYDELAY - CALCULATE CHURN METRIC");
		System.out.println("---------------------------------------------------------");

		if (specificCommitsToMining != null) {
			commits = specificCommitsToMining;
		} else {
			if (developers == null) {
				commits = codeRepository.findCommitsByTimeRange(startDate, endDate, true, ignoredPaths, acceptedPaths);
			} else {
				commits = codeRepository.findCommitsByTimeRangeAndDevelopers(startDate, endDate, developers, true,
						ignoredPaths, acceptedPaths);
			}
		}

		calculateChurnToCommits(commits);

		System.out.println("\n---------------------------------------------------------");
		System.out.println("THE CHURN METRIC WAS CALCULATED!");
		System.out.println("=========================================================");
	}

	private void calculateChurnToCommits(List<Commit> commits) {

		int processedCommits = 0;

		if (!commits.isEmpty())
			System.out.println("\n>> DELIVERYDELAY IS CALCULATING METRICS TO FOUNDED COMMITS ... ");
		else
			System.out.println("\n>> NO COMMIT WAS FOUNDED!");

		for (Commit commit : commits) {
			System.out.println("\n---------------------------------------------------------");
			System.out.println("REVISION #"
					+ ((commit.getRevision().length() <= 7) ? commit.getRevision()
							: commit.getRevision().subSequence(0, 7))
					+ " - [" + ++processedCommits + "]/[" + commits.size() + "]");

			if (commit.getTaskId() == null)
				continue;

			List<ChangedPath> changedPaths = commit.getChangedPaths();

			cp: for (ChangedPath changedPath : changedPaths) {

				if (changedPath.getChangeType().equals('D') || !(changedPath.getPath().toLowerCase().contains(".java")))
					continue;

				commit.setJavaFiles(commit.getJavaFiles() + 1);

				System.out.println(">>> Path: " + changedPath.getPath());

				String currentContent = changedPath.getContent();

				if (changedPath.getChangeType().equals('A')) {

					List<String> lines = CodeRepositoryUtil.getContentByLines(currentContent);

					Integer added = 0;
					for (String line : lines) {
						if (CodeRepositoryUtil.isComment(line))
							continue;
						added++;
					}

					commit.setChurn(commit.getChurn() + added);

				} else {

					System.out.print(">>> It's looking for path revisions ... ");
					
					List<String> fileRevisions = null;
					
					boolean revisionsExecuted = false;
					while(!revisionsExecuted) {
						try {
							fileRevisions = codeRepository.getFileRevisions(changedPath.getPath(), null,
									changedPath.getCommit().getRevision());

							System.out.println("Done! " + fileRevisions.size() + " revisions were founded!");
							
							revisionsExecuted = true;
						} catch(Exception e) {
							if(e.getCause() instanceof ConnectException || 
									e.getCause() instanceof UnknownHostException || 
									e.getMessage().contains("Connection reset") || 
									e.getMessage().contains("recv failed")) {								
								e.printStackTrace();
								revisionsExecuted = true;
								continue cp;
//								try {
//									Thread.sleep(300000);
//								} catch (InterruptedException e1) {
//									// TODO Auto-generated catch block
//									e1.printStackTrace();
//								}
//								System.out.println("Falha em capturar as revisoes do arquivo. Tentaremos de novo");
//								revisionsExecuted = false;
							} else if(e.getMessage().contains("is not a file in revision")) {
								continue cp;
							} else if(e.getMessage().contains("path not found: 404 Not Found")) {
								continue cp;
							} else {
								throw e;
							}
						}
					}
									
//					List<String> fileRevisions = codeRepository.getFileRevisions(changedPath.getPath(), null,
//							changedPath.getCommit().getRevision());
//
//					System.out.println("Done! " + fileRevisions.size() + " revisions were founded!");

					String previousRevision = CodeRepositoryUtil
							.getPreviousRevision(changedPath.getCommit().getRevision(), fileRevisions);

					String previousContent = null;

					previousContent = codeRepository.getFileContent(changedPath.getPath(), previousRevision);

					if (currentContent == null || previousContent == null)
						continue;

					calculateChurnToChangedPath(changedPath, previousContent, currentContent);

					int changedPathChurn = changedPath.getAddedLines() + changedPath.getChangedLines()
							+ changedPath.getRemovedLines();

					commit.setChurn(commit.getChurn() + changedPathChurn);
				}
			}
			System.out.println("---------------------------------------------------------");
		}
	}

	private void calculateChurnToChangedPath(ChangedPath changedPath, String previousContent, String currentContent) {

		Integer added = 0;
		Integer deleted = 0;
		Integer changed = 0;

		Patch<String> patch = DiffUtils.diff(CodeRepositoryUtil.getContentByLines(previousContent),
				CodeRepositoryUtil.getContentByLines(currentContent));

		List<Delta<String>> deltas = patch.getDeltas();

		for (Delta<String> delta : deltas) {
			switch (delta.getType()) {
			case DELETE:
				List<String> deletedLines = (List<String>) delta.getOriginal().getLines();
				for (String line : deletedLines) {
					if (CodeRepositoryUtil.isComment(line)) {
						continue;
					}
					deleted++;
				}
				break;
			case CHANGE:
				List<String> changedLines = (List<String>) delta.getRevised().getLines();
				for (String line : changedLines) {
					if (CodeRepositoryUtil.isComment(line)) {
						continue;
					}
					changed++;
				}
				break;
			case INSERT:
				List<String> addedLines = (List<String>) delta.getRevised().getLines();
				for (String line : addedLines) {
					if (CodeRepositoryUtil.isComment(line)) {
						continue;
					}
					added++;
				}
				break;
			}
		}
		changedPath.setAddedLines(added);
		changedPath.setChangedLines(changed);
		changedPath.setRemovedLines(deleted);
	}

	public List<Commit> getSpecificCommitsToMining() {
		return specificCommitsToMining;
	}

	public void setSpecificCommitsToMining(List<Commit> specificCommitsToMining) {
		this.specificCommitsToMining = specificCommitsToMining;
	}
}