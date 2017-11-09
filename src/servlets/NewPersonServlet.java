package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.SortedMap;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.sql.Result;
import javax.servlet.jsp.jstl.sql.ResultSupport;

import org.apache.commons.lang3.ArrayUtils;

import helpers.DBConnection;
import helpers.InputValidator;

@WebServlet("/NewPersonServlet")
public class NewPersonServlet extends HttpServlet {

	private static final long serialVersionUID = -8631735889250182877L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringBuilder messages = new StringBuilder();
		StringBuilder errors = new StringBuilder();
		
		Statement propStatement = null;
		ResultSet propertySet = null;
		Result propSet = null;
		
		try (Connection connection = DBConnection.getConnection()) {
			propStatement = connection.createStatement();
			propertySet = propStatement.executeQuery("SELECT property.*, property_group.name AS group_name FROM property, property_group WHERE property_group = property_group_id ORDER BY (property_group.name != 'Basic Data'), property_group.name, property.property_id ASC");
			propSet  = ResultSupport.toResult(propertySet);
		}
		catch (SQLException | ClassNotFoundException exc) {
			errors.append("An error occurred while fetching properties from database: " + exc.getMessage());
		}
		finally {
			request.setAttribute("message", messages.toString());
			request.setAttribute("error", errors.toString());
			request.setAttribute("propertySet", propSet);
			
			getServletContext().getRequestDispatcher("/newPerson.jsp").forward(request, response);
			
			if (propStatement != null) try { propStatement.close(); } catch (SQLException exc) {}
			if (propertySet != null) try { propertySet.close(); } catch (SQLException exc) {}
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringBuilder messages = new StringBuilder();
		StringBuilder errors = new StringBuilder();
		
		Connection connection = null;
		Statement personStatement = null, insertStatement = null, idStatement = null, updateStatement = null;
		ResultSet idResult = null, personData = null;
		Result pData = null;
		Statement propStatement = null;
		ResultSet propertySet = null;
		Result propSet = null;
		
		String person_id_string = request.getParameter("person_id");
		int personId = (person_id_string != null && !"".equals(person_id_string)) ? Integer.parseInt(person_id_string) : 0;
		
		// attempts to create user with rollback in case of failure
		try {
			connection = DBConnection.getConnection();
			propStatement = connection.createStatement();
			propertySet = propStatement.executeQuery("SELECT property.*, property_group.name AS group_name FROM property, property_group WHERE property_group = property_group_id ORDER BY (property_group.name != 'Basic Data'), property_group.name");
			propSet  = ResultSupport.toResult(propertySet);
			
			connection.setAutoCommit(false);
			
			if ("Save".equals(request.getParameter("save"))) {
				if (personId == 0) {
					synchronized (this) {
						insertStatement = connection.createStatement();
						insertStatement.executeUpdate("INSERT INTO person (deleted) VALUES (true)");				
						idStatement = connection.createStatement();
						idResult = insertStatement.executeQuery("SELECT MAX(person_id) AS person_id FROM person");
						
						if (!idResult.next()) {
							errors.append("The database is empty.");
						}
						else {
							personId = idResult.getInt("person_id");
						}
					}
				}
				
				StringBuilder sql = new StringBuilder();
				sql.append("UPDATE person SET deleted=FALSE");
				
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
						sql.append(", ");
						if (!new InputValidator().validateInput(parameterValue, parameterType)) {
							// rollback operation
							errors.append("Invalid " + parameterType + " format: " + parameterValue);
							connection.rollback();
							
							throw new SQLException();
						}
						
						sql.append(parameterName).append("='").append(parameterValue).append("'");
					}
				}
				
				sql.append(" WHERE person_id=").append(personId);
				
				if (errors.length() == 0) {
					updateStatement = connection.createStatement();
					updateStatement.executeUpdate(sql.toString());
				}
				messages.append("Dataset saved.");
			}
			
			if (request.getParameter("person_id") != null) {
				personStatement = connection.createStatement();
				personData = personStatement.executeQuery("SELECT * FROM person WHERE person_id = " + personId);
				pData = ResultSupport.toResult(personData);
				if (pData.getRowCount() == 0) {
					errors.append("No person with id " + request.getParameter("person_id"));
					personData.close();
					personData = null;
					connection.rollback();
				};
			}
			
			if (errors.length() == 0) {
				connection.commit();
				PreparedStatement makeNewPersonVisible = connection.prepareStatement("UPDATE person SET deleted=false WHERE person_id=?");
				makeNewPersonVisible.setInt(1, personId);
				makeNewPersonVisible.executeUpdate();
				connection.commit();
			}
		}
		catch (SQLException | ClassNotFoundException exc) {
			if (errors.length() == 0) {
				errors.append("An error occured while attempting to create new user: " + exc.getMessage());
			}
		}
		finally {
			request.setAttribute("message", messages.toString());
			request.setAttribute("error", errors.toString());
			request.setAttribute("personData", pData);
			request.setAttribute("person_id", personId);
			request.setAttribute("propertySet", propSet);
			
			getServletContext().getRequestDispatcher("/newPersonResult.jsp").forward(request, response);
			
			if (connection != null) try { connection.setAutoCommit(true); } catch (SQLException exc) {}
			if (personStatement != null) try { personStatement.close(); } catch (SQLException exc) {}
			if (insertStatement != null) try { insertStatement.close(); } catch (SQLException exc) {}
			if (idStatement != null) try { idStatement.close(); } catch (SQLException exc) {}
			if (updateStatement != null) try { updateStatement.close(); } catch (SQLException exc) {}
			if (idResult != null) try { idResult.close(); } catch (SQLException exc) {}
			if (personData != null) try { personData.close(); } catch (SQLException exc) {}
			if (propStatement != null) try { propStatement.close(); } catch (SQLException exc) {}
			if (propertySet != null) try { propertySet.close(); } catch (SQLException exc) {}
			if (connection != null) try { connection.close(); } catch (SQLException exc) {}
		}
	}
	
	
	
}
