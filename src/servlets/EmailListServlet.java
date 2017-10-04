package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.sql.Result;
import javax.servlet.jsp.jstl.sql.ResultSupport;

import helpers.DBConnection;

@WebServlet("/EmailListServlet")
public class EmailListServlet extends HttpServlet {

	private static final long serialVersionUID = 7222505240072999734L;

	public void init() {
		ServletContext context = getServletContext();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringBuilder messages = new StringBuilder();
		StringBuilder errors = new StringBuilder();
		
		String[] checkValues = request.getParameterValues("toChoose");
		
		Statement propStatement = null;
		ResultSet resultData = null;
		Result rData = null;
		
		try (Connection connection = DBConnection.getConnection()) {
			if (checkValues == null) {
				errors.append("No names were selected!");
			}
			else {
				StringBuilder selectedIDs = new StringBuilder("(");
				for (String value : checkValues) {
					selectedIDs.append(value.replace("person", ""));
					selectedIDs.append(",");
				}
				selectedIDs.setCharAt(selectedIDs.length() - 1, ')');
			
				propStatement = connection.createStatement();
				resultData = propStatement.executeQuery("SELECT basic_data_given_name, basic_data_family_name, basic_data_email FROM person WHERE person.person_id IN " + selectedIDs.toString());
				rData = ResultSupport.toResult(resultData);
			}
		}
		catch(SQLException | ClassNotFoundException exc) {
			errors.append("Error while loading recipients: " + exc.toString());
		}
		finally {
			request.setAttribute("message", messages.toString());
			request.setAttribute("error", errors.toString());
			request.setAttribute("resultData", rData);
			
			getServletContext().getRequestDispatcher("/emailList.jsp").forward(request, response);
			
			if (resultData != null) try { resultData.close(); } catch (SQLException exc) {}
	        if (propStatement != null) try { propStatement.close(); } catch (SQLException exc) {}
		}
	}
}
