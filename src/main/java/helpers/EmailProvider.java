package helpers;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailProvider {

	public synchronized static void sendEmail(String host, String port, String sender, String username, String password,
			String recipients, String subjectText, String messageText) throws AddressException,
    MessagingException {
		Properties properties = System.getProperties();
		properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.ssl.enable", "true");
        properties.setProperty("mail.smtp.auth", "false");
		properties.setProperty("mail.smtp.from", sender);
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", port);

        Session session = Session.getInstance(properties);
        
		Message message = new MimeMessage(session);
		InternetAddress[] recipientAddresses = InternetAddress.parse(recipients);
		
		message.setFrom(new InternetAddress(sender));
		message.setRecipients(Message.RecipientType.TO, recipientAddresses);
		message.setSubject(subjectText);
		message.setText(messageText);
		message.setSentDate(new Date());
		
		Transport.send(message, recipientAddresses);
	}
}
