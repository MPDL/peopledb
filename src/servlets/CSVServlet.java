package servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import helpers.DBConnection;

@WebServlet("/CSVServlet")
public class CSVServlet extends HttpServlet {

	private static final long serialVersionUID = 5926838569460262388L;

	public void init() {
		ServletContext context = getServletContext();
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String sql = request.getParameter("export_csv");
		
		if (sql != null) {
			String[] toExport = request.getParameterValues("toExport");
			String[] approved = request.getParameterValues("recipients");
			sql = buildExportQuery(sql, toExport, approved);
			exportResults(sql, response);
		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String[] toChoose = request.getParameterValues("toChoose");
		
		StringBuilder messages = new StringBuilder();
		StringBuilder errors = new StringBuilder();
		
		ResultSet propertySet = null;
		ResultSet resultData = null;
		Statement personStatement = null;
		Statement propStatement = null;
				
		Map<String, List<Triple<String, String, String>>> propertyMap = null;
		Result result = null;
		
		try (Connection connection = DBConnection.getConnection()) {
			if (request.getParameter("export_csv") == null) {
				throw new SQLException("Illegal command for this page.");
			}
			if (toChoose == null) {
				throw new SQLException("No entries were selected!");
			}
			StringBuilder selectedIDs = new StringBuilder("(");
			for (String value : toChoose) {
				selectedIDs.append(value.replace("person", ""));
				selectedIDs.append(",");
			}
			selectedIDs.setCharAt(selectedIDs.length() - 1, ')');
			
			personStatement = connection.createStatement();
			resultData = personStatement.executeQuery("SELECT person_id, basic_data_given_name, basic_data_family_name FROM person WHERE person.person_id IN " + selectedIDs.toString());
				
			propStatement = connection.createStatement();
			propertySet = propStatement.executeQuery(DBConnection.getPropertyQuery());
			
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
		catch (SQLException | ClassNotFoundException exc) {
			errors.append(exc.getMessage());
		}
		finally {
			request.setAttribute("message", messages.toString());
			request.setAttribute("error", errors.toString());
			request.setAttribute("resultData", result);
			request.setAttribute("map", propertyMap);
			request.setAttribute("current_query", request.getParameter("export_csv"));
			
			getServletContext().getRequestDispatcher("/exportCSV.jsp").forward(request, response);
			
			if (propertySet != null) try { propertySet.close(); } catch (SQLException exc) {}
			if (resultData != null) try { resultData.close(); } catch (SQLException exc) {}
	        if (personStatement != null) try { personStatement.close(); } catch (SQLException exc) {}
	        if (propStatement != null) try { propStatement.close(); } catch (SQLException exc) {}
		}
	}
	
	private void exportResults(String sql, HttpServletResponse response) {
		StringBuilder errors = new StringBuilder();
		StringBuilder messages = new StringBuilder();
		
		ResultSet resultData = null;
		Statement searchStatement = null;
		String date = LocalDate.now(ZoneId.of("Europe/Berlin")).toString();
		
		try (Connection connection = DBConnection.getConnection()) {
			searchStatement = connection.createStatement();
			resultData = searchStatement.executeQuery(sql);
			messages.append(sql);
			
			response.setContentType("text/csv; charset=utf-8");
			response.setCharacterEncoding("UTF-8");
		    response.setHeader("Content-Disposition", "attachment; filename=\"peopleDB_export_" + date + ".csv\"");
			OutputStream outStream = response.getOutputStream();
			writeToStream(resultData, outStream);
		}
		catch (Exception exc) {
			errors.append("An error occured during CSV file generation: " + exc);
			exc.printStackTrace();
		}
		finally {
			if (resultData != null) try { resultData.close(); } catch (SQLException exc) {}
	        if (searchStatement != null) try { searchStatement.close(); } catch (SQLException exc) {}
		}
	}
	
	private void writeToStream(ResultSet resultData, OutputStream outStream) {
		try {
			// write file header: database columns
			StringBuilder content = new StringBuilder();
			ResultSetMetaData metadata = resultData.getMetaData();
			int columns = metadata.getColumnCount();
			
			for (int i = 1; i <= columns; i++) {
				String currentName = metadata.getColumnName(i);
				content.append(currentName);
				if (i < columns) {
					content.append(",");
				}
				else {
					content.append("\n");
				}
			}
			
			// write data: database entries
			while (resultData.next()) {
				for (int i = 1; i <= columns; i++) {
					String columnValue = resultData.getString(i);
					if (columnValue != null)
						content.append(columnValue);
					if (i < columns) {
						content.append(",");
					}
					else {
						content.append("\n");
					}
				}
			}
			outStream.write(content.toString().getBytes("UTF-8"));
			outStream.flush();
		} 
		catch (SQLException | IOException e) {
			e.printStackTrace();
		}
		finally {
			if (outStream != null) try { outStream.close(); } catch (IOException exc) {}
			if (resultData != null) try { resultData.close(); } catch (SQLException exc) {}
		}
		
	}
	
	/**
	 *  Export only selected columns
	 */
	private String buildExportQuery(String nested, String[] columns, String[] rows) {
		StringBuilder sql = new StringBuilder();
		String tableAlias = "nestedQuery";
		String selectedIDs = processIDs(rows);
		
		sql.append("SELECT person_id,");
		for (String colName : columns) {
			sql.append(tableAlias).append(".").append(colName);
			sql.append(",");
		}
		sql.delete(sql.length() - 1, sql.length());
		sql.append(" FROM ");
		sql.append("(").append(nested).append(")");
		sql.append(" AS ").append(tableAlias);
		sql.append(" WHERE ").append(tableAlias).append(".person_id IN ").append(selectedIDs);
		return sql.toString();
	}
	
	private String processIDs(String[] rows) {
		StringBuilder result = new StringBuilder();
		result.append("(");
		for (String id : rows) {
			result.append(id.replace("person", "")).append(",");
		}
		return StringUtils.substringBeforeLast(result.toString(), ",") + ")";
	}
}
