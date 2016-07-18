package br.ufrn.deliverydelay.connectors;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgreSQLConnector extends Connector<Connection> {
	
	public PostgreSQLConnector(String user, String password, String url) {
		try {
			Class.forName("org.postgresql.Driver");
			super.encapsulation = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
