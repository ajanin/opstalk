package combattalk.sr;

// Note that currently the grammar is fixed, including the people.
// See people.txt and opstalkdemo.jsgf for the order.

public interface SpeechCommandHandler {
	// The integer arguments are the indices (0 based) of the objects. See
	// the grammar for details on order.
	public void whereIsPersonCommand(int person);
	public void whereIsWaypointCommand(int waypoint);
	public void whereIsTeamCommand(int team);
	public void whereIsRallyCommand(int rally);
	public void whereIsObjectiveCommand(int obj);
	public void whoIsNearPersonCommand(int person);
	public void whoIsNearWaypointCommand(int waypoint);
	public void whoIsNearTeamCommand(int team);
	public void whoIsNearRallyCommand(int rally);
	public void whoIsNearObjectiveCommand(int obj);
	public void closestPersonToMeCommand();
	public void closestWaypointToMeCommand();
	public void closestRallyToMeCommand();
	public void closestObjectiveToMeCommand();
	public void whereAmICommand();
	public void sayAgainCommand();
	public void voiceNoteCommand();
	public void enemySpottedCommand();
	
	public void parserError(String msg);
}
