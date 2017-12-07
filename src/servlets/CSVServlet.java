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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import helpers.DBConnection;

@WebServlet("/CSVServlet")
public class CSVServlet extends HttpServlet {

	private static final long serialVersionUID = 5926838569460262388L;

	public void init() {
		ServletContext context = getServletContext();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String sql = request.getParameter("export_csv");
		
		if (sql != null) {
			String[] toExport = request.getParameterValues("toExport");
			String[] toChoose = request.getParameterValues("toChoose");
			sql = buildExportQuery(sql, toExport, toChoose);
			exportResults(sql, response);
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
		
		sql.append("SELECT ");
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
