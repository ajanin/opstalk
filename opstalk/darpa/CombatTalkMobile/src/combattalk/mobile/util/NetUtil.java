package combattalk.mobile.util;

import java.io.IOException;
import java.io.InputStream;

public class NetUtil {
	public static final char delimiter='#'; 
	public static double readValue(InputStream is, int len) throws IOException {
		byte[] lenb = new byte[len];
		int off=0;
		while(off<len){
			int nread=is.read(lenb, off, len-off);
			off+=nread;
		}
		String lenStr = "";
		for (int i = 0; i < len; i++) {
			if ((char) lenb[i] == delimiter)
				break;
			lenStr += (char) lenb[i];
		}
		return Double.parseDouble(lenStr);
	}

	public static String readString(InputStream is, int len) throws IOException {
		byte[] lenb = new byte[len];
		int off=0;
		while(off<len){
			int nread=is.read(lenb, off, len-off);
			off+=nread;
		}
		String lenStr = "";
		for (int i = 0; i < len; i++) {
			if ((char) lenb[i] == delimiter)
				break;
			lenStr += (char) lenb[i];
		}
		return lenStr;
	}

	public static  byte[] string2bytes(String str, int len) {
		byte[] lenb = new byte[len];
		if (str != null && str.length() <= len) {
			for (int i = 0; i < len; i++) {
				if (i < str.length())
					lenb[i] = (byte) str.charAt(i);
				else
					lenb[i] = (byte) delimiter;
			}
		}
		return lenb;
	}

	public static byte[] value2bytes(double v, int len) {
		String lenStr = Double.toString(v);
		if (lenStr.length() > len)
			return null;
		byte[] lenb = new byte[len];
		for (int i = 0; i < len; i++) {
			if (i < lenStr.length())
				lenb[i] = (byte) lenStr.charAt(i);
			else
				lenb[i] = (byte) delimiter;
		}
		return lenb;
	}
	public static byte[] readBytes(InputStream is, int len) throws IOException {
		
		byte[] lenb = null;
		if (len > 0) {
			lenb = new byte[len];
			int off=0;
			while(off<len){
				int nread=is.read(lenb, off, len-off);
				off+=nread;
			}
		}
		return lenb;
	}
}
