<%@page import="helpers.DBConnection"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="java.sql.Connection"%>
<%
	Connection connection = DBConnection.getConnection();

	StringBuilder errors = new StringBuilder();
	StringBuilder messages = new StringBuilder();
%>
<%!
	String dbEscape(String value)
	{
		return value.replace("'", "\\'");
	}
%>
<%!
	String dbQueryEscape(String value)
	{
		return value.replace("'", "\\'").replace("*", "%");
	}
%>
<%!
	String toDbName(String value)
	{
		return value.replaceAll(" |-", "_").toLowerCase();
	}
%>
