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

@WebServlet("/CustomQueryServlet")
public class CustomQueryServlet extends HttpServlet {

	private static final long serialVersionUID = 3916561089562495184L;

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
		
		List<String> nameList = new LinkedList<>();
		List<String> dbNameList = new LinkedList<>();
		
		Statement customStatement = null;
		ResultSet resultData = null;
		ResultSet propertySet = null;
		Result result = null;
		
		try (Connection connection = DBConnection.getConnection()) {
			customStatement = connection.createStatement();
			StringBuilder sql = new StringBuilder();
			
			// TODO: better filter
			if ("Send query".equals(request.getParameter("custom_query")) && !"".equals(request.getParameter("query"))) {
				sql.append(request.getParameter("query"));
				if (queryInvalid(sql.toString())) {
					throw new SQLException("Unsupported operation: " + sql.toString());
				}
			}
			else {
				// empty input: server-side validation
				throw new SQLException("No input was provided.");
			}
			
			if (errors.length() == 0) {
				String query = sql.toString();
				if (StringUtils.containsIgnoreCase(query, "SELECT") && StringUtils.containsIgnoreCase(query, "FROM")) {
					resultData = customStatement.executeQuery(query);
					if (resultData != null) {
						result = ResultSupport.toResult(resultData);
					}
					
					propertySet = getPropertySet(connection);
					getPropertyNames(propertySet, nameList, dbNameList);
				}
				else {
					customStatement.executeUpdate(query);
				}
				
				messages.append(sql.toString());
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
			
			if (result != null) {
				request.setAttribute("resultData", result);
				request.setAttribute("nameList", nameList);
				request.setAttribute("dbNameList", dbNameList);
				getServletContext().getRequestDispatcher("/results.jsp").forward(request, response);
			}
			else {
				response.sendRedirect("/people/ViewAllServlet");
			}
			
			if (resultData != null) try { resultData.close(); } catch (SQLException exc) {}
			if (customStatement != null) try { customStatement.close(); } catch (SQLException exc) {}
			if (propertySet != null) try { propertySet.close(); } catch (SQLException exc) {}
		}
	}
	
	public boolean queryInvalid(String sql) {
		return (StringUtils.containsIgnoreCase(sql.toString(), "delete") && !StringUtils.containsIgnoreCase(sql.toString(), "deleted"))
				|| StringUtils.containsIgnoreCase(sql.toString(), "drop") || StringUtils.containsIgnoreCase(sql.toString(), "alter") 
				|| StringUtils.containsIgnoreCase(sql.toString(), "grant")|| StringUtils.containsIgnoreCase(sql.toString(), "revoke");
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
	private void getPropertyNames(ResultSet propertySet, List<String> names, List<String> dbNames) throws SQLException {
		while (propertySet.next()) {
			String propertyName = propertySet.getString("name");
			String propertyDbName = propertySet.getString("db_name");
			names.add(propertyName);
			dbNames.add(propertyDbName);
		}
	}
	
}
