package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.sql.Result;
import javax.servlet.jsp.jstl.sql.ResultSupport;

import org.apache.commons.lang3.StringUtils;

import helpers.DBConnection;

@WebServlet("/QueryServlet")
public class QueryServlet extends HttpServlet {

	private static final long serialVersionUID = -702218860600042606L;

	public void init() {
		ServletContext context = getServletContext();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processQuery(request, response);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processQuery(request, response);
	}
	
	private void processQuery(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringBuilder errors = new StringBuilder();
		StringBuilder messages = new StringBuilder();
		
		LinkedList<String> nameList = new LinkedList<String>();
		LinkedList<String> dbNameList = new LinkedList<String>();
		LinkedList<String> typeList = new LinkedList<String>();
		ResultSet propertySet = null;
		ResultSet resultData = null;
		Result result = null;
		Statement searchStatement = null;
		
		try (Connection connection = DBConnection.getConnection()) {
			propertySet = getPropertySet(connection);
			getPropertyNames(propertySet, nameList, dbNameList, typeList);
			
			searchStatement = connection.createStatement();
			StringBuilder sql = new StringBuilder();
			
			// search within results
			if (request.getParameter("current_query") != null && "nested".equals(request.getParameter("nested_search")) && !"".equals(request.getParameter("query"))) {
				String currentQuery = request.getParameter("current_query");
				sql.append(StringUtils.substringBeforeLast(currentQuery, "ORDER BY"));
				if (StringUtils.containsIgnoreCase(currentQuery, "WHERE")) {
					sql.append(" AND (");
				}
				else {
					sql.append(" WHERE (");
				}
				appendCriteria(request, sql, dbNameList, typeList);
			}
			// sort results
			else if (request.getParameter("current_query") != null) {
				sql = sortResults(request, sql);
			}
			// quick search
			else if ("Search".equals(request.getParameter("quick_search")) && !"".equals(request.getParameter("query"))) {
				sql = beginRequest(request, sql);
				sql = appendCriteria(request, sql, dbNameList, typeList);
			}
			// advanced search
			else if ("Search".equals(request.getParameter("advanced_search")) && !"".equals(request.getParameter("value1"))) {
				sql = beginRequest(request, sql);
				sql = buildAdvancedQuery(request, sql, dbNameList.getFirst());
			}
			// custom query TODO: better filter
			else if ("Send query".equals(request.getParameter("custom_query")) && !"".equals(request.getParameter("query"))) {
				sql.append(request.getParameter("query"));
				if (StringUtils.containsIgnoreCase(sql.toString(), "delete") || StringUtils.containsIgnoreCase(sql.toString(), "drop")
						|| StringUtils.containsIgnoreCase(sql.toString(), "alter") || StringUtils.containsIgnoreCase(sql.toString(), "grant")
						|| StringUtils.containsIgnoreCase(sql.toString(), "revoke")) {
					errors.append("Unsupported operation: " + sql.toString());
				}
			}
			// empty input: server-side validation
			else {
				errors.append("No input was provided.");
			}
			
			if (errors.length() == 0) {
				resultData = dispatchRequest(searchStatement, sql, messages, errors);
				result = ResultSupport.toResult(resultData);
			}
			
		}
		catch (Exception exc) {
			errors.append("An error occured during database retrieval: " + exc.getMessage());
			exc.printStackTrace();
		}
		finally {
			request.setAttribute("message", messages.toString());
			request.setAttribute("current_query", messages.toString());
			request.setAttribute("error", errors.toString());
			request.setAttribute("nameList", nameList);
			request.setAttribute("dbNameList", dbNameList);
			request.setAttribute("resultData", result);
			
			getServletContext().getRequestDispatcher("/results.jsp").forward(request, response);
			
			if (resultData != null) try { resultData.close(); } catch (SQLException exc) {}
	        if (searchStatement != null) try { searchStatement.close(); } catch (SQLException exc) {}
		}
	}
	
	private ResultSet getPropertySet(Connection connection) throws ClassNotFoundException, SQLException {
		Statement propStatement = connection.createStatement();
		String selectPropertyQuery = "SELECT property.* FROM property, property_group WHERE property_group = property_group_id";
		return propStatement.executeQuery(selectPropertyQuery);
	}
	
	/**
	 * Mutates @param nameList and @param dbNameList
	 * @throws SQLException 
	 */
	private void getPropertyNames(ResultSet propertySet, List<String> names, List<String> dbNames, List<String> types) throws SQLException {
		while (propertySet.next()) {
			String propertyName = propertySet.getString("name");
			String propertyDbName = propertySet.getString("db_name");
			String type = propertySet.getString("type");
			names.add(propertyName);
			dbNames.add(propertyDbName);
			types.add(type);
		}
	}
	
	/**
	 * Mutates @param sql
	 */
	private StringBuilder sortResults(final HttpServletRequest request, StringBuilder sql) {
		String toSort = request.getParameter("current_query");
		String criteria = request.getParameter("sort_criteria");
		sql.append(StringUtils.substringBefore(toSort, "ORDER BY"));
		sql.append(" ORDER BY ");
		sql.append(criteria);
		if (request.getParameter("sort_by") == null || "ASC".equals(request.getParameter("sort_by"))) {
			sql.append(" ASC");
		}
		else {
			sql.append(" DESC");
		}
		
		return sql;
	}
	
	private ResultSet dispatchRequest(Statement searchStatement, StringBuilder sql, StringBuilder messages, StringBuilder errors) throws SQLException {
		ResultSet resultData = null;
		resultData = searchStatement.executeQuery(sql.toString());
		messages.append(sql.toString());
		
		return resultData;
	}
	
	/**
	 * Mutates @param sql
	 */
	private StringBuilder beginRequest(final HttpServletRequest request, StringBuilder sql) {
		sql.append("SELECT * FROM person WHERE (");
		
		String showDeleted = request.getParameter("show_deleted");
		if (showDeleted == null || !"true".equals(showDeleted)) {
			sql.append("deleted = false) AND (");
		}
		
		return sql;
	}
	
	/**
	 * Mutates @param sql
	 */
	private StringBuilder appendCriteria(final HttpServletRequest request, StringBuilder sql, LinkedList<String> dbNameList, LinkedList<String> typeList) {
		String query = request.getParameter("query");
		query = DBConnection.toPostgreSQLWildcards(query);
		
		int listCount = dbNameList.size();
		boolean first = true;
		for (int i = 0; i < listCount; i++)
		{
			if (typeList.get(i).matches("character_varying|text|email")) {
				String dbName = dbNameList.get(i);
				if (!first) {
					sql.append(" OR ");
				}
				else {
					first = false;
				}
				sql.append(dbName);
				sql.append(" ILIKE '%").append(query).append("%'");
			}
		}
		sql.append(") ORDER BY " + dbNameList.getFirst());
		
		return sql;
	}
	
	/**
	 * Mutates @param sql
	 */
	private StringBuilder buildAdvancedQuery(final HttpServletRequest request, StringBuilder sql, String firstCol) {
		// handling multiple requests: for now connected with AND
		for (int i = 1; ; i++) {
			String propertyAndType = request.getParameter("property" + i);
			if (propertyAndType == null) {
				break;
			}
			String property = StringUtils.split(propertyAndType, '$')[0];
			String type = StringUtils.split(propertyAndType, '$')[1];
			String searchTerm = DBConnection.dbEscape(property);
			/* form request according to lexicographic input */
			String compareArg = request.getParameter("lexicographic" + i);
			sql.append(searchTerm);
			sql.append(getCompareArg(compareArg));
			sql.append(DBConnection.dbQueryEscape(request.getParameter("value" + i)));
			sql.append("'");
			sql.append(" AND ");
		}
		sql = new StringBuilder(StringUtils.substringBeforeLast(sql.toString(), "AND"));
		sql.append(") ORDER BY ").append(firstCol);
		
		return sql;
	}
	
	private String getCompareArg(String compareArg) {
		switch (compareArg) {
			case "is":
				return " = '";
			case "isnot":
				return " != '";
			case "biggerOrEqual":
				return " >= '";
			case "smallerOrEqual":
				return " <= '";
			default:
				return " ILIKE '";
		}
	}
}
