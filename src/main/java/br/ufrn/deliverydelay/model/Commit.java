package br.ufrn.deliverydelay.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import br.ufrn.deliverydelay.enuns.RepositoryType;

public class Commit{
		
	private String revision;	
	private int repositoryType;
	private String comment;
	private Date createdAt;	
	private String branch;
	private String author;
	private int churn;
	private int javaFiles;
	private Integer taskId;
	private int system;
	private List<ChangedPath> changedPaths;
	
	public Commit(){
		this(RepositoryType.GIT);
	}
	
	public Commit(RepositoryType repositoryType){
		this.repositoryType = repositoryType.getValue();
		this.changedPaths = new LinkedList<ChangedPath>();
	}
	
	public Commit(String revision, String comment, Date createdAt, String branch,
			String author, RepositoryType repositoryType) {
		this(revision, comment, createdAt, branch, author, new LinkedList<ChangedPath>(), repositoryType);
	}

	public Commit(String revision, String comment, Date createdAt, String branch,
			String author, List<ChangedPath> changedPaths, RepositoryType repositoryType) {
		super();
		this.revision = revision;
		this.comment = comment;
		this.createdAt = createdAt;
		this.branch = branch;
		this.author = author;
		this.changedPaths = changedPaths;
		this.repositoryType = repositoryType.getValue();
	}
	
	public Integer getChurn() {
		return churn;
	}

	public void setChurn(Integer churn) {
		this.churn = churn;
	}

	public Integer getJavaFiles() {
		return javaFiles;
	}

	public void setJavaFiles(Integer javaFiles) {
		this.javaFiles = javaFiles;
	}

	public List<ChangedPath> getChangedPaths() {
		return changedPaths;
	}
	public void setChangedPaths(List<ChangedPath> changedPaths) {
		this.changedPaths = changedPaths;
	}

	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public String getRevision() {
		return revision;
	}
	public void setRevision(String revision) {
		this.revision = revision;
	}
	public String getBranch() {
		return branch;
	}
	public void setBranch(String branch) {
		this.branch = branch;
	}
	public Integer getRepositoryType() {
		return repositoryType;
	}
	public void setRepositoryType(int repositoryType) {
		this.repositoryType = repositoryType;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public Integer getTaskId() {
		return taskId;
	}
	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}
	public Integer getSystem() {
		return system;
	}
	public void setSystem(Integer system) {
		this.system = system;
	}

	@Override
	public String toString() {
		return "Commit [revision=" + revision + ", repositoryType="
				+ repositoryType + ", author=" + author	+ "]";
	}
}
