package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import helpers.DBConnection;

@WebServlet("/NewGroupServlet")
public class NewGroupServlet extends HttpServlet {

	private static final long serialVersionUID = -8758546033222188943L;
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringBuilder messages = new StringBuilder();
		StringBuilder errors = new StringBuilder();
		
		Statement propGroupStatement = null;
		ResultSet propertyGroupSet = null;
		Statement testPropStatement = null;
		ResultSet testPropSet = null;
		Statement createStatement = null;
		
		try (Connection connection = DBConnection.getConnection()) {
			propGroupStatement = connection.createStatement();
			propertyGroupSet = propGroupStatement.executeQuery("SELECT * FROM property_group");
			
			if ("Save".equals(request.getParameter("save"))) {
				synchronized(this) {
					String gName = request.getParameter("group_name");
					validateGroupName(gName);
					
					testPropStatement = connection.createStatement();
					testPropSet = testPropStatement.executeQuery("SELECT * FROM property_group WHERE property_group.name ILIKE '" + DBConnection.dbEscape(gName) + "'");
					if (testPropSet.next()) {
						errors.append("Property group '" + gName + "' already exists.");
					}
					else {
						String dbName = DBConnection.toDbName(gName);
						createStatement = connection.createStatement();
						createStatement.executeUpdate("INSERT INTO property_group (db_name, name) VALUES ('" + dbName + "', '" + gName + "')");
						
						messages.append("Property group " + gName + " created");
					}
				}
			}
		}
		catch (SQLException | ClassNotFoundException exc) {
			errors.append("Property group could not be created: " + exc.getMessage());
		}
		finally {
			request.setAttribute("message", messages.toString());
			request.setAttribute("error", errors.toString());
			
			getServletContext().getRequestDispatcher("/newGroup.jsp").forward(request, response);
			
			if (propGroupStatement != null) try { propGroupStatement.close(); } catch (SQLException exc) {}
			if (propertyGroupSet != null) try { propertyGroupSet.close(); } catch (SQLException exc) {}
			if (testPropStatement != null) try { testPropStatement.close(); } catch (SQLException exc) {}
			if (testPropSet != null) try { testPropSet.close(); } catch (SQLException exc) {}
			if (createStatement != null) try { createStatement.close(); } catch (SQLException exc) {}
		}
	}
	
	private void validateGroupName(String gName) throws SQLException {
		if (StringUtils.isNumeric(gName)) {
			throw new SQLException("Group name cannot contain only numeric characters.");
		}
		if (!StringUtils.isAlphanumericSpace(gName)) {
			throw new SQLException("Invalid character in property group name.");
		}
		// group names have a character limit. TODO increase limit and add proper CSS line breaks
		if (gName.length() > 60) {
			throw new SQLException("Group name is too long. Maximal length: 60 characters.");
		}
	}
	
}
