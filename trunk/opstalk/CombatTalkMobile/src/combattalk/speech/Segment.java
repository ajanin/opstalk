package combattalk.speech;
import android.location.Location;
public class Segment {
	public short[] buffer;
	public int length;
	public long timeStamp;
	public Location location;
	public int isSpeech; //1 speech, 0 non-speech
	public Segment(short[] buffer,int length,long time, Location loc,int isSpeech){
		this.buffer=buffer;
		this.length=length;
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
