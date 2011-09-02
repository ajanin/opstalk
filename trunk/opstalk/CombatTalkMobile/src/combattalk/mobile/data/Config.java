package combattalk.mobile.data;

import java.util.HashMap;

import combattalk.mobile.data.People.Position;

import android.R;
import android.graphics.Color;

/**
 * @author jeffrey Config Class: store parameters
 */
public class Config {
	static HashMap<People.Position, Integer> positionColor = new HashMap();
	public static String[] Directions = new String[] { "east", "northeast", "north",
			"northwest", "west", "southwest", "south", "southeast" };

	public static void setColor(People.Position pos, int color) {
		positionColor.put(pos, color);
	}

	public static int getColor(People.Position pos) {
		Integer color = positionColor.get(pos);
		if (color == null)
			color = Color.BLACK;
		return color;
	}

	public static void defaultConfig() {
		setColor(Position.Soldier, Color.BLUE);
		setColor(Position.SquadLeader, Color.RED);
		setColor(Position.TeamLeader, Color.GREEN);
	}
}
