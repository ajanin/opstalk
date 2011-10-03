package combattalk.mobile.data;

import java.util.LinkedList;

import com.google.android.maps.GeoPoint;

import combattalk.mobile.R;

public class People {

	private String id;
	private String teamId;
	private String squadId;
	private String name = "";
	private String rank = "";
	private String lastName = "";
	private String firstName = "";
	private int iconId = R.drawable.soldier;
	private LinkedList<LocationInfo> locations = new LinkedList<LocationInfo>();
	Position position;

	public People(String id, String rank, String firstName, String lastName,
			String termID) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.name = firstName + " " + lastName;
		this.teamId = teamId;
		if (rank.equals("1"))
			this.iconId = R.drawable.squad_leader;
		if (rank.equals("2"))
			this.iconId = R.drawable.team_leader;
		if (rank.equals("3"))
			this.iconId = R.drawable.soldier;
	}

	public GeoPoint getGeoLocation() {
		People.LocationInfo loc = this.getLocation();
		if (loc != null) {
			double lat = loc.latitude;
			double lon = loc.longitude;
			return new GeoPoint((int) (lat * 1E6), (int) (lon * 1E6));
		}
		return null;
	}

	public String getRankName() {
		if (rank.equals("1"))
			return "PL " + id;
		else if (rank.equals("2"))
			return "SQ " + id;
		else
			return "FT " + id;
	}

	public People(String id, String lastName, String firstName, int iconId) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.name = firstName + " " + lastName;
		this.iconId = iconId;
	}

	public String getRank() {
		return rank;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public int getIconId() {
		return iconId;
	}

	public void setIconId(int iconId) {
		this.iconId = iconId;
	}

	public class LocationInfo {
		public double latitude = -1000;
		public double longitude = -1000;
		public double speed;
		public double direction; // angle , from 0-2pi
		public long validTime = 0;

		public LocationInfo(double lat, double lon, double speed,
				double direction) {
			this.latitude = lat;
			this.longitude = lon;
			this.speed = speed;
			this.direction = direction;
			this.validTime = System.currentTimeMillis();
		}
	}

	public enum Position {
		Soldier, TeamLeader, SquadLeader;
	}

	public People() {
	}

	public void addLocation(LocationInfo info) {
		this.locations.addLast(info);
	}

	public LocationInfo getLocation() {
		return locations.isEmpty() ? null : locations.getLast(); // return
		// latest
		// location
		// info
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean hasValidLocation() {
		return getLocation() != null;
	}

	public People(String id, String teamId, String squadId, Position position) {
		this.id = id;
		this.teamId = teamId;
		this.squadId = squadId;
		this.position = position;
	}

	public String getTeamId() {
		return teamId;
	}

	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}

	public String getSquadId() {
		return squadId;
	}

	public void setSquadId(String squadId) {
		this.squadId = squadId;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
