package combattalk.mobile.data;

import java.util.HashMap;
import java.util.LinkedList;

import combattalk.mobile.R;

import android.location.Location;

/**
 * @author jeffrey Repository Class: a mock data storage
 */
public class Repository {
	public static LinkedList<Event> events = new LinkedList<Event>();
	public static LinkedList<Message> messages = new LinkedList<Message>();
	public static HashMap<String, People> peopleList = new HashMap<String, People>();
	public static Location location = null;
	public static HashMap<String, Long> transTime = new HashMap();

	public static void addEvent(Event e) {
		try{
		synchronized (events) {
			events.addFirst(e);
		}
		}catch(Exception ev){
			ev.getCause();
		}
	}

	public static Event getEvent() {
		Event e = null;
		try{
		synchronized (events) {
			if (events.size() != 0)
				e = events.removeLast();
		}}
		catch(Exception ev){
			ev.getCause();
		}
		return e;
	}

	public static void init() {
		peopleList.put("firetalk.t", new People("firetalk.t", "Jeffrey",R.drawable.squad_leader));
		peopleList.put("jiex.cs@gm", new People("jiex.cs@gm", "Tom",R.drawable.soldier));
		peopleList.put("uci.sharad", new People("uci.sharad","Jack",R.drawable.team_leader));
	}
}
