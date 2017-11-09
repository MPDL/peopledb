package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.SortedMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.sql.Result;
import javax.servlet.jsp.jstl.sql.ResultSupport;

import helpers.DBConnection;
import helpers.InputValidator;

@WebServlet("/EditPersonServlet")
public class EditPersonServlet extends HttpServlet {

	private static final long serialVersionUID = 2888479459153873622L;
	
	public void init() {
		ServletContext context = getServletContext();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int personId;
		String idRequest = request.getParameter("person_id");
		if (idRequest != null && !"".equals(idRequest)) {
			personId = Integer.parseInt(idRequest);
		}
		else {
			personId = 0;
		}
		
		Statement propStatement = null, personStatement = null;
		ResultSet propertySet = null, personData = null;
		Result propSet = null, pData = null;
		
		StringBuilder messages = new StringBuilder();
		StringBuilder errors = new StringBuilder();
		
		try (Connection connection = DBConnection.getConnection()) {
			propStatement = connection.createStatement();
			propertySet = propStatement.executeQuery("SELECT property.*, property_group.name AS group_name FROM property, property_group WHERE property_group = property_group_id ORDER BY (property_group.name != 'Basic Data'), property_group.name, property.property_id");
			propSet = ResultSupport.toResult(propertySet);
			personStatement = connection.createStatement();
			
			if (request.getParameter("person_id") != null) {
				personData = personStatement.executeQuery("SELECT * FROM person WHERE person_id = " + personId);
				pData = ResultSupport.toResult(personData);
			}
			
		}
		catch (SQLException | ClassNotFoundException exc) {
			errors.append("Error during fetching properties from database: " + exc.getMessage());
		}
		finally {
			request.setAttribute("message", messages.toString());
			request.setAttribute("error", errors.toString());
			request.setAttribute("propertySet", propSet);
			request.setAttribute("personData", pData);
			request.setAttribute("person_id", personId + "");
			
			getServletContext().getRequestDispatcher("/editPerson.jsp").forward(request, response);
			
	        if (propStatement != null) try { propStatement.close(); } catch (SQLException exc) {}
	        if (personStatement != null) try { personStatement.close(); } catch (SQLException exc) {}
	        if (propertySet != null) try { propertySet.close(); } catch (SQLException exc) {}
	        if (personData != null) try { personData.close(); } catch (SQLException exc) {}
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int personId = (request.getParameter("person_id") != null && !"".equals(request.getParameter("person_id"))) ? Integer.parseInt(request.getParameter("person_id")) : 0;
		
		Connection connection = null;
		Statement propStatement = null;
		Statement personStatement = null;
		ResultSet propertySet = null;
		ResultSet personData = null;
		Result propSet = null;
		Result pData = null;
		
		StringBuilder messages = new StringBuilder();
		StringBuilder errors = new StringBuilder();
		
		try {
			connection = DBConnection.getConnection();
			propStatement = connection.createStatement();
			propertySet = propStatement.executeQuery("SELECT property.*, property_group.name AS group_name FROM property, property_group WHERE property_group = property_group_id ORDER BY (property_group.name != 'Basic Data'), property_group.name");
			propSet = ResultSupport.toResult(propertySet);
			personStatement = connection.createStatement();
			
			connection.setAutoCommit(false);
			
			if ("Save".equals(request.getParameter("save"))) {
				if ( personId == 0) {
					synchronized (this) {
						Statement insertStatement = connection.createStatement();
						insertStatement.executeUpdate("INSERT INTO person (deleted) VALUES (true)");
						
						Statement idStatement = connection.createStatement();
						ResultSet idResult = insertStatement.executeQuery("SELECT MAX(person_id) AS person_id FROM person");
						
						if (!idResult.next()) {
							errors.append("The database is empty.");
						}
						else {
							personId = idResult.getInt("person_id");
						}
					}
				}
				
				StringBuilder sql = new StringBuilder();
				sql.append("UPDATE person SET ");
				
				boolean first = true;
				for (SortedMap<String, Object> row : propSet.getRows()) {
					String parameterName = (String) row.get("db_name");
					String parameterType = (String) row.get("type");
					String parameterValue = request.getParameter(parameterName);
					if (parameterValue == null && (boolean) row.get("required")) {
						errors.append("Error: property should not be empty.");
						connection.rollback();
						
						throw new SQLException();
					}
					if (parameterValue != null && parameterValue != "") {
						parameterValue = DBConnection.dbEscape(parameterValue);
						if (!first) {
							sql.append(", ");
						}
						else {
							first = false;
						}
						if (!new InputValidator().validateInput(parameterValue, parameterType)) {
							// rollback operation
							errors.append("Invalid " + parameterType + " format: " + parameterValue);
							connection.rollback();
							
							throw new SQLException();
						}
						
						sql.append(parameterName).append("='").append(parameterValue).append("'");
					}
				}
				// end of validation
				sql.append(" WHERE person_id=");
				sql.append(personId);
				
				Statement updateStatement = connection.createStatement();
				updateStatement.executeUpdate(sql.toString());
				
				messages.append("Dataset saved.");
			}
			
			if (request.getParameter("person_id") != null) {
				personData = personStatement.executeQuery("SELECT * FROM person WHERE person_id = " + personId);
				pData = ResultSupport.toResult(personData);
			}
			
			if (errors.length() == 0) {
				connection.commit();
			}
			
		}
		catch (SQLException | ClassNotFoundException exc) {
			errors.append("Error during update: " + exc.getMessage());
		}
		finally {
			request.setAttribute("message", messages.toString());
			request.setAttribute("error", errors.toString());
			request.setAttribute("propertySet", propSet);
			request.setAttribute("personData", pData);
			request.setAttribute("person_id", personId + "");
			
			getServletContext().getRequestDispatcher("/editPerson.jsp").forward(request, response);
			
			if (connection != null) try { connection.setAutoCommit(false); } catch (SQLException exc) {}
	        if (propStatement != null) try { propStatement.close(); } catch (SQLException exc) {}
	        if (personStatement != null) try { personStatement.close(); } catch (SQLException exc) {}
	        if (propertySet != null) try { propertySet.close(); } catch (SQLException exc) {}
	        if (personData != null) try { personData.close(); } catch (SQLException exc) {}
	        if (connection != null) try { connection.close(); } catch (SQLException exc) {}
		}
	}

}
