package firetalk.model;

public class Enemy {
	private double latitude;
	private double longitude;
	private double dist;
	private double degree;
	public Enemy(double lat, double lon,double dist,double degree) {
		this.latitude=lat;
		this.longitude=lon;
		this.dist=dist;
		this.degree=degree;
	}
	public double getDist() {
		return dist;
	}
	public void setDist(double dist) {
		this.dist = dist;
	}
	public double getDegree() {
		return degree;
	}
	public void setDegree(double degree) {
		this.degree = degree;
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
