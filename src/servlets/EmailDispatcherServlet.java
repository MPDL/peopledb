package servlets;

import java.io.IOException;
import java.util.LinkedList;

import javax.mail.MessagingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import helpers.EmailProvider;

@WebServlet("/EmailDispatcherServlet")
public class EmailDispatcherServlet extends HttpServlet {

	private static final long serialVersionUID = 8367164284034569433L;

	String senderEmail = "mpdl_employee_database@mpdl.mpg.de";
	String username = "MUCAM\\apetrova";
	String password = System.getProperty("emailpw");
	LinkedList<String> recipients = new LinkedList<>();
	String host = "mail.mucam.mpg.de";
	String port = "25";
	
	public void init() {
		ServletContext context = getServletContext();
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String[] recipient = request.getParameterValues("recipients");
		String subject = request.getParameter("subject");
		String content = request.getParameter("msgBody");
		
		StringBuilder csRecipients = new StringBuilder();
		for (String email : recipient) {
			csRecipients.append(email);
			csRecipients.append(",");
		}
		csRecipients.deleteCharAt(csRecipients.length() - 1);
		
		StringBuilder messages = new StringBuilder();
		StringBuilder errors = new StringBuilder();
		
		try {
			EmailProvider.sendEmail(host, port, senderEmail, username, password, csRecipients.toString(), subject, content);
			messages.append("Email was sent successfully");
		}
		catch (MessagingException exc) {
			errors.append("An error occured while sending message: " + exc.getMessage());
			exc.printStackTrace();
		}
		finally {
			request.setAttribute("message", messages.toString());
			request.setAttribute("error", errors.toString());
			request.setAttribute("subject", subject);
			request.setAttribute("content", content);
			getServletContext().getRequestDispatcher("/emailResult.jsp").forward(request, response);
		}
	}
}
