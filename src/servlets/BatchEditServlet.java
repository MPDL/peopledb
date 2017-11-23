package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.sql.Result;
import javax.servlet.jsp.jstl.sql.ResultSupport;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import helpers.DBConnection;

@WebServlet("/BatchEditServlet")
public class BatchEditServlet extends HttpServlet {

	private static final long serialVersionUID = -5322889347153667343L;

	public void init() {
		ServletContext context = getServletContext();
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String[] checkValues = request.getParameterValues("toChoose");
		int widgets = Integer.parseInt(request.getParameter("editSelected"));
		
		StringBuilder messages = new StringBuilder();
		StringBuilder errors = new StringBuilder();
		
		ResultSet propertySet = null;
		ResultSet resultData = null;
		Statement personStatement = null;
		Statement propStatement = null;
				
		Map<String, List<Triple<String, String, String>>> propertyMap = null;
		Result result = null;
		
		try (Connection connection = DBConnection.getConnection()) {
			if (checkValues == null) {
				errors.append("No entries were selected!");
			}
			else {
				StringBuilder selectedIDs = new StringBuilder("(");
				for (String value : checkValues) {
					selectedIDs.append(value.replace("person", ""));
					selectedIDs.append(",");
				}
				selectedIDs.setCharAt(selectedIDs.length() - 1, ')');
			
				personStatement = connection.createStatement();
				resultData = personStatement.executeQuery("SELECT person_id, basic_data_given_name, basic_data_family_name, basic_data_email FROM person WHERE person.person_id IN " + selectedIDs.toString());
				
				propStatement = connection.createStatement();
				propertySet = propStatement.executeQuery("SELECT property.*, property_group.name AS group_name FROM property, property_group WHERE property_group = property_group_id ORDER BY (property_group.name != 'Basic Data'), property_group.name");
			
				propertyMap = new LinkedHashMap<>();
				
				while (propertySet.next()) {
					String groupName = propertySet.getString("group_name");
					if (!propertyMap.containsKey(groupName)) {
						propertyMap.put(groupName, new LinkedList<Triple<String, String, String>>());
					}
					String dbName = propertySet.getString("db_name");
					String name = propertySet.getString("name");
					String type = propertySet.getString("type");
					
					propertyMap.get(groupName).add(new ImmutableTriple<String, String, String>(dbName, type, name));
				}
				
				result = ResultSupport.toResult(resultData);
			}
		}
		catch (SQLException | ClassNotFoundException exc) {
			errors.append(exc.getMessage());
		}
		finally {
			request.setAttribute("message", messages.toString());
			request.setAttribute("error", errors.toString());
			request.setAttribute("current_query", request.getParameter("current_query"));
			request.setAttribute("result", result);
			request.setAttribute("map", propertyMap);
			request.setAttribute("widgets", widgets);
			
			getServletContext().getRequestDispatcher("/batchEdit.jsp").forward(request, response);
			
			if (propertySet != null) try { propertySet.close(); } catch (SQLException exc) {}
			if (resultData != null) try { resultData.close(); } catch (SQLException exc) {}
	        if (personStatement != null) try { personStatement.close(); } catch (SQLException exc) {}
	        if (propStatement != null) try { propStatement.close(); } catch (SQLException exc) {}
		}
	}
}
