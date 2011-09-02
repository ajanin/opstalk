package firetalk.operators.speech;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.mail.MessagingException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Recognizer {
	SendMailTLS sender = new SendMailTLS();
	InboxReader receiver = new InboxReader();

	public String recognize(String filename) {
		sender.send(filename);	
		InboxReader receiver = new InboxReader();
		try {
			String mes = receiver.readNewMail();
			while (mes == null) {
				Thread.sleep(3000);
				mes = receiver.readNewMail();
			}
			StringTokenizer st=new StringTokenizer(mes,"\n");
			while(st.hasMoreTokens()){
				String line=st.nextToken();
				if(line.contains("Trans"))
					break;
			}
			String transcript="";
			while(st.hasMoreTokens())
				transcript+=st.nextToken();
			return transcript;
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	public static void main(String[] args) {
		Recognizer rec=new Recognizer();
		rec.recognize("1300221349.pcm");
	}
}
