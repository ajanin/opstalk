package firetalk.db;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

import firetalk.model.CheckPoint;
import firetalk.model.Event;
import firetalk.model.ObjPoint;
import firetalk.model.People;
import firetalk.model.IEDPoint;
import firetalk.model.RallyPoint;
import firetalk.model.Team;

public class Repository {
	public static LinkedList<Event> events = new LinkedList<Event>(); // events
	// to
	public static HashMap<String, People> peopleList = new HashMap<String, People>();
	public static LinkedList<CheckPoint> checkPoints = new LinkedList<CheckPoint>(); // @jve:decl-index=0:
	public static LinkedList<IEDPoint> IEDList = new LinkedList<IEDPoint>();
	public static LinkedList<RallyPoint> rallyList = new LinkedList<RallyPoint>();
	public static HashMap<String, String> id2name = new HashMap<String, String>();
	public static HashMap<String, Long> transTime = new HashMap<String, Long>();
	public static HashMap<String, Team> teamList = new HashMap<String, Team>();
	public static Vector<String> overallObjs = new Vector<String>();
	public static HashMap<String, ObjPoint> objPoints = new HashMap<String, ObjPoint>();

	public static void addIED(IEDPoint mes) {
		IEDList.add(mes);
		// if (dbManager != null)
		// try {
		// dbManager.insertMessage(mes.getUserId(), mes.getLatitude(), mes
		// .getLongitude(), mes.getMes(), mes.getValidTime());
		// } catch (NumberFormatException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (SQLException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	public static void init() {
		parsePeopleFromFile("db/peopleList.txt");
		parseCheckPointsFromFile("db/checkPoints.txt");
		parseTeamFromFile("db/teamList.txt");
		parseObjectivesFromFile("db/objectives.txt");
		parseRallyFromFile("db/rallyPoints.txt");
		retrieveIEDFromFile();
		printPeople();
		readOperations();
		// try {
		// dbManager = new Testdb();
		// } catch (SQLException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	public static void parseRallyFromFile(String fileName) {
		try {
			Scanner scan = new Scanner(new FileReader(fileName));
			while (scan.hasNext()) {
				String line = scan.nextLine();
				if (!line.startsWith("#")) {
					StringTokenizer st = new StringTokenizer(line, "$");
					String id = st.nextToken();
					double lat = Double.parseDouble(st.nextToken());
					double lon = Double.parseDouble(st.nextToken());
					String userID = st.nextToken();
					Repository.rallyList.add(new RallyPoint(id, userID, "",
							lat, lon));
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void readOperations() {
		try {
			Scanner scan = new Scanner(new FileReader("db/operations.txt"));
			while (scan.hasNext()) {
				String line = scan.nextLine();
				Repository.overallObjs.add(line);
			}
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void storeRallyPoints() {
		try {
			FileWriter fw = new FileWriter("db/rallyPoints.txt");
			fw.write("# id lat lon name userId\n");
			for (RallyPoint cp : Repository.rallyList) {
				fw.write(String.format("%s$%f$%f$%s\n", cp.id, cp.lat,
						cp.lon, cp.userID));
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void parseObjectivesFromFile(String fileName) {
		try {
			Scanner scan = new Scanner(new FileReader(fileName));
			while (scan.hasNext()) {
				String line = scan.nextLine();
				if (!line.startsWith("#")) {
					StringTokenizer st = new StringTokenizer(line, "$");
					String id = st.nextToken();
					double lat = Double.parseDouble(st.nextToken());
					double lon = Double.parseDouble(st.nextToken());
					String userId = st.nextToken();
					objPoints.put(userId, new ObjPoint(id, userId, "", lat,
							lon));
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void parseTeamFromFile(String fileName) {
		try {
			Scanner scan = new Scanner(new FileReader(fileName));
			while (scan.hasNext()) {
				String line = scan.nextLine();
				if (!line.startsWith("#")) {
					StringTokenizer st = new StringTokenizer(line, "$");
					String id = st.nextToken();
					String name = st.nextToken();
					teamList.put(id, new Team(id, name));
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void parseCheckPointsFromFile(String fileName) {
		try {
			Scanner scan = new Scanner(new FileReader(fileName));
			while (scan.hasNext()) {
				String line = scan.nextLine();
				if (!line.startsWith("#")) {
					StringTokenizer st = new StringTokenizer(line, "$");
					String id = st.nextToken();
					double lat = Double.parseDouble(st.nextToken());
					double lon = Double.parseDouble(st.nextToken());
					String userID = st.nextToken();
					String isObj = st.nextToken();
					String isReached = st.nextToken();
					String deadline=st.nextToken();
					checkPoints.add(new CheckPoint(id, userID, "", lat, lon,
							isObj.equals("1"), isReached.equals("1"),deadline));
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void printPeople() {
		for(People p:peopleList.values())
			System.out.println(p.toString());
	}

	public static void parsePeopleFromFile(String fileName) {
		try {
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
					peopleList.put(id,
							new People(id, rank, first, last, teamID));
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void retrieveIEDFromFile() {
		try {
			Scanner scan = new Scanner(new FileReader("db/IEDPoints.txt"));
			while (scan.hasNext()) {
				String line = scan.nextLine();
				if (!line.startsWith("#")) {
					StringTokenizer st = new StringTokenizer(line, "$");
					double lat = Double.parseDouble(st.nextToken());
					double lon = Double.parseDouble(st.nextToken());
					String userId = st.nextToken();
					String mes = st.nextToken();
					long time = Long.parseLong(st.nextToken());
					IEDList.add(new IEDPoint(userId, mes, time, lat, lon));
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void storeObjPoints() {

		try {
			FileWriter fw = new FileWriter("db/objectives.txt");
			fw.write("# id lat lon name userId\n");
			for (ObjPoint cp : Repository.objPoints.values()) {
				fw.write(String.format("%s$%f$%f$%s\n", cp.id, cp.lat,
						cp.lon, cp.userID));
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void storeCheckPoints() {

		try {
			FileWriter fw = new FileWriter("db/checkPoints.txt");
			fw.write("# id lat lon name userId\n");
			for (CheckPoint cp : Repository.checkPoints) {
				fw.write(String.format("%s$%f$%f$%s$%s$%s$%s\n", cp.id, cp.lat,
						cp.lon, cp.userID, cp.isObj() ? "1" : "0", cp
								.isReached() ? "1" : "0",cp.deadline));

			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void storeIEDPoints() {
		try {
			FileWriter fw = new FileWriter("db/IEDPoints.txt");
			fw.write("#lat lon userId mes\n");
			for (IEDPoint cp : Repository.IEDList) {
				fw.write(String.format("%f$%f$%s$%s$%d\n", cp.getLatitude(), cp
						.getLongitude(), cp.getUserId(), cp.getMes(), cp
						.getValidTime()));
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void removeObjPoint(String userId) {
		Repository.objPoints.remove(userId);
		LinkedList<CheckPoint> cps = new LinkedList<CheckPoint>();
		for (CheckPoint cp : Repository.checkPoints) {
			if (!cp.userID.equals(userId))
				cps.add(cp);
		}
		Repository.checkPoints = cps;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
