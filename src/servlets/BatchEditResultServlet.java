package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

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

@WebServlet("/BatchEditResultServlet")
public class BatchEditResultServlet extends HttpServlet {

	private static final long serialVersionUID = -6010413981611552024L;

	public void init() {
		ServletContext context = getServletContext();
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String current_query = request.getParameter("current_query");
		request.setAttribute("current_query", current_query);
		
		StringBuilder messages = new StringBuilder();
		StringBuilder errors = new StringBuilder();
		
		String[] toEdit = request.getParameterValues("toChoose");
		
		Statement propStatement = null;
		Statement updateStatement = null;
		Statement selectStatement = null;
		ResultSet propertySet = null;
		ResultSet resultData = null;
		LinkedList<String> nameList = new LinkedList<String>();
		LinkedList<String> dbNameList = new LinkedList<String>();
		Result result = null;
		
		try (Connection connection = DBConnection.getConnection()) {
			propStatement = connection.createStatement();
			propertySet = propStatement.executeQuery(DBConnection.getPropertyQuery());
			
			
			while (propertySet.next()) {
				String propertyName = propertySet.getString("name");
				String propertyDbName = propertySet.getString("db_name");
				nameList.add(propertyName);
				dbNameList.add(propertyDbName);
			}

			StringBuilder selectedIDs = new StringBuilder("(");
			if (toEdit != null) {
				for (String value : toEdit) {
					selectedIDs.append(value);
					selectedIDs.append(",");
				}
					selectedIDs.setCharAt(selectedIDs.length() - 1, ')');
			}

			StringBuilder sql = new StringBuilder();
			sql.append("UPDATE person SET ");
			for (int i = 1; ; i++) {
				String propertyAndType = request.getParameter("property" + i);
				if (propertyAndType == null) {
					break;
				}
				String property = StringUtils.split(propertyAndType, '$')[0];
				String type = StringUtils.split(propertyAndType, '$')[1];
				String searchTerm = DBConnection.dbEscape(property);
				String newValue = DBConnection.dbQueryEscape(getBatchEditValue(request, i));
				if (sql.toString().contains(searchTerm)) {
					errors.append("A property has been specified twice. Please review your inputs.");
					break;
				}
				sql.append(searchTerm);
				sql.append(" = ");
				if (type.equals("boolean")) {
					sql.append(newValue);
				}
				else {
					sql.append("'").append(newValue).append("'");
				}
				sql.append(" , ");
			}
			sql = new StringBuilder(StringUtils.substringBeforeLast(sql.toString(), ","));
			sql.append(" WHERE person_id IN ");
			sql.append(selectedIDs.toString());
			
			synchronized(this) {
				if (errors.length() == 0) {
					updateStatement = connection.createStatement();
					updateStatement.executeUpdate(sql.toString());
					
					selectStatement = connection.createStatement();
					resultData = selectStatement.executeQuery("SELECT * FROM person WHERE person_id IN " + selectedIDs.toString());
					messages.append("Batch edit operation was successful.");
				}
			}
			
			result = ResultSupport.toResult(resultData);
		}
		catch(SQLException | ClassNotFoundException exc) {
			errors.append(exc.getMessage());
		}
		finally {
			request.setAttribute("message", messages.toString());
			request.setAttribute("error", errors.toString());
			request.setAttribute("result", result);
			request.setAttribute("nameList", nameList);
			request.setAttribute("dbNameList", dbNameList);
			
			getServletContext().getRequestDispatcher("/batchEditResult.jsp").forward(request, response);
			
			if (propertySet != null) try { propertySet.close(); } catch (SQLException exc) {}
			if (resultData != null) try { resultData.close(); } catch (SQLException exc) {}
	        if (updateStatement != null) try { updateStatement.close(); } catch (SQLException exc) {}
	        if (selectStatement != null) try { selectStatement.close(); } catch (SQLException exc) {}
	        if (propStatement != null) try { propStatement.close(); } catch (SQLException exc) {}
		}
	}
	
	private String getBatchEditValue(HttpServletRequest request, int widget) {
		if (request.getParameter("new_prop" + widget) != null && !"".equals(request.getParameter("new_prop" + widget))) {
			return request.getParameter("new_prop" + widget);
		}
		if (request.getParameter("booleanSelect" + widget) != null) {
			return request.getParameter("booleanSelect" + widget);
		}
		throw new IllegalArgumentException("No input was provided for parameter number " + widget + ".");
	}
}
