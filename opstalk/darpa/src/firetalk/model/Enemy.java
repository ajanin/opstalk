package firetalk.model;

import firetalk.db.UIRepository;

public class Enemy {
	private double latitude;
	private double longitude;

	public Enemy(double lat, double lon) {
		this.latitude = lat;
		this.longitude = lon;
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

	public String toString() {
		return String.format("Enemy Location: <%f,%f>", latitude, longitude);
	}
}
