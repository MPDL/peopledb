package helpers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

public class DBConnection {

	public static Connection getConnection() throws ClassNotFoundException, SQLException {
		Class.forName("org.postgresql.Driver");
		Connection connection = null;
		connection = DriverManager.getConnection("jdbc:postgresql://vm28.mpdl.mpg.de/people", System.getProperty("peopleDBName"), System.getProperty("peopleDBPassword"));
		
		return connection;
	}
	
	public static String dbEscape(String value) {
		return value.replace("'", "\\'");
	}
	
	public static String dbQueryEscape(String value) {
		value = value.replace("'", "\\'").replace("*", "%");
		return StringUtils.replaceIgnoreCase(value, "SELECT % FROM", "SELECT * FROM");
	}
	
	public static String toDbName(String value) {
		return value.replaceAll(" |-", "_").toLowerCase();
	}
	
	public static String toPostgreSQLWildcards(String expression) {
		return expression.replaceAll("\\*", "%").replaceAll("\\?", "_");
	}
}
