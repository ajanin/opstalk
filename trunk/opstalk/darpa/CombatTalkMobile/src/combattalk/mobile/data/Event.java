package combattalk.mobile.data;

import java.util.Date;

import android.location.Location;

public class Event {
	private long validTime;
	private long transTime;
	private double latitude;
	private double longitude;
	private int eventType;
	private byte[] content;
	public static final int DUMMY=0;
	public static final int MESSAGE=1;
	public static final int QUERY=2;
	public static final int AUDIO=3;
	public static final int LOCATION=4;
	public static final int CONTEXT=5;
	public static final int IMAGE=6;
	public static final int QUERY_RESULT=7;
	public Event(int type){
		this.eventType=type;
	}
	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}
	public int getEventType() {
		return eventType;
	}
	public void setEventType(int eventType) {
		this.eventType = eventType;
	}
	
	public Event(int type,long transTime,double lat,double lon){
		this.eventType=type;
		this.latitude=lat;
		this.longitude=lon;
		this.transTime=transTime;
		this.validTime=(new Date()).getTime();	
	}
	public long getValidTime() {
		return validTime;
	}
	public void setValidTime(long validTime) {
		this.validTime = validTime;
	}
	public long getTransTime() {
		return transTime;
	}
	public void setTransTime(long transTime) {
		this.transTime = transTime;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}


}
