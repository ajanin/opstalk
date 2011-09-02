package combattalk.mobile.data;

public class Message {
	private String mes;
	private double latitude;
	private double longitude;
	private String userId;
	private long validTime;
	private int reachNumber = 0; // how many times this message is reached
	private final int triggerN = 3;
	private boolean isSpoken = false; // whether this mes has been spoken

	public boolean isSpoken() {
		return this.isSpoken;
	}

	public void setSpoken() {
		this.isSpoken = true;
	}

	public void resetReachNumber() {
		reachNumber = 0;
	}

	public boolean increaseAndCheck() {
		if (isSpoken)
			return false;
		reachNumber++;
		if (reachNumber >= triggerN) {
			isSpoken = true;
			resetReachNumber();
			return true;
		} else
			return false;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Message(String id, String mes, long time, double lat, double lon) {
		this.userId = id;
		this.latitude = lat;
		this.longitude = lon;
		this.validTime = time;
		this.mes = mes;
	}

	public long getValidTime() {
		return validTime;
	}

	public void setValidTime(long validTime) {
		this.validTime = validTime;
	}

	public String getMes() {
		return mes;
	}

	public void setMes(String mes) {
		this.mes = mes;
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
