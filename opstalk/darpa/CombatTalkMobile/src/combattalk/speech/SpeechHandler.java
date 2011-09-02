package combattalk.speech;

import java.util.ArrayList;

import android.location.Location;

import combattalk.mobile.CombatTalkView;
import combattalk.mobile.data.People;
import combattalk.mobile.data.Repository;
import combattalk.mobile.data.People.LocationInfo;
import combattalk.mobile.util.DataUtil;

// Handle simple speech commands. Most of this is to make up for the fact that the Google
// ASR component doesn't handle grammars.
//
// An instance of SpeechHandler should be created with an instance of a class that
// implements CommandHandler and an ArrayList<Person> with all the people.
// doCommand(String) should be called when a new command is heard. SpeechHandler will
// then call back into the CommandHander with the appropriate command
// (e.g. CommandHandler.whereIsCommand(Person); )

public class SpeechHandler implements CommandHandler {
	private ArrayList<People> people=new ArrayList();
	private CommandHandler commandHandler;
	CombatTalkView parent;
	private java.lang.String str;

	public SpeechHandler( CombatTalkView parent) {
		commandHandler =this;
		people.addAll(Repository.peopleList.values());
		this.parent = parent;
	}

	// Used by doCommand to track best command so far.
	// TODO: use some method other than score == 99999 to tell if a command was
	// found.

	private class CommandScore {
		int command; // Should be one of the *_COMMAND constants from
		// CommandHandler
		int score; // 0 - perfect match
		Object data; // Person for WHERE_IS_COMMAND, null for SAY_AGAIN_COMMAND,

		// etc.

		CommandScore() {
			command = 0;
			score = 99999;
			data = null;
		}
	}

	// Try to find something that looks like "command", assuming
	// it may be off because of ASR errors.

	public boolean doCommand(String command) {
		CommandScore bestCommand;
		CommandScore curCommand;

		bestCommand = tryWhereIsCommand(command);

		// Only try others if tryWhereIsCommand wasn't a perfect match.

		if (bestCommand.score > 0) {
			curCommand = trySayAgainCommand(command);
			if (curCommand.score < bestCommand.score) {
				bestCommand = curCommand;
			}

			// Only try others if trySayAgainCommand isn't a perfect match.
			if (bestCommand.score > 0) {
				curCommand = tryWhoIsNearCommand(command);
				if (curCommand.score < bestCommand.score) {
					bestCommand = curCommand;
				}
			}
		}
		if (bestCommand.score < 99999) {
			if (bestCommand.command == CommandHandler.WHERE_IS_COMMAND) {
				commandHandler.whereIsCommand((People) bestCommand.data);
				return true;
			} else if (bestCommand.command == CommandHandler.SAY_AGAIN_COMMAND) {
				commandHandler.sayAgainCommand();
				return true;
			} else if (bestCommand.command == CommandHandler.WHO_IS_NEAR_COMMAND) {
				commandHandler.whoIsNearCommand((People) bestCommand.data);
				return true;
			} else {
				// Unknown command! This shouldn't happen.
				return false;
			}
		}
		return false;
	}

	// Dirt simple Levenshtein distance between two strings. This could
	// obviously be improved (e.g. tokenize words, confusion matrices,
	// etc), but since we're just trying to make up for the fact that
	// Google's ASR doesn't use grammars, it isn't worth it at this time.
	//
	// NOTE: Converts to lower case.

	public static int LevenshteinDistance(String as1, String as2) {
		int ii, jj, v;
		int len1 = as1.length();
		int len2 = as2.length();
		char c1;

		String s1 = as1.toLowerCase();
		String s2 = as2.toLowerCase();

		if (len1 == 0)
			return len2;
		if (len2 == 0)
			return len1;

		int[][] d = new int[len1 + 1][len2 + 1];

		for (ii = 0; ii <= len1; d[ii][0] = ii++) {
		}
		;
		for (jj = 1; jj <= len2; d[0][jj] = jj++) {
		}
		;

		for (ii = 1; ii <= len1; ii++) {
			c1 = s1.charAt(ii - 1);
			for (jj = 1; jj <= len2; jj++) {
				v = d[ii - 1][jj - 1];
				if (s2.charAt(jj - 1) != c1) {
					v++;
				}
				d[ii][jj] = Math.min(Math.min(d[ii - 1][jj] + 1,
						d[ii][jj - 1] + 1), v);
			}
		}
		return d[len1][len2];
	} // LevenshteinDistance()

	public CommandScore trySayAgainCommand(String command) {
		CommandScore bestcommand = new CommandScore();
		bestcommand.command = CommandHandler.SAY_AGAIN_COMMAND;
		int score;

		bestcommand.score = LevenshteinDistance(command, "say that again");
		if (bestcommand.score == 0) {
			return bestcommand;
		}

		score = LevenshteinDistance(command, "say again all after");
		if (score < bestcommand.score) {
			bestcommand.score = score;
		}
		if (score == 0) {
			return bestcommand;
		}

		score = LevenshteinDistance(command, "repeat that");
		if (score < bestcommand.score) {
			bestcommand.score = score;
		}
		if (score == 0) {
			return bestcommand;
		}

		score = LevenshteinDistance(command, "say again");
		if (score < bestcommand.score) {
			bestcommand.score = score;
		}
		if (score == 0) {
			return bestcommand;
		}

		score = LevenshteinDistance(command, "what did you say");
		if (score < bestcommand.score) {
			bestcommand.score = score;
		}

		return bestcommand;
	}

	// Most of this was generated automatically by makehandlers.pl

	public CommandScore tryWhereIsCommand(String command) {
		CommandScore bestcommand = new CommandScore();
		bestcommand.command = CommandHandler.WHERE_IS_COMMAND;
		int score;

		for (People person : people) {

			// Generated file starts here.

			// what is $rank $first $last's location
			score = LevenshteinDistance(command, String.format(
					"what is %s %s %s's location", person.getRank(), person
							.getFirstName(), person.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// what's $rank $first $last's location
			score = LevenshteinDistance(command, String.format(
					"what's %s %s %s's location", person.getRank(), person
							.getFirstName(), person.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// where is $rank $first $last
			score = LevenshteinDistance(command, String.format(
					"where is %s %s %s", person.getRank(), person
							.getFirstName(), person.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// where's $rank $first $last
			score = LevenshteinDistance(command, String.format(
					"where's %s %s %s", person.getRank(),
					person.getFirstName(), person.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// what is $rank $last's location
			score = LevenshteinDistance(command, String.format(
					"what is %s %s's location", person.getRank(), person
							.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// what's $rank $last's location
			score = LevenshteinDistance(command, String.format(
					"what's %s %s's location", person.getRank(), person
							.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// where is $rank $last
			score = LevenshteinDistance(command, String.format(
					"where is %s %s", person.getRank(), person.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// where's $rank $last
			score = LevenshteinDistance(command, String.format("where's %s %s",
					person.getRank(), person.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// what is $last's location
			score = LevenshteinDistance(command, String.format(
					"what is %s's location", person.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// what is $first's location
			score = LevenshteinDistance(command, String.format(
					"what is %s's location", person.getFirstName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// what's $last's location
			score = LevenshteinDistance(command, String.format(
					"what's %s's location", person.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// what's $first's location
			score = LevenshteinDistance(command, String.format(
					"what's %s's location", person.getFirstName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// where is $last
			score = LevenshteinDistance(command, String.format("where is %s",
					person.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// where is $first
			score = LevenshteinDistance(command, String.format("where is %s",
					person.getFirstName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// where's $last
			score = LevenshteinDistance(command, String.format("where's %s",
					person.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// where's $first
			score = LevenshteinDistance(command, String.format("where's %s",
					person.getFirstName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// Generated file ends here.

		}
		return bestcommand;
	} // tryWhereIsCommand()

	public CommandScore tryWhoIsNearCommand(String command) {
		CommandScore bestcommand = new CommandScore();
		bestcommand.command = CommandHandler.WHO_IS_NEAR_COMMAND;
		int score;

		for (People person : people) {

			// Generated file starts here.

			// who is close to $rank $first $last
			score = LevenshteinDistance(command, String.format(
					"who is close to %s %s %s", person.getRank(), person
							.getFirstName(), person.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// who is closest to $rank $first $last
			score = LevenshteinDistance(command, String.format(
					"who is closest to %s %s %s", person.getRank(), person
							.getFirstName(), person.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// who is near $rank $first $last
			score = LevenshteinDistance(command, String.format(
					"who is near %s %s %s", person.getRank(), person
							.getFirstName(), person.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// who is nearby $rank $first $last
			score = LevenshteinDistance(command, String.format(
					"who is nearby %s %s %s", person.getRank(), person
							.getFirstName(), person.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// who is close to $rank $last
			score = LevenshteinDistance(command, String.format(
					"who is close to %s %s", person.getRank(), person
							.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// who is closest to $rank $last
			score = LevenshteinDistance(command, String.format(
					"who is closest to %s %s", person.getRank(), person
							.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// who is near $rank $last
			score = LevenshteinDistance(command, String
					.format("who is near %s %s", person.getRank(), person
							.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// who is nearby $rank $last
			score = LevenshteinDistance(command, String.format(
					"who is nearby %s %s", person.getRank(), person
							.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// who is close to $last
			score = LevenshteinDistance(command, String.format(
					"who is close to %s", person.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// who is close to $first
			score = LevenshteinDistance(command, String.format(
					"who is close to %s", person.getFirstName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// who is closest to $last
			score = LevenshteinDistance(command, String.format(
					"who is closest to %s", person.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// who is closest to $first
			score = LevenshteinDistance(command, String.format(
					"who is closest to %s", person.getFirstName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// who is near $last
			score = LevenshteinDistance(command, String.format(
					"who is near %s", person.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// who is near $first
			score = LevenshteinDistance(command, String.format(
					"who is near %s", person.getFirstName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// who is nearby $last
			score = LevenshteinDistance(command, String.format(
					"who is nearby %s", person.getLastName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// who is nearby $first
			score = LevenshteinDistance(command, String.format(
					"who is nearby %s", person.getFirstName()));
			if (score < bestcommand.score) {
				bestcommand.score = score;
				bestcommand.data = person;
			}
			if (score == 0) {
				return bestcommand;
			}

			// Generated file ends here.

		}
		return bestcommand;
	} // tryWhoIsNearCommand()

	@Override
	public void sayAgainCommand() {
		String str = getOutputText();
    	parent.speak(str);

	}


	@Override
	public void whereIsCommand(People person) {
		Location myLoc = parent.getMyLocationOverlay().getLastFix();
		if (myLoc == null)
			this.setOutput("My location is not available");
		else {
			if(person==null)
				return;
			LocationInfo loc = person.getLocation();
			if (loc == null)
				this.setOutput(String.format(
						"Location of %s %s %s is not available", person
								.getRank(), person.getFirstName(), person
								.getLastName()));
			else {
				double ang = DataUtil.calAngle(myLoc.getLatitude(),myLoc.getLongitude(),loc.latitude,loc.longitude);
				String dir = DataUtil.angle2String(ang);
				int dist = Math.round(DataUtil.calDistance(myLoc.getLatitude(),myLoc.getLongitude(),loc.latitude,loc.longitude));
				String str = String.format("%s %s %s is %d meters %s",
						person.getRank(), person.getFirstName(), person.getLastName(), dist, dir);
				this.setOutput(str);
			}
		}

	}

	@Override
	public void whoIsNearCommand(People person) {
		People bestperson = null;
		double bestdistance = Double.POSITIVE_INFINITY;
		double distance;
		LocationInfo loc = person.getLocation();
		if (loc != null) {
			for (People aperson : people) {
				if (aperson == person) {
					continue;
				}
				LocationInfo aloc = aperson.getLocation();
				if (aloc != null) {
					distance = DataUtil.calDistance(loc.latitude,
							loc.longitude, aloc.latitude, aloc.longitude);
					if (distance < bestdistance) {
						bestdistance = distance;
						bestperson = aperson;
					}
				}
			}
		}
		if (bestperson != null) {
			LocationInfo bestLoc = bestperson.getLocation();
			double ang = DataUtil.calAngle(loc.latitude, loc.longitude,
					bestLoc.latitude, bestLoc.longitude);

			String dir = DataUtil.angle2String(ang);
			String str = String.format("%s %s %s is %d meters to %s of %s %s %s",
					bestperson.getRank(), bestperson.getFirstName(), bestperson
							.getLastName(), (int) Math.round(bestdistance),
					dir, person.getRank(), person.getFirstName(), person
							.getLastName());
			this.setOutput(str);
		}
		else 
			this.setOutput("No people found");

	}

	private String getOutputText() {
		// TODO Auto-generated method stub
		return this.str;
	}
	private void setOutput(String str){
		this.str=str;
		parent.speak(str);
	}
} // class SpeechHandler
