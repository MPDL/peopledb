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

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import helpers.DBConnection;

@WebServlet("/AdvancedSearchServlet")
public class AdvancedSearchServlet extends HttpServlet {

	private static final long serialVersionUID = 3374404167237934719L;

	public void init() {
		ServletContext context = getServletContext();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringBuilder errors = new StringBuilder();
		StringBuilder messages = new StringBuilder();
		int widgets = 1;
		
		Statement propStatement = null;
		ResultSet propertySet = null;
		Map<String, List<Triple<String, String, String>>> propertyMap = null;
		
		try (Connection connection = DBConnection.getConnection()) {
			if (request.getParameter("more_criteria") != null) {
				widgets = Integer.parseInt(request.getParameter("more_criteria"));
			}
			
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
			
		}
		catch (SQLException | ClassNotFoundException exc) {
			errors.append("An error occured while rendering this page: " + exc.getMessage());
			exc.printStackTrace();
		}
		finally {
			request.setAttribute("message", messages.toString());
			request.setAttribute("error", errors.toString());
			request.setAttribute("widgets", widgets);
			request.setAttribute("map", propertyMap);
			for (int i = 1; i <= widgets; i++) {
				request.setAttribute("property" + i, request.getParameter("property" + i));
			}
			
			getServletContext().getRequestDispatcher("/advancedSearch.jsp").forward(request, response);
			
			if (propertySet != null) try { propertySet.close(); } catch (SQLException exc) {}
	        if (propStatement != null) try { propStatement.close(); } catch (SQLException exc) {}
		}
	}
}
