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

import helpers.DBConnection;

@WebServlet("/RefreshedQueryServlet")
public class RefreshedQueryServlet extends HttpServlet {

	private static final long serialVersionUID = 3677109899403365393L;

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
			
			// process old query
			if (request.getParameter("current_query") != null && !"to_sort".equals(request.getParameter("go_sort")) && !"nested".equals(request.getParameter("nested_search"))) {
				sql.append(request.getParameter("current_query"));
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
}
