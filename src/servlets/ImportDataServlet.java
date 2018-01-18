package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import helpers.DBConnection;

@WebServlet("/ImportDataServlet")
@MultipartConfig
public class ImportDataServlet extends HttpServlet {

	private static final long serialVersionUID = 7645843132248525337L;

	public void init() {
		ServletContext context = getServletContext();
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringBuilder messages = new StringBuilder();
		StringBuilder errors = new StringBuilder();
		
		String format = request.getParameter("format");
		
		if (format != null && format.equals("csv")) {
			importCSV(request, messages, errors);
		}

		request.setAttribute("message", messages.toString());
		request.setAttribute("error", errors.toString());
		request.getRequestDispatcher("/importResult.jsp").forward(request, response);
	}
	
	private void importCSV(HttpServletRequest request, StringBuilder messages, StringBuilder errors) {
		if (ServletFileUpload.isMultipartContent(request)) {
			try {
				Collection<Part> parts = request.getParts();
				
				if (parts.isEmpty()) {
					throw new IllegalArgumentException("No files were specified.");
				}
				
				long newRows = 0;
				for (Part part : parts) {
					InputStream partInputStream = part.getInputStream();
					String header = new BufferedReader(new InputStreamReader(partInputStream, "UTF-8")).readLine();
					
					// the format dropdown is a Part object itself
					if (!header.equals("csv"))
						newRows += importFileIntoDB(partInputStream, header, errors);
				}
				
				messages.append(newRows + " new " + (newRows == 1 ? "row was" : "rows were") + " imported.");
			}
			catch (Exception exc) {
				errors.append("An error occured during file upload: " + exc.getMessage());
			}
		}
		else {
			errors.append("Inappropriate format for file upload.");
		}
	}
	
	private long importFileIntoDB(InputStream inputStream, String header, StringBuilder errors) {
		Connection connection = null;
		
		try {
			connection = DBConnection.getConnection();
			connection.setAutoCommit(false);
			CopyManager copyManager = new CopyManager((BaseConnection) connection);
			long newRows = copyManager.copyIn("COPY person (" + header + ") FROM STDIN WITH (DELIMITER ',', FORMAT CSV, HEADER TRUE);", inputStream);
			connection.commit();
			return newRows;
		}
		catch (Exception exc) {
			if (connection != null) try { connection.rollback(); } catch (SQLException exp) {}
			errors.append("An error occured while importing file into database: " + exc.getMessage());
			exc.printStackTrace();
			return 0;
		}
		finally {
			if (connection != null) try { connection.setAutoCommit(true); } catch (SQLException exc) {}
			if (connection != null) try { connection.close(); } catch (SQLException exc) {}
		}
	}
}
