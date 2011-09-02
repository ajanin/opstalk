package firetalk.model;

public class Location {
	public double lat;
	public double lon;
	public double speed;
	public double direction;
	public String peopleId;
	public Location(double lat, double lon, double speed, double direction){
		this.lat=lat;
		this.lon=lon;
		this.speed=speed;
		this.direction=direction;
	}
}
