package combattalk.mobile.data;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

import combattalk.mobile.R;

import android.content.res.Resources;
import android.location.Location;
import android.os.Environment;

/**
 * @author jeffrey Repository Class: a mock data storage
 */
public class Repository {
	public static LinkedList<Event> events = new LinkedList<Event>();
	public static Vector<Message> messages = new Vector<Message>();
	public static HashMap<String, People> peopleList = new HashMap<String, People>();
	public static Vector<People> peopleVector = new Vector<People>();
	public static Location location = null;
	public static Vector<RallyPoint> rallyList = new Vector<RallyPoint>();
	public static HashMap<String, Long> transTime = new HashMap();
	public static Vector<CheckPoint> checkPoints = new Vector<CheckPoint>();
	// public final static String rootPath = Environment
	// .getExternalStorageDirectory().getAbsolutePath();
	public final static String rootPath = "/sdcard";
	public static String speakLine = null;

	// public final static

	public static void initDB() {
		try{
//		Repository.readMessage();
		Repository.readPeople();
		Repository.readTransTime();
		Repository.readCheckPoints();
		Repository.readRallyPoints();
		}
		catch(Exception e){
			
		}
	}

	public static void storeDB() {
//		Repository.storeMessage();
		Repository.storeTransTime();
		Repository.storeCheckPoints();
		Repository.storeTransTime();
		Repository.storeRallyPoints();
	}

	public static void readRallyPoints() {
		try {
			String fileName = rootPath + "/database/rallypoints.txt";
			Scanner scan = new Scanner(new FileReader(fileName));
			while (scan.hasNext()) {
				String line = scan.nextLine();
				if (!line.startsWith("#")) {
					StringTokenizer st = new StringTokenizer(line, "$");
					String id = st.nextToken();
					double lat = Double.parseDouble(st.nextToken());
					double lon = Double.parseDouble(st.nextToken());
					String isReached = st.nextToken();
					rallyList.add(new RallyPoint(id, "", lat, lon, false,
							isReached.equals("1")));
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void storeRallyPoints() {

		try {
			FileWriter fw = new FileWriter(rootPath + "/database/rallyList.txt");
			fw.write("# id lat lon name\n");
			for (RallyPoint point : rallyList) {
				fw.write(String.format("%s$%f$%f$%s\n", point.id, point.lat,
						point.lon, point.isReached() ? "1" : "0"));
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void readCheckPoints() {
		try {
			String fileName = rootPath + "/database/checkpoints.txt";
			Scanner scan = new Scanner(new FileReader(fileName));
			while (scan.hasNext()) {
				String line = scan.nextLine();
				if (!line.startsWith("#")) {
					StringTokenizer st = new StringTokenizer(line, "$");
					String id = st.nextToken();
					double lat = Double.parseDouble(st.nextToken());
					double lon = Double.parseDouble(st.nextToken());
					String isObj = st.nextToken();
					String isReached = st.nextToken();
					checkPoints.add(new CheckPoint(id, "", lat, lon, isObj
							.equals("1"), isReached.equals("1")));
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void storeCheckPoints() {

		try {
			FileWriter fw = new FileWriter(rootPath
					+ "/database/checkpoints.txt");
			fw.write("# id lat lon name\n");
			for (CheckPoint point : checkPoints) {
				fw.write(String.format("%s$%f$%f$%s$%s\n", point.id,
						point.lat, point.lon, point.isObj() ? "1"
								: "0", point.isReached() ? "1" : "0"));
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void readTransTime() {
		try {
			String fileName = rootPath + "/database/transtime.txt";
			Scanner scan = new Scanner(new FileReader(fileName));
			while (scan.hasNext()) {
				String line = scan.nextLine();
				if (!line.startsWith("#")) {
					StringTokenizer st = new StringTokenizer(line, " \t");
					String id = st.nextToken();
					long transT = Long.parseLong(st.nextToken());
					transTime.put(id, transT);
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void storeTransTime() {
		try {
			FileWriter fw = new FileWriter(rootPath + "/database/transtime.txt");
			fw.write("# userID transTime\n");
			for (String id : transTime.keySet()) {
				fw.write(String.format("%s %s\n", id, "" + transTime.get(id)));
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void readMessage() {
		try {
			String fileName = rootPath + "/database/messages.txt";
			Scanner scan = new Scanner(new FileReader(fileName));
			while (scan.hasNext()) {
				String line = scan.nextLine();
				if (!line.startsWith("#")) {
					StringTokenizer st = new StringTokenizer(line, "$");
					String id = st.nextToken();
					double lat = Double.parseDouble(st.nextToken());
					double lon = Double.parseDouble(st.nextToken());
					long transT = Long.parseLong(st.nextToken());
					String mes = st.nextToken();
					messages.add(new Message(id, mes, transT, lat, lon));
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void readPeople() {
		try {
			String fileName = rootPath + "/database/peoplelist.txt";
			Scanner scan = new Scanner(new FileReader(fileName));
			while (scan.hasNext()) {
				String line = scan.nextLine();
				if (!line.startsWith("#")) {
					StringTokenizer st = new StringTokenizer(line, " \t");
					String id = st.nextToken();
					String rank = st.nextToken();
					String first = st.nextToken();
					String last = st.nextToken();
					String teamID = st.nextToken();
					People p = new People(id, rank, first, last, teamID);
					peopleVector.add(p);
					peopleList.put(id, p);
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void storeMessage() {
		try {
			FileWriter fw = new FileWriter(rootPath + "/database/messages.txt");
			fw.write("# userID lat lon validtime mes\n");
			for (Message mes : messages) {
				fw.write(String.format("%s$%f$%f$%s$%s\n", mes.getUserId(), mes
						.getLatitude(), mes.getLongitude(), ""
						+ mes.getValidTime(), mes.getMes()));
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void addEvent(Event e) {
		try {
			synchronized (events) {
				events.addFirst(e);
			}
		} catch (Exception ev) {
			ev.getCause();
		}
	}

	public static Event getEvent() {
		Event e = null;
		try {
			synchronized (events) {
				if (events.size() != 0)
					e = events.removeLast();
			}
		} catch (Exception ev) {
			ev.getCause();
		}
		return e;
	}
}
