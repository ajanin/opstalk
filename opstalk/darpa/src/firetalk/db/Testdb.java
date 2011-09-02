package firetalk.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.ListIterator;

import firetalk.model.Alert;
import firetalk.model.CheckPoint;
import firetalk.model.People;
import firetalk.model.Task;
import firetalk.model.Team;

public class Testdb {

	private String userName;
	private String password;
	private String serverName;
	private String myDatabase;
	private String url;
	private Connection conn;
	private LinkedList<String> iconLocations;

	public Testdb() throws SQLException {
		establishConn();
	}

	public void establishConn() throws SQLException {
		try {
			// Load the JDBC driver
			String driverName = "com.mysql.jdbc.Driver"; // MySQL JDBC driver
			Class.forName(driverName);

			// Create a connection to the database
			this.serverName = "localhost";
			this.myDatabase = "darpadb";
			this.url = "jdbc:mysql://" + serverName + "/" + myDatabase; // a
			// JDBC
			// url
			this.userName = "root";
			this.password = "xjzjlxjzjl";

			conn = DriverManager.getConnection(url, userName, password);
			System.out.println("Connection to the database established.");

		} catch (ClassNotFoundException e) {
			// Could not find the database driver
			System.out.println("Could not find database driver.");
		}
	}

	public void deleteTable(String tableName) throws SQLException {

		String query = "drop table " + tableName;
		Statement stmt;
		stmt = conn.createStatement();
		stmt.execute(query);
	}

	public void removeTable(String tableName) {

	}

	public void insertPeople(int userID, String pName, int levelID)
			throws SQLException {

		String query = "Insert into people values(" + userID + ", " + "\""
				+ pName + "\", " + levelID + ")";
		Statement stmt;
		stmt = conn.createStatement();
		stmt.execute(query);
	}

	public void retrievePeople() throws SQLException {

		ResultSet rs;
		String query = "select * from people";
		Statement stmt = null;
		stmt = conn.createStatement();
		rs = stmt.executeQuery(query);

		while (rs.next()) {
			People p = new People();
			String userID = rs.getString(1);
			String pname = (rs.getString(2));
			String rank = (rs.getString(3));
			String teamID = rs.getString(4);
			Repository.peopleList.put(userID, new People(userID, rank, pname,
					"", teamID));
		}
	}

	public void insertLocation(int userID, double latitude, double longitude,
			double speed, double direction) throws SQLException {

		String query = "Insert into location values(" + userID + ", "
				+ latitude + ", " + longitude + ", " + speed + ", " + direction
				+ ")";
		Statement stmt;
		stmt = conn.createStatement();
		stmt.execute(query);
	}

	public void insertMessage(String userID, double latitude, double longitude,
			String message, long time) throws SQLException {

		String query = "Insert into message values(\"" + userID + "\", "
				+ latitude + ", " + longitude + ", " + "\"" + message + "\","
				+ time + ")";
		Statement stmt;
		stmt = conn.createStatement();
		stmt.execute(query);
	}

	public void retrieveMessage(int userID) throws SQLException {

		ResultSet rs;
		String query = "select message from message where userID = " + userID;
		Statement stmt = null;
		stmt = conn.createStatement();
		rs = stmt.executeQuery(query);

		while (rs.next()) {
			System.out.println(rs.getString(1));
		}
	}

	public void insertTasks(int taskID, int userID, String task, String status,
			String priority) throws SQLException {

		String query = "Insert into task values(" + taskID + ", " + userID
				+ ", " + "\"" + task + "\", " + "\"" + status + "\", "
				+ priority + ")";
		Statement stmt;
		stmt = conn.createStatement();
		stmt.execute(query);

		Task t = new Task();
		t.priority = priority;
		t.status = status;
		t.task = task;

	}

	public void retrieveTasks() throws SQLException {

		ResultSet rs;
		String query = "select * from task";
		Statement stmt = null;
		stmt = conn.createStatement();
		rs = stmt.executeQuery(query);
		while (rs.next()) {
			Task t = new Task();
			t.taskID = Integer.parseInt(rs.getString(1));
			// t.userID = Integer.parseInt(rs.getString(2));
			t.task = rs.getString(2);
			t.status = rs.getString(3);
			t.priority = rs.getString(4);
			// taskList.add(t);
		}
	}

	public void insertAlerts(int alertID, int alertLevel, double time,
			int personID) throws SQLException {

		String query = "Insert into location values(" + alertID + ", "
				+ alertLevel + ", " + time + ", " + +personID + ")";
		Statement stmt;
		stmt = conn.createStatement();
		stmt.execute(query);
	}

	public void retrieveAlerts() throws SQLException {

		ResultSet rs;
		String query = "select * from alert";
		Statement stmt = null;
		stmt = conn.createStatement();
		rs = stmt.executeQuery(query);

		while (rs.next()) {
			Alert a = new Alert();
			// a.id = Integer.parseInt(rs.getString(1));
			// a.level = Integer.parseInt(rs.getString(2));
			// a.time = Double.parseDouble(rs.getString(3));
			// a.personId = Integer.parseInt(rs.getString(4));
			// alertList.add(a);
		}
	}

	public void retrieveCheckPoints() throws SQLException {

		ResultSet rs;
		String query = "select * from checkPoints";
		Statement stmt = null;
		stmt = conn.createStatement();
		rs = stmt.executeQuery(query);

		while (rs.next()) {
			String id = rs.getString(1);
			String userID = rs.getString(2);
			double lat = rs.getDouble(3);
			double lon = rs.getDouble(4);
			String name = rs.getString(5);
			Repository.checkPoints.add(new CheckPoint(id, userID, name, lat,
					lon, false, false,""));
		}
	}

	public void insertCheckPoint(CheckPoint cp) throws SQLException {
//		String query = "Insert into checkPoints values(\"" + cp.id + "\", \""
//				+ cp.userID + "\", " + cp.lat + ", " + cp.lon + ", \""
//				+ cp.name + "\")";
//		Statement stmt;
//		stmt = conn.createStatement();
//		stmt.execute(query);
	}

	public void retrieveTeams() throws SQLException {

		ResultSet rs;
		String query = "select * from team";
		Statement stmt = null;
		stmt = conn.createStatement();
		rs = stmt.executeQuery(query);

		while (rs.next()) {
			String teamID = rs.getString(1);
			String teamName = rs.getString(2);
			Repository.teamList.put(teamID, new Team(teamID, teamName));
		}
	}

	public void insertLevel() throws SQLException {

		String query = "insert into person_level values(1, \"soldier\", \"F:\\\\military\\\\army\")";
		Statement stmt1 = null;
		stmt1 = conn.createStatement();
		stmt1.execute(query);

		query = "insert into person_level values(2, \"teamLead\", \"F:\\\\military\\\\colonel\")";
		Statement stmt2 = null;
		stmt2 = conn.createStatement();
		stmt2.execute(query);

		query = "insert into person_level values(3, \"squadLead\", \"F:\\\\military\\\\major\")";
		Statement stmt3 = null;
		stmt3 = conn.createStatement();
		stmt3.execute(query);
	}

	public void retrieveLevel() throws SQLException {

		ResultSet rs;

		String query = "select image from person_level order by levelID";
		Statement stmt = null;
		stmt = conn.createStatement();
		rs = stmt.executeQuery(query);

		while (rs.next()) {
			iconLocations.add(rs.getString(1));
		}

		ListIterator<String> it = iconLocations.listIterator();

		while (it.hasNext()) {
			String temp = it.next();
			System.out.println(temp);
		}
	}
}
