package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.sql.Result;
import javax.servlet.jsp.jstl.sql.ResultSupport;

import helpers.DBConnection;

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
			propertySet = propStatement.executeQuery("SELECT property.*, property_group.name AS group_name FROM property, property_group WHERE property_group = property_group_id ORDER BY (property_group.name != 'Basic Data'), property_group.name");
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
		
		Statement personStatement = null;
		Statement insertStatement = null;
		Statement idStatement = null;
		Statement updateStatement = null;
		ResultSet idResult = null;
		ResultSet personData = null;
		Result pData = null;
		int personId = 0;
		Statement propStatement = null;
		ResultSet propertySet = null;
		Result propSet = null;
		
		try (Connection connection = DBConnection.getConnection()) {
			personId = (request.getParameter("person_id") != null && !"".equals(request.getParameter("person_id"))) ? Integer.parseInt(request.getParameter("person_id")) : 0;
			
			if ("Save".equals(request.getParameter("save"))) {
				if ( personId == 0) {
					synchronized (this) {
						insertStatement = connection.createStatement();
						insertStatement.executeUpdate("INSERT INTO person (deleted) VALUES (true)");
						
						idStatement = connection.createStatement();
						idResult = insertStatement.executeQuery("SELECT MAX(person_id) AS person_id FROM person");
						
						if (!idResult.next()) {
							errors.append("Database empty!?");
						}
						else {
							personId = idResult.getInt("person_id");
						}
					}
				}
				
				StringBuilder sql = new StringBuilder();
				sql.append("UPDATE person SET ");
				
				Enumeration<String> parameterNames = request.getParameterNames();
				boolean first = true;
				while (parameterNames.hasMoreElements()) {
					String parameterName = parameterNames.nextElement();
					if (!"save".equals(parameterName) && !"person_id".equals(parameterName)) {
						if (!first) {
							sql.append(", ");
						}
						else {
							first = false;
						}
						sql.append(parameterName);
						sql.append("='");
						sql.append(request.getParameter(parameterName));
						sql.append("'");
					}
				}
				
				sql.append(" WHERE person_id=");
				sql.append(personId);
				
				updateStatement = connection.createStatement();
				updateStatement.executeUpdate(sql.toString());
				
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
				};
			}
			
			propStatement = connection.createStatement();
			propertySet = propStatement.executeQuery("SELECT property.*, property_group.name AS group_name FROM property, property_group WHERE property_group = property_group_id ORDER BY (property_group.name != 'Basic Data'), property_group.name");
			propSet  = ResultSupport.toResult(propertySet);
		}
		catch (SQLException | ClassNotFoundException exc) {
			errors.append("An error occured while attempting to create new user: " + exc.getMessage());
		}
		finally {
			request.setAttribute("message", messages.toString());
			request.setAttribute("error", errors.toString());
			request.setAttribute("personData", pData);
			request.setAttribute("person_id", personId);
			request.setAttribute("propertySet", propSet);
			
			getServletContext().getRequestDispatcher("/newPersonResult.jsp").forward(request, response);
			
			if (personStatement != null) try { personStatement.close(); } catch (SQLException exc) {}
			if (insertStatement != null) try { insertStatement.close(); } catch (SQLException exc) {}
			if (idStatement != null) try { idStatement.close(); } catch (SQLException exc) {}
			if (updateStatement != null) try { updateStatement.close(); } catch (SQLException exc) {}
			if (idResult != null) try { idResult.close(); } catch (SQLException exc) {}
			if (personData != null) try { personData.close(); } catch (SQLException exc) {}
			if (propStatement != null) try { propStatement.close(); } catch (SQLException exc) {}
			if (propertySet != null) try { propertySet.close(); } catch (SQLException exc) {}
		}
	}
}
