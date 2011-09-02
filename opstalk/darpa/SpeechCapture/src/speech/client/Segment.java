package speech.client;
import android.location.Location;
public class Segment {
	public String fileName;
	public long timeStamp;
	public Location location;
	public int isSpeech; //1 speech, 0 non-speech
	public Segment(String fName,long time, Location loc,int isSpeech){
		this.fileName=fName;
		this.timeStamp=time;
		this.location=loc;
		this.isSpeech=isSpeech;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
