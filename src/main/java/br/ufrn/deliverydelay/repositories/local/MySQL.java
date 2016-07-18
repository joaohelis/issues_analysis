package br.ufrn.deliverydelay.repositories.local;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import br.ufrn.deliverydelay.connectors.Connector;
import br.ufrn.deliverydelay.connectors.PostgreSQLConnector;

public class MySQL {
	
	static final String DB_URL = "jdbc:mysql://localhost/sigsalesjpa";
	static final String USERNAME = "root";
	static final String PASSWORD = "81011236";

	public static void main(String[] args){
		
		Connection conn = null;
		Statement stmt = null;
		
		try {
			Connector<Connection> pg = new PostgreSQLConnector(USERNAME, PASSWORD, DB_URL);
			
			conn = pg.getEncapsulation();
			stmt = conn.createStatement();
									
			ResultSet rs = stmt.executeQuery("select * from category");
	        while (rs.next()) {
	            System.out.println(rs.getString("id"));	            
	        }
			
			
			
//			// STEP 2: Register JDBC driver
//			Class.forName("com.mysql.jdbc.Driver");
//
//			// STEP 3: Open a connection
//			System.out.println("Connecting to a selected database...");
//			conn = DriverManager.getConnection(DB_URL, USER, PASS);
//			System.out.println("Connected database successfully...");
//
//			// STEP 4: Execute a query
//			System.out.println("Inserting records into the table...");
//			stmt = conn.createStatement();
//
//			String sql = "INSERT INTO Registration " + "VALUES (100, 'Zara', 'Ali', 18)";
//			stmt.executeUpdate(sql);
//			sql = "INSERT INTO Registration " + "VALUES (101, 'Mahnaz', 'Fatma', 25)";
//			stmt.executeUpdate(sql);
//			sql = "INSERT INTO Registration " + "VALUES (102, 'Zaid', 'Khan', 30)";
//			stmt.executeUpdate(sql);
//			sql = "INSERT INTO Registration " + "VALUES(103, 'Sumit', 'Mittal', 28)";
//			stmt.executeUpdate(sql);
//			System.out.println("Inserted records into the table...");

		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					conn.close();
			} catch (SQLException se) {
			} // do nothing
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		System.out.println("Goodbye!");
	}// end main
}
