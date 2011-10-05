package firetalk.model;

import java.util.Date;

public class Event {
	private long validTime;
	private long transTime;
	private double latitude;
	private double longitude;
	private int eventType;
	private byte[] content;
	private String id = null; // id of the device who generate the event

	public String extractInfo() {
		String content = "";
		for (int i = 0; i < this.getContent().length; i++)
			content += (char) this.getContent()[i];
		String mes = null;
		switch (this.getEventType()) {
		case Event.CHECK_REACH:
			mes = String.format("Team %s reached point %s", this.getId(),
					content);
			break;
		case Event.MESSAGE:
			if (content.equalsIgnoreCase("$enemy$"))
				mes = "Enemy spotted";
			else
				mes = " New IED point: " + content;
			break;
		}
		return mes;

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public static final int DUMMY = 0;
	public static final int MESSAGE = 1;
	public static final int QUERY = 2;
	public static final int AUDIO = 3;
	public static final int LOCATION = 4;
	public static final int CONTEXT = 5;
	public static final int IMAGE = 6;
	public static final int QUERY_RESULT = 7;
	public static final int CHECK_REACH = 8;

	public Event(int type) {
		this.eventType = type;
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

	public Event(int type, String id, long validTime, long transTime,
			double lat, double lon) {
		this.eventType = type;
		this.id = id;
		this.latitude = lat;
		this.longitude = lon;
		this.transTime = transTime;
		this.validTime = validTime;
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

	@Override
	public String toString() {
		String line = this.id + ": ";
		switch (this.eventType) {
		case Event.AUDIO:
			line += "audio";
			break;
		case Event.IMAGE:
			line += "image";
			break;
		case Event.LOCATION:
			line += "location";
			break;
		case Event.MESSAGE:
			line += "message";
			break;
		}
		line += " <" + this.latitude + " " + this.longitude + ">\n";
		return line;
	}
}
