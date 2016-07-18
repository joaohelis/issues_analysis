package br.ufrn.deliverydelay.model;

public class ChangedPath{

	private Long id;
	private String path;
	private Character changeType;
	private Commit commit;
	private String content;
	private int addedLines;
	private int changedLines;
	private int removedLines;
	
	public ChangedPath(){}
	
	public ChangedPath(String path, Character changeType, Commit commit,
			String content) {
		super();
		this.path = path;
		this.changeType = changeType;
		this.commit = commit;
		this.content = content;
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Character getChangeType() {
		return changeType;
	}
	public void setChangeType(Character changeType) {
		this.changeType = changeType;
	}
	public Commit getCommit() {
		return commit;
	}
	public void setCommit(Commit commit) {
		this.commit = commit;
	}
	public int getAddedLines() {
		return addedLines;
	}
	public void setAddedLines(int addedLines) {
		this.addedLines = addedLines;
	}
	public int getChangedLines() {
		return changedLines;
	}
	public void setChangedLines(int changedLines) {
		this.changedLines = changedLines;
	}
	public int getRemovedLines() {
		return removedLines;
	}
	public void setRemovedLines(int removedLines) {
		this.removedLines = removedLines;
	}

	@Override
	public String toString() {
		return "ChangedPath [id=" + id + ", changeType="
				+ changeType + ", commitID=" + commit.getRevision() + "]";
	}
}
