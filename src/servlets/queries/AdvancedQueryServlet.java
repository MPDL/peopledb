package servlets.queries;

import java.io.IOException;
import java.sql.Connection;
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
import helpers.InputValidator;

@WebServlet("/AdvancedQueryServlet")
public class AdvancedQueryServlet extends HttpServlet {

	private static final long serialVersionUID = -4925455607191756110L;

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
		LinkedList<String> groupNameList = new LinkedList<String>();
		LinkedList<String> typeList = new LinkedList<String>();
		ResultSet propertySet = null;
		ResultSet resultData = null;
		Result result = null;
		Statement searchStatement = null;
		
		try (Connection connection = DBConnection.getConnection()) {
			propertySet = getPropertySet(connection);
			getPropertyNames(propertySet, nameList, dbNameList, groupNameList, typeList);
			
			searchStatement = connection.createStatement();
			StringBuilder sql = new StringBuilder();
			
			String firstValue = request.getParameter("value1");
			String firstBooleanValue = request.getParameter("booleanSelect1");
			if ("Search".equals(request.getParameter("advanced_search")) && (!StringUtils.isAllEmpty(firstValue, firstBooleanValue))) {
				sql = beginRequest(request, sql);
				sql = buildAdvancedQuery(request, sql, dbNameList.getFirst());
			}
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
			request.setAttribute("error", errors.toString());
			request.setAttribute("current_query", messages.toString());
			request.setAttribute("nameList", nameList);
			request.setAttribute("dbNameList", dbNameList);
			request.setAttribute("groupList", groupNameList);
			request.setAttribute("resultData", result);
			
			getServletContext().getRequestDispatcher("/results.jsp").forward(request, response);
			
			if (resultData != null) try { resultData.close(); } catch (SQLException exc) {}
	        if (searchStatement != null) try { searchStatement.close(); } catch (SQLException exc) {}
		}
	}
	
	private ResultSet getPropertySet(Connection connection) throws ClassNotFoundException, SQLException {
		Statement propStatement = connection.createStatement();
		String selectPropertyQuery = DBConnection.getPropertyQuery();
		return propStatement.executeQuery(selectPropertyQuery);
	}
	
	/**
	 * Mutates @param nameList and @param dbNameList
	 * @throws SQLException 
	 */
	private void getPropertyNames(ResultSet propertySet, List<String> names, List<String> dbNames, List<String> groups, List<String> types) throws SQLException {
		while (propertySet.next()) {
			String propertyName = propertySet.getString("name");
			String propertyDbName = propertySet.getString("db_name");
			String groupName = propertySet.getString("group_name");
			String type = propertySet.getString("type");
			names.add(propertyName);
			dbNames.add(propertyDbName);
			groups.add(groupName);
			types.add(type);
		}
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
	private StringBuilder buildAdvancedQuery(final HttpServletRequest request, StringBuilder sql, String firstCol) throws SQLException {
		// handling multiple requests: for now connected with AND
		for (int i = 1; ; i++) {
			String propertyAndType = request.getParameter("property" + i);
			if (propertyAndType == null) {
				break;
			}
			String property = StringUtils.split(propertyAndType, '$')[0];
			String type = StringUtils.split(propertyAndType, '$')[1];
			String searchTerm = DBConnection.dbEscape(property);
			String parameterValue = getSearchParameterValue(request, i);
			// validate input
			if (!new InputValidator().validateInput(parameterValue, type)) {
				throw new SQLException("Invalid value '" + parameterValue  + "' for parameter '" + searchTerm + "' of type '" + type + "'.");
			}
			// form request according to lexicographic input
			String compareArg = request.getParameter("lexicographic" + i);
			String matchArg = request.getParameter("queryMatch" + i);
			// add matching to query style with appropriate types
			sql.append(searchTerm);
			sql.append(getComparator(DBConnection.dbQueryEscape(parameterValue), compareArg, matchArg, type));
			sql.append(" AND ");
		}
		sql = new StringBuilder(StringUtils.substringBeforeLast(sql.toString(), "AND"));
		sql.append(") ORDER BY ").append(firstCol);
		
		return sql;
	}
	
	private String getComparator(String parameterValue, String compareArg, String matchArg, String type) {
		// TODO patterns and enums
		// (I)LIKE supported only for type varchar
		if (type.equals("character_varying") && compareArg.equals("is") && matchArg.equals("matchLike")) {
			return " ILIKE '%" + parameterValue + "%' ";
		}
		else if (type.equals("character_varying") && compareArg.equals("isnot") && matchArg.equals("matchLike")) {
			return " NOT ILIKE '%" + parameterValue + "%' ";
		}
		else if (type.equals("character_varying") && compareArg.equals("is") && matchArg.equals("matchExact")) {
			return " ILIKE '" + parameterValue + "' ";
		}
		// if the "is" search is case-insensitive, the "is not" search should be case-insensitive as well
		else if (type.equals("character_varying") && compareArg.equals("isnot") && matchArg.equals("matchExact")) {
			return " NOT ILIKE '" + parameterValue + "' ";
		}
		else if (type.equals("boolean") && compareArg.equals("is")) {
			return " IS " + parameterValue;
		}
		else if (type.equals("boolean") && compareArg.equals("isnot")) {
			return " IS NOT " + parameterValue;
		}
		else if (compareArg.equals("is")) {
			return " = '" + parameterValue + "' ";
		}
		else if (compareArg.equals("isnot")) {
			return " != '" + parameterValue + "' ";
		}
		else if (compareArg.equals("biggerOrEqual")) {
			return " >= '" + parameterValue + "' ";
		}
		else if (compareArg.equals("smallerOrEqual")) {
			return " <= '" + parameterValue + "' ";
		}
		return " = '" + parameterValue + "' ";
	}
	
	private String getSearchParameterValue(HttpServletRequest request, int widget) {
		if (request.getParameter("value" + widget) != null && !"".equals(request.getParameter("value" + widget))) {
			return request.getParameter("value" + widget);
		}
		if (request.getParameter("booleanSelect" + widget) != null) {
			return request.getParameter("booleanSelect" + widget);
		}
		throw new IllegalArgumentException("No input was provided for parameter number " + widget + ".");
	}
}
