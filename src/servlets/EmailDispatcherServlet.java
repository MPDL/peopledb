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

import org.codehaus.plexus.util.ExceptionUtils;

import helpers.EmailProvider;

@WebServlet("/EmailDispatcherServlet")
public class EmailDispatcherServlet extends HttpServlet {

	private static final long serialVersionUID = 8367164284034569433L;

	String senderEmail = "mpdl_dbapp@mpdl.mpg.de";
	String host = System.getProperty("mailHost");
	String port = "25";
	String username;
	String password;
	LinkedList<String> recipients = new LinkedList<>();
	
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
			EmailProvider.sendEmail(host, port, senderEmail, "", "", csRecipients.toString(), subject, content);
			messages.append("Email was sent successfully");
		}
		catch (MessagingException exc) {
			errors.append("An error occured while sending message: " + exc.getMessage());
			exc.printStackTrace();
		}
		catch (Exception exc) {
			errors.append("An unexpected error occured while sending the message: " + ExceptionUtils.getFullStackTrace(exc));
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
