package firetalk.model;

public class RallyPoint extends CheckPoint {
	private String mes = null;

	public RallyPoint(String id, String userID, String name, double lat,
			double lon) {
		super(id, userID, name, lat, lon, false, false, "");
		this.id = id;
		this.userID = userID;
		this.lat = lat;
		this.lon = lon;
	}

	public RallyPoint(String id, String userID, String name, double lat,
			double lon, String mes) {
		super(id, userID, name, lat, lon, false, false, "");
		this.id = id;
		this.userID = userID;
		this.lat = lat;
		this.lon = lon;
		this.mes = mes;
	}
}
