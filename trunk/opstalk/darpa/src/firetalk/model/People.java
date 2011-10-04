package firetalk.model;

import java.util.LinkedList;

import firetalk.db.Repository;

public class People {
	private String id; // soldier id;
	private String name;
	private String first;
	private String last;
	private String Level="3";  //default level is lowest level
	private String teamID;
	private LinkedList<Location> history = new LinkedList<Location>();
	private boolean selected = false;
	private final static int history_len = 200;

	public String getRandName() {
		if (Level.equalsIgnoreCase("1"))
			return "PL " + id;
		else if (Level.equalsIgnoreCase("2"))
			return "SQ " + id;
		else
			return "FT " + id;

	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public People() {
	}

	public String getTeamID() {
		return teamID;
	}

	public void setTeamID(String teamID) {
		this.teamID = teamID;
	}

	public People(String id, String level, String first, String last,
			String teamID) {
		this.id = id;
		this.first = first;
		this.last = last;
		this.name = first + " " + last;
		this.Level = level;
		this.teamID = teamID;

	}

	@Override
	public String toString() {
		String text = "ID: " + id + "\nTeam Name: "
				+ Repository.teamList.get(teamID).teamName + "\nRank: "
				+ this.Level + "\nName: " + name + "\nLocation: ";
		Location loc = this.getLocation();
		if (loc == null)
			text += "not available";
		else
			text += "<" + loc.lat + "," + loc.lon + ">";

		return text;
	}

	public double getSpeed() {
		return history.getFirst().speed;
	}

	public double getDirection() {
		return history.getFirst().direction;
	}

	// up to when the server has receive data from this people
	private long serverTransTime = 0;

	public long getServerTransTime() {
		return serverTransTime;
	}

	public void setServerTransTime(long serverTransTime) {
		this.serverTransTime = serverTransTime;
	}

	public void addLocation(double lon, double lat, double speed,
			double direction) {
		Location l = new Location(lat, lon, speed, direction);
		history.addFirst(l);
		if (history.size() > history_len)
			history.removeLast();
	}

	public LinkedList<Location> getHistory() {
		return history;
	}

	// public void setHistory(LinkedList<Location> history) {
	// this.history = history;
	// }

	public double getLatitude() {
		return history.getFirst().lat;
	}

	public double getLongitude() {
		if (history.isEmpty())
			return 0;
		return history.getFirst().lon;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLevel(String level) {
		this.Level = level;
	}

	public String getLevel() {
		return Level;
	}

	public Location getLocation() {
		// TODO Auto-generated method stub
		if (history.isEmpty())
			return null;
		else
			return history.getFirst();
	}

}
