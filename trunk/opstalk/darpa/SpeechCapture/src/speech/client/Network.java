package speech.client;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Network {
	Socket s;
	DataOutputStream dos;
	OutputStream out = null;

	public void connect(String user) {
		String ip = "192.168.1.113";
		// String ip= "169.234.133.205";
		int port = 8989;
		try {
			s = new Socket(ip, port);
			out = s.getOutputStream();
			out.write(string2bytes(user, 20));
			// BufferedOutputStream bos = new
			// BufferedOutputStream(s.getOutputStream());
			// DataOutputStream dos=new DataOutputStream(bos);
			// //dos.writeUTF(user);
			// dos.writeInt(12);

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private byte[] string2bytes(String str, int len) {
		if (str.length() > len)
			return null;
		byte[] lenb = new byte[len];
		char empty = ' ';
		for (int i = 0; i < len; i++) {
			if (i < str.length())
				lenb[i] = (byte) str.charAt(i);
			else
				lenb[i] = (byte) empty;
		}
		return lenb;
	}

	private byte[] value2bytes(long v, int len) {
		String lenStr = Long.toString(v);
		if (lenStr.length() > len)
			return null;
		byte[] lenb = new byte[len];
		char empty = ' ';
		for (int i = 0; i < len; i++) {
			if (i < lenStr.length())
				lenb[i] = (byte) lenStr.charAt(i);
			else
				lenb[i] = (byte) empty;
		}
		return lenb;
	}

	public void send(Segment seg) throws IOException {
		File file = new File(seg.fileName);
		if (file.exists()) {
			FileInputStream fis = new FileInputStream(seg.fileName);
			// dos.writeLong(seg.timeStamp);
			// dos.writeLong(file.length());
			byte[] isSpeech = value2bytes(seg.isSpeech, 1);
			out.write(isSpeech);
			if (seg.isSpeech == 1) {
				byte[] lenb = value2bytes(seg.timeStamp, 20);
				out.write(lenb);
				lenb = value2bytes(file.length(), 10);
				out.write(lenb);
				byte[] b = new byte[1024];
				int len;
				while ((len = fis.read(b)) != -1) {
					// dos.write(b, 0, len);
					out.write(b, 0, len);
				}
			}
		} else {
			System.out.println("file not exist");
		}
	}

	public void disconnect() throws IOException {
		dos.close();
		s.close();
	}

}
