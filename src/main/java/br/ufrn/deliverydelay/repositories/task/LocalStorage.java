package br.ufrn.deliverydelay.repositories.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import br.ufrn.deliverydelay.connectors.Connector;
import br.ufrn.deliverydelay.connectors.PostgreSQLConnector;
import br.ufrn.deliverydelay.model.Commit;

public class LocalStorage {

	private String DB_URL; 
	private String USERNAME; 
	private String PASSWORD;	
	
	/**
	 * @param dB_URL
	 * @param uSERNAME
	 * @param pASSWORD
	 */
	public LocalStorage(String dB_URL, String uSERNAME, String pASSWORD) {
		super();
		DB_URL = dB_URL;
		USERNAME = uSERNAME;
		PASSWORD = pASSWORD;
	}

	static final String INSERT = "INSERT INTO deliverydelay.commit("
			+ "revision, commit_comment, createdat, author, churn, javafiles, task_id, repository_type, id_sistema) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

	public void saveCommits(List<Commit> commits) {

		Connection conn = null;
		PreparedStatement preparedStatement = null;

		Connector<Connection> pg = new PostgreSQLConnector(USERNAME, PASSWORD, DB_URL);

		conn = pg.getEncapsulation();
		try {
			preparedStatement = conn.prepareStatement(INSERT);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
			
		int insertCount = 0;
		for (Commit commit : commits) {
			
			insertCount++;
			try {
				preparedStatement.setString(1, commit.getRevision());
				preparedStatement.setString(2, commit.getComment());
				preparedStatement.setDate(3, new java.sql.Date(commit.getCreatedAt().getTime()));
				preparedStatement.setString(4, commit.getAuthor());
				preparedStatement.setInt(5, (commit.getChurn() != null) ? commit.getChurn() : -1);
				preparedStatement.setInt(6, (commit.getJavaFiles() != null) ? commit.getJavaFiles() : -1);
				preparedStatement.setInt(7, (commit.getTaskId() != null) ? commit.getTaskId() : -1);
				preparedStatement.setInt(8, (commit.getRepositoryType() != null) ? commit.getRepositoryType() : -1);
				preparedStatement.setInt(9, (commit.getSystem() != null) ? commit.getSystem() : -1);
				preparedStatement.addBatch();
				if (insertCount % 50 == 0){
					preparedStatement.executeBatch();
					preparedStatement.clearBatch();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}			
		}
		try {
			preparedStatement.executeBatch();
			preparedStatement.clearBatch();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}