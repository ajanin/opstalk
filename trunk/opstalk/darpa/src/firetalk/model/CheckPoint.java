package firetalk.model;

public class CheckPoint {
	public String id;
	public String userID;
	public double lat;
	public double lon;
	private boolean isObj = false;
	public String deadline = null;

	public boolean isObj() {
		return isObj;
	}

	public void setObj(boolean isObj) {
		this.isObj = isObj;
	}

	public CheckPoint(String id, String userID, String name, double lat,
			double lon, boolean isObj, boolean isReached, String deadline) {
		this.id = id;
		this.userID = userID;
		this.lat = lat;
		this.lon = lon;
		this.isObj = isObj;
		this.isReached = isReached;
		this.deadline = deadline;
	}

	private boolean isReached = false;

	public boolean isReached() {
		return isReached;
	}

	public void setReached(boolean reached) {
		this.isReached = reached;
	}

	@Override
	public String toString() {
		String text = "Checkpoint ID: " + id + "\nPeople ID: " + userID
				+ "\nLocation: <" + lat + "," + lon + ">";
		return text;
	}
}
