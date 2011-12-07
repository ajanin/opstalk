package firetalk.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

import firetalk.model.CheckPoint;
import firetalk.model.Enemy;
import firetalk.model.Event;
import firetalk.model.ObjPoint;
import firetalk.model.People;
import firetalk.model.IEDPoint;
import firetalk.model.RallyPoint;
import firetalk.model.Team;
import firetalk.model.DBEvent.DBType;
import firetalk.util.Parameter;

public class UIRepository {
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
	public static HashMap<String, Vector<File>> audioFiles = new HashMap();
	public static LinkedList<Enemy> enemyList = new LinkedList<Enemy>();

	public static void addEnemy(Enemy e) {
		enemyList.add(e);
	}

	public static void addAudio(String id, File file) {
		Vector<File> fileV = audioFiles.get(id);
		if (fileV != null)
			fileV.add(file);
		else {
			fileV = new Vector<File>();
			fileV.add(file);
			audioFiles.put(id, fileV);
		}
	}

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
		parsePeopleFromFile(Parameter.uiDBFolder + "peopleList.txt");
		parseCheckPointsFromFile();
		parseTeamFromFile(Parameter.uiDBFolder + "teamList.txt");
		parseObjectivesFromFile();
		parseRallyFromFile();
		parseIEDFromFile();
		printPeople();
		readOperations();
		// try {
		// dbManager = new Testdb();
		// } catch (SQLException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	public static void parseRallyFromFile() {
		try {
			rallyList.clear();
			Scanner scan = new Scanner(new FileReader(Parameter.uiDBFolder
					+ Parameter.rallyFileName));
			while (scan.hasNext()) {
				String line = scan.nextLine();
				if (!line.startsWith("#")) {
					StringTokenizer st = new StringTokenizer(line, "$");
					String id = st.nextToken();
					double lat = Double.parseDouble(st.nextToken());
					double lon = Double.parseDouble(st.nextToken());
					String userID = st.nextToken();
					UIRepository.rallyList.add(new RallyPoint(id, userID, "",
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
			Scanner scan = new Scanner(new FileReader(Parameter.uiDBFolder
					+ "operations.txt"));
			while (scan.hasNext()) {
				String line = scan.nextLine();
				UIRepository.overallObjs.add(line);
			}
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void storeRallyPoints() {
		try {
			FileWriter fw = new FileWriter(Parameter.uiDBFolder
					+ Parameter.rallyFileName);
			fw.write("# id lat lon name userId\n");
			for (RallyPoint cp : UIRepository.rallyList) {
				fw.write(String.format("%s$%f$%f$%s\n", cp.id, cp.lat, cp.lon,
						cp.userID));
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void parseObjectivesFromFile() {
		try {
			objPoints.clear();
			Scanner scan = new Scanner(new FileReader(Parameter.uiDBFolder
					+ Parameter.objFileName));
			while (scan.hasNext()) {
				String line = scan.nextLine();
				if (!line.startsWith("#")) {
					StringTokenizer st = new StringTokenizer(line, "$");
					String id = st.nextToken();
					double lat = Double.parseDouble(st.nextToken());
					double lon = Double.parseDouble(st.nextToken());
					String userId = st.nextToken();
					objPoints.put(userId,
							new ObjPoint(id, userId, "", lat, lon));
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
			teamList.clear();
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

	public static void parseCheckPointsFromFile() {
		try {
			checkPoints.clear();
			Scanner scan = new Scanner(new FileReader(Parameter.uiDBFolder
					+ Parameter.wayPointFileName));
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
					String deadline = st.nextToken();
					checkPoints
							.add(new CheckPoint(id, userID, "", lat, lon, isObj
									.equals("1"), isReached.equals("1"),
									deadline));
				}
			}
			scan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void storeDB(int type, byte[] content) throws IOException {
		FileWriter fw = null;
		char[] cbuf = new char[content.length];
		for (int i = 0; i < content.length; i++)
			cbuf[i] = (char) content[i];
		if (type == DBType.IED.ordinal()) {
			fw = new FileWriter(Parameter.uiDBFolder + Parameter.IEDFileName,false);
			fw.write(cbuf);
			fw.close();
			parseIEDFromFile();
		} else if (type == DBType.objPoint.ordinal()) {
			fw = new FileWriter(Parameter.uiDBFolder + Parameter.objFileName,false);
			fw.write(cbuf);
			fw.close();
			parseObjectivesFromFile();
		} else if (type == DBType.rally.ordinal()) {
			fw = new FileWriter(Parameter.uiDBFolder + Parameter.rallyFileName,false);
			fw.write(cbuf);
			fw.close();
			parseRallyFromFile();
		} else if (type == DBType.wayPoint.ordinal()) {
			fw = new FileWriter(Parameter.uiDBFolder
					+ Parameter.wayPointFileName,false);
			fw.write(cbuf);
			fw.close();
			parseCheckPointsFromFile();
		}

	}

	public static byte[] retrieveDB(int type) {
		BufferedReader fr = null;
		Vector<Byte> buf = new Vector<Byte>();
		byte[] bytes = null;
		try {
			if (type == DBType.IED.ordinal()) {

				fr = new BufferedReader(new FileReader(Parameter.uiDBFolder
						+ Parameter.IEDFileName));
			} else if (type == DBType.objPoint.ordinal()) {
				fr = new BufferedReader(new FileReader(Parameter.uiDBFolder
						+ Parameter.objFileName));
			} else if (type == DBType.rally.ordinal()) {
				fr = new BufferedReader(new FileReader(Parameter.uiDBFolder
						+ Parameter.rallyFileName));
			} else if (type == DBType.wayPoint.ordinal()) {
				fr = new BufferedReader(new FileReader(Parameter.uiDBFolder
						+ Parameter.wayPointFileName));
			}
			if (fr != null) {
				String line = null;
				while ((line = fr.readLine()) != null) {
					for (int i = 0; i < line.length(); i++)
						buf.add((byte) line.charAt(i));
					buf.add((byte) ('\n'));
				}
				fr.close();
				bytes = new byte[buf.size()];
				for (int i = 0; i < bytes.length; i++)
					bytes[i] = buf.get(i);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bytes;

	}

	public static void printPeople() {
		for (People p : peopleList.values())
			System.out.println(p.toString());
	}

	public static void parsePeopleFromFile(String fileName) {
		try {
			peopleList.clear();
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

	public static void parseIEDFromFile() {
		try {
			IEDList.clear();
			Scanner scan = new Scanner(new FileReader(Parameter.uiDBFolder
					+ Parameter.IEDFileName));
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
			FileWriter fw = new FileWriter(Parameter.uiDBFolder
					+ Parameter.objFileName);
			fw.write("# id lat lon name userId\n");
			for (ObjPoint cp : UIRepository.objPoints.values()) {
				fw.write(String.format("%s$%f$%f$%s\n", cp.id, cp.lat, cp.lon,
						cp.userID));
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void storeCheckPoints() {

		try {
			FileWriter fw = new FileWriter(Parameter.uiDBFolder
					+ Parameter.wayPointFileName);
			fw.write("# id lat lon name userId\n");
			for (CheckPoint cp : UIRepository.checkPoints) {
				fw.write(String.format("%s$%f$%f$%s$%s$%s$%s\n", cp.id, cp.lat,
						cp.lon, cp.userID, cp.isObj() ? "1" : "0", cp
								.isReached() ? "1" : "0", cp.deadline));

			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void storeIEDPoints() {
		try {
			FileWriter fw = new FileWriter(Parameter.uiDBFolder
					+ Parameter.IEDFileName);
			fw.write("#lat lon userId mes\n");
			for (IEDPoint cp : UIRepository.IEDList) {
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
		UIRepository.objPoints.remove(userId);
		LinkedList<CheckPoint> cps = new LinkedList<CheckPoint>();
		for (CheckPoint cp : UIRepository.checkPoints) {
			if (!cp.userID.equals(userId))
				cps.add(cp);
		}
		UIRepository.checkPoints = cps;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
