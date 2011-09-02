package firetalk.model;

public class Team {

	public String teamID;
	public String teamName;

	public Team(String id, String name) {
		this.teamID = id;
		this.teamName = name;
	}

	@Override
	public String toString() {
		String text = "Team ID: " + teamID + "\nTeam Name: " + teamName;

		return text;
	}

}
