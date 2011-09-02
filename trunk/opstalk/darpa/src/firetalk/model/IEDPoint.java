package firetalk.model;

public class IEDPoint {
	private String mes;
	private double latitude;
	private double longitude;
	private String userId;  // people who left the message
	private long validTime;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public IEDPoint(String id, String mes, long time, double lat,
			double lon) {
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

	@Override
	public String toString() {
		String text = "People ID: " + userId
				 + "\nLocation: <" + latitude + "," + longitude + ">\n-----------------------\nMessage: "+mes;
		return text;
	}
}
