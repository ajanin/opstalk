package firetalk.operators.speech;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
 
public class SendMailTLS {
	Session session=null;
	public SendMailTLS(){
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		session = Session.getInstance(props,
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication("firetalk.test","jeffin4@uci");
				}
			});
	}
	public void send(String filename){
		try { 
			if(session==null){
				System.out.println("Email session not ready");
				return;
			}
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("firetalk.test@gmail.com"));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("janin@icsi.berkeley.edu"));
			message.setSubject("RunFireTalkRecognize");
//			message.setText("Dear Mail Crawler," +
//					"\n\n No spam to my email, please!");
			// Create the message part 
			BodyPart messageBodyPart = new MimeBodyPart();

			// Fill the message
			messageBodyPart.setText("raw: true\nsystem: calo");

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			// Part two is attachment
			messageBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource(filename);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(filename);
			multipart.addBodyPart(messageBodyPart);

			// Put parts in message
			message.setContent(multipart);
			Transport.send(message);
 
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
	public static void main(String[] args) {
		
		
	}
}