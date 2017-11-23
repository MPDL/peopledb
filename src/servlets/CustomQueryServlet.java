package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
		
		Statement customStatement = null;
		ResultSet resultData = null;
		Result result = null;
		int updatedRows = 0;
		
		try (Connection connection = DBConnection.getConnection()) {
			customStatement = connection.createStatement();
			StringBuilder sql = new StringBuilder();
			
			// TODO: better filter
			if ("Send query".equals(request.getParameter("custom_query")) && !"".equals(request.getParameter("query"))) {
				sql.append(request.getParameter("query"));
				if (queryInvalid(sql.toString())) {
					errors.append("Unsupported operation: " + sql.toString());
				}
			}
			else {
				// empty input: server-side validation
				errors.append("No input was provided.");
			}
			
			if (errors.length() == 0) {
				String query = sql.toString();
				if (StringUtils.containsIgnoreCase(query, "SELECT FROM")) {
					resultData = customStatement.executeQuery(query);
					if (resultData != null) {
						result = ResultSupport.toResult(resultData);
					}
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
				getServletContext().getRequestDispatcher("/results.jsp").forward(request, response);
			}
			else {
				response.sendRedirect("/people/ViewAllServlet");
			}
			
			if (resultData != null) try { resultData.close(); } catch (SQLException exc) {}
			if (customStatement != null) try { customStatement.close(); } catch (SQLException exc) {}
		}
	}
	
	public boolean queryInvalid(String sql) {
		return StringUtils.containsIgnoreCase(sql.toString(), "delete") || StringUtils.containsIgnoreCase(sql.toString(), "drop")
				|| StringUtils.containsIgnoreCase(sql.toString(), "alter") || StringUtils.containsIgnoreCase(sql.toString(), "grant")
				|| StringUtils.containsIgnoreCase(sql.toString(), "revoke");
	}
}
