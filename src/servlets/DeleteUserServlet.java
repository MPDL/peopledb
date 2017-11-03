package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import helpers.DBConnection;

@WebServlet("/DeleteUserServlet")
public class DeleteUserServlet extends HttpServlet {

	private static final long serialVersionUID = -5665356949913785052L;

	public void init() {
		ServletContext context = getServletContext();
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String deletedID = request.getParameter("delete_user");
		
		StringBuilder messages = new StringBuilder();
		StringBuilder errors = new StringBuilder();
		
		Statement statement = null;
		
		try (Connection connection = DBConnection.getConnection()) {
			if (deletedID == null) {
				errors.append("An error occured during user retrieval.");
				System.err.println("No index is available: check parameter passing if delete_user was passed correctly.");
			}
			else {
				synchronized(this) {
					statement = connection.createStatement();
					statement.executeUpdate("UPDATE person SET deleted = TRUE WHERE person_id = " + deletedID);
					messages.append("The user was successfully deleted.");
				}
			}
		}
		catch (SQLException | ClassNotFoundException exc) {
			errors.append("An error occured during delete: ").append(exc.getMessage());
			exc.printStackTrace();
		}
		finally {
			request.setAttribute("message", messages.toString());
			request.setAttribute("error", errors.toString());
			
			getServletContext().getRequestDispatcher("/deletedUser.jsp").forward(request, response);
			
	        if (statement != null) try { statement.close(); } catch (SQLException exc) {}
		}
	}
}
