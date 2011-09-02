package firetalk.operators.speech;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;


public class InboxReader {
	Session session=null;
	public InboxReader() {
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		session = Session.getDefaultInstance(props);//(props, null);
		

	}

	public String readNewMail() throws MessagingException, IOException {
		Store store=null;
		try {
			store = session.getStore("imaps");
			store.connect("imap.gmail.com", "firetalk.test", "jeffin4@uci");
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Folder inbox = store.getFolder("Inbox");
		inbox.open(Folder.READ_WRITE);
		Message messages[] = inbox.search(new FlagTerm(new Flags(
				Flags.Flag.SEEN), false));
		for (Message message : messages) {
			message.setFlag(Flags.Flag.SEEN, true);	
			//long timeStamp=message.getReceivedDate().getTime();
			return (String) message.getContent();
		}
		return null;
	}

	public static void main(String args[]){
		InboxReader reader=new InboxReader();
		try {
			System.out.println(reader.readNewMail());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}