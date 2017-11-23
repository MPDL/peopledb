package servlets;

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

@WebServlet("/ViewAllServlet")
public class ViewAllServlet extends HttpServlet {

	private static final long serialVersionUID = -2889922891971140765L;

	public void init() {
		ServletContext context = getServletContext();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		StringBuilder messages = new StringBuilder();
		String messages = request.getParameter("message");
		StringBuilder errors = new StringBuilder();
		
		LinkedList<String> nameList = new LinkedList<String>();
		LinkedList<String> dbNameList = new LinkedList<String>();
		ResultSet propertySet = null;
		ResultSet resultData = null;
		Result result = null;
		Statement searchStatement = null;
		
		try (Connection connection = DBConnection.getConnection()) {
			propertySet = getPropertySet(connection);
			getPropertyNames(propertySet, nameList, dbNameList);
			
			searchStatement = connection.createStatement();
//			String sql = "SELECT person.* FROM person, person_intern WHERE (person.person_id=person_intern.person_id AND person_intern.deleted = FALSE) ORDER BY " + dbNameList.getFirst();
			String sql = "SELECT * FROM person WHERE (deleted = FALSE) ORDER BY " + dbNameList.getFirst();
			
			resultData = dispatchRequest(searchStatement, sql, null, errors);
			result = ResultSupport.toResult(resultData);
			
			request.setAttribute("current_query", sql);
		}
		catch (Exception exc) {
			errors.append("An error occured during database retrieval: " + exc.getMessage());
			exc.printStackTrace();
		}
		finally {
			request.setAttribute("message", messages);
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
	private void getPropertyNames(ResultSet propertySet, List<String> names, List<String> dbNames) throws SQLException {
		while (propertySet.next()) {
			String propertyName = propertySet.getString("name");
			String propertyDbName = propertySet.getString("db_name");
			names.add(propertyName);
			dbNames.add(propertyDbName);
		}
	}
	
	private ResultSet dispatchRequest(Statement searchStatement, String sql, StringBuilder messages, StringBuilder errors) throws SQLException {
		ResultSet resultData = null;
		resultData = searchStatement.executeQuery(sql);
		
		return resultData;
	}
}
