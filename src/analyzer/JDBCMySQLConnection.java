package analyzer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCMySQLConnection {
	
	private static JDBCMySQLConnection instance = new JDBCMySQLConnection();
	
	public static final String userName = "root";
	
	public static final String password = "";
	
	public static final String url = "jdbc:mysql://localhost/word_occurrences";
	
	public static final String driver_class = "com.mysql.cj.jdbc.Driver";
	
	private JDBCMySQLConnection() {
		try {
			Class.forName(driver_class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Connection createConnection() {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(url, userName, password);
		} catch (SQLException e) {
			System.out.println("ERROR: Unable to connect to DB");
		}
		return connection;
	}
	
	public static Connection getConnection() {
		return instance.createConnection();
	}
}
