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
import javax.servlet.jsp.jstl.sql.Result;
import javax.servlet.jsp.jstl.sql.ResultSupport;

import org.apache.commons.lang3.StringUtils;

import helpers.DBConnection;

@WebServlet("/NewPropertyServlet")
public class NewPropertyServlet extends HttpServlet {
	
	private static final long serialVersionUID = -5342060432234643990L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringBuilder messages = new StringBuilder();
		StringBuilder errors = new StringBuilder();
		
		Statement propGroupStatement = null;
		ResultSet propertyGroupSet = null;
		Result propGroupSet = null;
		
		try (Connection connection = DBConnection.getConnection()) {
			propGroupStatement = connection.createStatement();
			propertyGroupSet = propGroupStatement.executeQuery("SELECT * FROM property_group  ORDER BY (property_group.name != 'Basic Data'), name");
			propGroupSet  = ResultSupport.toResult(propertyGroupSet);
		}
		catch (SQLException | ClassNotFoundException exc) {
			errors.append("An error occurred while fetching property groups from database: " + exc.getMessage());
		}
		finally {
			request.setAttribute("message", messages.toString());
			request.setAttribute("error", errors.toString());
			request.setAttribute("propertyGroupSet", propGroupSet);
			
			getServletContext().getRequestDispatcher("/newProperty.jsp").forward(request, response);
			
			if (propGroupStatement != null) try { propGroupStatement.close(); } catch (SQLException exc) {}
			if (propertyGroupSet != null) try { propertyGroupSet.close(); } catch (SQLException exc) {}
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringBuilder messages = new StringBuilder();
		StringBuilder errors = new StringBuilder();
		
		int pGroup = 0;
		Statement testPropStatement = null;
		ResultSet testPropSet = null;
		Statement propGroupStatement2 = null;
		ResultSet propertyGroupSet2 = null;
		Statement createStatement = null;
		Statement alterStatement = null;
		
		Statement propGroupStatement = null;
		ResultSet propertyGroupSet = null;
		Result propGroupSet = null;
		
		try (Connection connection = DBConnection.getConnection()) {
			if ("Save".equals(request.getParameter("save"))) {
				pGroup = Integer.parseInt(request.getParameter("property_group"));
				String pName = request.getParameter("property_name");
				validatePropertyName(pName);
				
				testPropStatement = connection.createStatement();
	
				testPropSet = testPropStatement.executeQuery("SELECT * FROM property WHERE property.name ILIKE '" + DBConnection.dbEscape(pName) + "' AND property_group = " + pGroup);
				if (testPropSet.next()) {
					errors.append("Property '" + pName + "' already exists in this group");
				}
				else {
					synchronized(this) {
						String dbName = DBConnection.toDbName(pName);
						propGroupStatement2 = connection.createStatement();
						propertyGroupSet2 = propGroupStatement2.executeQuery("SELECT db_name FROM property_group WHERE property_group_id = " + pGroup);
						if (propertyGroupSet2.next()) {
							dbName = propertyGroupSet2.getString("db_name") + "_" + dbName;
							
							String type, inputType;
							switch (request.getParameter("data_type")) {
								case "varchar":
									type = " character varying DEFAULT ''";
									inputType = "text";
									break;
								case "decimal":
									type = " double precision";
									inputType = "decimal";
									break;
								case "integer":
									type = " integer";
									inputType = "number";
									break;
								case "boolean":
									type = " boolean";
									inputType = "boolean";
									break;
								case "email":
									type = " email";
									inputType = "email";
									break;
								case "date":
									type = " date";
									inputType = "date";
									break;
								default:
									type = " character varying DEFAULT ''";
									inputType = "text";
									break;
							}
							
							String required;
							String parRequired = request.getParameter("prop_required");
							if (parRequired != null && StringUtils.equalsIgnoreCase(parRequired, "true")) {
								required = "TRUE";
							}
							else {
								required = "FALSE";
							}
							
							createStatement = connection.createStatement();
							createStatement.executeUpdate("INSERT INTO property (property_group, db_name, name, type, required) VALUES (" + pGroup + ", '" + dbName + "', '" + pName + "', '" + inputType + "', '" + required + "')");
							
							alterStatement = connection.createStatement();
							alterStatement.executeUpdate("ALTER TABLE person ADD COLUMN " + dbName + type); // + default
							
							messages.append("Property " + pName + " created.");
						}
						else {
							errors.append("Property group " + pGroup + " not found!?");
						}
					}
				}
			}
			propGroupStatement = connection.createStatement();
			propertyGroupSet = propGroupStatement.executeQuery("SELECT * FROM property_group  ORDER BY (property_group.name != 'Basic Data'), name");
			propGroupSet  = ResultSupport.toResult(propertyGroupSet);
		}
		catch (SQLException | ClassNotFoundException exc) {
			errors.append("An error occured while adding property to database: " + exc.getMessage());
		}
		finally {
			request.setAttribute("message", messages.toString());
			request.setAttribute("error", errors.toString());
			request.setAttribute("propertyGroupSet", propGroupSet);
			
			getServletContext().getRequestDispatcher("/newProperty.jsp").forward(request, response);
			
			if (testPropStatement != null) try { testPropStatement.close(); } catch (SQLException exc) {}
			if (propGroupStatement2 != null) try { propGroupStatement2.close(); } catch (SQLException exc) {}
			if (createStatement != null) try { createStatement.close(); } catch (SQLException exc) {}
			if (alterStatement != null) try { alterStatement.close(); } catch (SQLException exc) {}
			if (testPropSet != null) try { testPropSet.close(); } catch (SQLException exc) {}
			if (propertyGroupSet2 != null) try { propertyGroupSet2.close(); } catch (SQLException exc) {}
			if (propGroupStatement != null) try { propGroupStatement.close(); } catch (SQLException exc) {}
			if (propertyGroupSet != null) try { propertyGroupSet.close(); } catch (SQLException exc) {}
		}
	}
	
	private void validatePropertyName(String pName) throws SQLException {
		if (StringUtils.isNumeric(pName)) {
			throw new SQLException("Property name cannot contain only numeric characters.");
		}
		if (!StringUtils.isAlphanumericSpace(pName)) {
			throw new SQLException("Invalid character in property name.");
		}
	}
}
