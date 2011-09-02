package combattalk.sr;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.Vector;

import org.apache.http.util.ByteArrayBuffer;

import com.google.android.maps.GeoPoint;

import android.location.Geocoder;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

import combattalk.mobile.CombatTalkView;
import combattalk.mobile.data.CheckPoint;
import combattalk.mobile.data.People;
import combattalk.mobile.data.RallyPoint;
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

public class SpeechHandler implements SpeechCommandHandler,
		SpeechResultsHandler {
	private DynaSpeakRecognizer recognizer;
	CombatTalkView parent;
	private String str;
	private Vector<String> peoples = new Vector<String>();

	public SpeechHandler(CombatTalkView parent) {

		recognizer = new DynaSpeakRecognizer(this, this);
		this.parent = parent;
	}

	// Reset the recognizer. This should be called, for example, if
	// you load a new grammar into /mnt/sdcard/dsexample_data.

	public void reload_grammar() {
		recognizer = new DynaSpeakRecognizer(this, this);
	}

	// TODO: make this asynchronous.

	// Download new version of the grammar from the web. If successful, replaces
	// existing grammar.
	public void update_grammar_from_web() {
		final String GRAMFILE = "dsexample.Grammars";
		final String WORDFILE = "dsexample.Words";

		if (download_grammar_file(GRAMFILE) && download_grammar_file(WORDFILE)) {
			update_grammar_file(GRAMFILE);
			update_grammar_file(WORDFILE);
			reload_grammar();
			setOutput("Updated grammar from web.");
		} else {
			setOutput("Updating grammar from web failed.");
		}
	}

	// Do the actual download of the file to a local file with the
	// same name but .new appended.
	// TODO: Make this more efficient.

	private boolean download_grammar_file(String name) {
		String localdir = Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ "/dsexample_data";

		try {
			URL url = new URL("http://www3.icsi.berkeley.edu/~janin/opstalk/"
					+ name);
			File file = new File(localdir, name + ".new");
			URLConnection con = url.openConnection();
			InputStream is = con.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			ByteArrayBuffer baf = new ByteArrayBuffer(20000);
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baf.toByteArray());
			fos.close();
		} catch (IOException e) {
			Log.d("opstalk", "Error: " + e);
			return false;
		}
		return true;
	}

	// Rename the local file by removing the .new at the end.

	private void update_grammar_file(String name) {
		String localdir = Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ "/dsexample_data";
		File origfile = new File(localdir, name + ".new");
		if (!origfile.exists()) {
			setOutput("Updating grammar failed unexpectedly.");
			return;
		}
		File newfile = new File(localdir, name);
		if (!origfile.renameTo(newfile)) {
			setOutput("Updating grammar failed unexpectedly.");
			return;
		}
	}

	public void start() {
		recognizer.start();
	}

	public void stop() {
		recognizer.stop();
	}

	public boolean canListen() {
		return recognizer.canListen();
	}

	private String getOutputText() {
		// TODO Auto-generated method stub
		return Repository.speakLine;
	}

	private void setOutput(String str) {
		parent.showMessage(str);
		parent.addToSpeak(str);
	}

	@Override
	public void parserError(String msg) {
		// TODO Auto-generated method stub
		setOutput(msg);
		parent.resetButton();
	}

	@Override
	public void finalResult(String str) {
		parent.showMessage(str);
		parent.resetButton();
	}

	@Override
	public void partialResult(String str) {
		parent.showMessage(str);

	}

	@Override
	public void sayAgainCommand() {
		parent.addToSpeak(getOutputText());

	}

	@Override
	public void closestPersonToMeCommand() {
		try {
			double minDist = Double.MAX_VALUE;
			LocationInfo minLoc = null;
			People minPeople = null;
			Location loc = parent.getMyLocation();
			if (loc != null) {
				for (People ap : Repository.peopleList.values()) {
					if (!ap.getId().equals(parent.account)) {
						LocationInfo aloc = ap.getLocation();
						if (aloc != null) {
							double dist = DataUtil.calDistance(loc
									.getLatitude(), loc.getLongitude(),
									aloc.latitude, aloc.longitude);
							if (dist < minDist) {
								minDist = dist;
								minPeople = ap;
								minLoc = aloc;
							}
						}
					}
				}
				if (minPeople != null) {
					String dir = DataUtil.angle2String(DataUtil.calAngle(loc
							.getLatitude(), loc.getLongitude(),
							minLoc.latitude, minLoc.longitude));
					setOutput(String.format(
							"%s is closest person, %.1f meters %s of you",
							minPeople.getName(), minDist, dir));
					parent.mapView.getController().animateTo(
							new GeoPoint((int) (minLoc.latitude * 1E6),
									(int) (minLoc.longitude * 1E6)));
				} else
					setOutput("no one is near you");
			} else
				setOutput("your location is not available");
		} catch (Exception e) {
			setOutput("exception in closest person to me");
		}
		parent.resetButton();

	}

	@Override
	public void closestWaypointToMeCommand() {
		try {
			double minDist = Double.MAX_VALUE;
			double minLat = 0;
			double minLon = 0;
			CheckPoint minCp = null;
			Location loc = parent.getMyLocation();
			if (loc != null) {
				for (CheckPoint ap : Repository.checkPoints) {

					double dist = DataUtil.calDistance(loc.getLatitude(), loc
							.getLongitude(), ap.lat, ap.lon);
					if (dist < minDist) {
						minDist = dist;
						minCp = ap;
						minLat = ap.lat;
						minLon = ap.lon;
					}
				}

				if (minCp != null) {
					String dir = DataUtil
							.angle2String(DataUtil.calAngle(loc.getLatitude(),
									loc.getLongitude(), minLat, minLon));
					setOutput(String
							.format(
									"way point %s is closest way point, %.1f meters %s of you",
									minCp.id, minDist, dir));
					parent.mapView.getController().animateTo(
							new GeoPoint((int) (minLat * 1E6),
									(int) (minLon * 1E6)));
				} else
					setOutput("no way point is near you");
			} else
				setOutput("your location is not available");
		} catch (Exception e) {
			setOutput("exception in closest way point to me");
		}
		parent.resetButton();

	}

	@Override
	public void voiceNoteCommand() {

		parent.resetButton();

	}

	@Override
	public void whereAmICommand() {
		try {
			Location loc = parent.getMyLocation();
			if (loc != null) {
				String address = parent.getAddress(loc);
				if (address != null) {
					setOutput("Your location is " + address);
					parent.mapView.getController().animateTo(
							new GeoPoint((int) (loc.getLatitude() * 1E6),
									(int) (loc.getLongitude() * 1E6)));
				} else
					setOutput("Can not recognize your location");
			} else {
				setOutput("your location is not available");
			}
		} catch (Exception e) {
			setOutput("exception in where am I ");

		}
		parent.resetButton();
		// TODO Auto-generated method stub

	}

	@Override
	public void whereIsPersonCommand(int person) {
		try {
			Location loc = parent.getMyLocation();
			if (loc != null) {
				if (person >= 0 && person < Repository.peopleVector.size()) {
					People pQ = Repository.peopleVector.get(person);
					if (parent.account.equals(pQ.getId())) {
						whereAmICommand();
						return;
					}
					People pQuery = Repository.peopleList.get(pQ.getId());
					LocationInfo locQuery = pQuery != null ? pQuery
							.getLocation() : null;
					if (locQuery != null) {
						double dist = DataUtil.calDistance(loc.getLatitude(),
								loc.getLongitude(), locQuery.latitude,
								locQuery.longitude);
						String direction = DataUtil.angle2String(DataUtil
								.calAngle(loc.getLatitude(),
										loc.getLongitude(), locQuery.latitude,
										locQuery.longitude));
						long timeDiff = (System.currentTimeMillis() - locQuery.validTime);
						String timeWord = timeDiff > 30000 ? ""
								+ (timeDiff / 1000) + "seconds ago " : "";

						setOutput(timeWord
								+ String.format("%s is %.1f meters %s of you",
										pQuery.getName(), dist, direction));
						parent.mapView.getController().animateTo(
								new GeoPoint((int) (locQuery.latitude * 1E6),
										(int) (locQuery.longitude * 1E6)));
					} else
						setOutput("location of " + pQuery.getName()
								+ " is not available");
				} else
					setOutput("can not get person " + (person + 1));
			} else
				setOutput("your location is not available");
		} catch (Exception e) {
			setOutput("exception in where is person ");
		}
		parent.resetButton();

	}

	@Override
	public void whereIsTeamCommand(int person) {
		try {
			Location loc = parent.getMyLocation();
			if (loc != null) {
				if (person >= 0 && person < Repository.peopleVector.size()) {
					People pQ = Repository.peopleVector.get(person);
					People pQuery = Repository.peopleList.get(pQ.getId());
					LocationInfo locQuery = pQuery != null ? pQuery
							.getLocation() : null;
					if (locQuery != null) {
						double dist = DataUtil.calDistance(loc.getLatitude(),
								loc.getLongitude(), locQuery.latitude,
								locQuery.longitude);
						String direction = DataUtil.angle2String(DataUtil
								.calAngle(loc.getLatitude(),
										loc.getLongitude(), locQuery.latitude,
										locQuery.longitude));
						long timeDiff = (System.currentTimeMillis() - locQuery.validTime);
						String timeWord = timeDiff > 30000 ? ""
								+ (timeDiff / 1000) + "seconds ago " : "";

						setOutput(timeWord
								+ String.format("Team %d is %.1f meters %s of you",
										(person+1), dist, direction));
						parent.mapView.getController().animateTo(
								new GeoPoint((int) (locQuery.latitude * 1E6),
										(int) (locQuery.longitude * 1E6)));
					} else
						setOutput("location of team" + (person+1)
								+ " is not available");
				} else
					setOutput("can not get team " + (person + 1));
			} else
				setOutput("your location is not available");
		} catch (Exception e) {
			setOutput("exception in where is person ");
		}
		parent.resetButton();
	}

	@Override
	public void whereIsWaypointCommand(int waypoint) {
		try {
			Location loc = parent.getMyLocation();
			if (loc != null) {
				if (waypoint >= 0 && waypoint < Repository.checkPoints.size()) {
					CheckPoint cp = Repository.checkPoints.get(waypoint);
					double dist = DataUtil.calDistance(loc.getLatitude(), loc
							.getLongitude(), cp.lat, cp.lon);
					String direction = DataUtil.angle2String(DataUtil.calAngle(
							loc.getLatitude(), loc.getLongitude(), cp.lat,
							cp.lon));
					setOutput(String.format(
							"way point %d is %.1f meters %s of you",
							waypoint + 1, dist, direction));
					parent.mapView.getController().animateTo(
							new GeoPoint((int) (cp.lat * 1E6),
									(int) (cp.lon * 1E6)));
				} else
					setOutput("way point is not available");
			} else
				setOutput("your location is not available");
		} catch (Exception e) {
			setOutput("exception in where is way point ");
		}
		parent.resetButton();
	}

	@Override
	public void whoIsNearPersonCommand(int person) {
		try {
			if (person >= 0 && person < Repository.peopleVector.size()) {
				People queryP = Repository.peopleList
						.get(Repository.peopleVector.get(person).getId());
				double minDist = Double.MAX_VALUE;
				LocationInfo minLoc = null;
				People minPeople = null;
				LocationInfo loc = queryP.getLocation();
				if (loc != null) {
					for (People ap : Repository.peopleList.values()) {
						if (!ap.getId().equals(queryP.getId())) {
							LocationInfo aloc = ap.getLocation();
							if (aloc != null) {
								double dist = DataUtil.calDistance(
										loc.latitude, loc.longitude,
										aloc.latitude, aloc.longitude);
								if (dist < minDist) {
									minDist = dist;
									minPeople = ap;
									minLoc = aloc;
								}
							}
						}
					}
					if (minPeople != null) {
						String dir = DataUtil.angle2String(DataUtil.calAngle(
								loc.latitude, loc.longitude, minLoc.latitude,
								minLoc.longitude));
						setOutput(String.format("%s is %.1f meters %s of %s",
								minPeople.getName(), minDist, dir, queryP
										.getName()));
						parent.mapView.getController().animateTo(
								new GeoPoint((int) (minLoc.latitude * 1E6),
										(int) (minLoc.longitude * 1E6)));
					} else
						setOutput("no one is near " + queryP.getName());
				} else
					setOutput("location of " + queryP.getName()
							+ " is not available");
			} else
				setOutput(String.format("Can not get person %d", person + 1));
		} catch (Exception e) {
			setOutput("exception in who is near person ");
		}
		parent.resetButton();

	}

	@Override
	public void whoIsNearTeamCommand(int person) {
		try {
			if (person >= 0 && person < Repository.peopleVector.size()) {
				People queryP = Repository.peopleList
						.get(Repository.peopleVector.get(person).getId());
				double minDist = Double.MAX_VALUE;
				LocationInfo minLoc = null;
				People minPeople = null;
				LocationInfo loc = queryP.getLocation();
				if (loc != null) {
					for (People ap : Repository.peopleList.values()) {
						if (!ap.getId().equals(queryP.getId())) {
							LocationInfo aloc = ap.getLocation();
							if (aloc != null) {
								double dist = DataUtil.calDistance(
										loc.latitude, loc.longitude,
										aloc.latitude, aloc.longitude);
								if (dist < minDist) {
									minDist = dist;
									minPeople = ap;
									minLoc = aloc;
								}
							}
						}
					}
					if (minPeople != null) {
						String dir = DataUtil.angle2String(DataUtil.calAngle(
								loc.latitude, loc.longitude, minLoc.latitude,
								minLoc.longitude));
						setOutput(String.format("%s is %.1f meters %s of team %d",
								minPeople.getName(), minDist, dir, (person+1)));
						parent.mapView.getController().animateTo(
								new GeoPoint((int) (minLoc.latitude * 1E6),
										(int) (minLoc.longitude * 1E6)));
					} else
						setOutput("no one is near team " + (person+1));
				} else
					setOutput("location of team " + (person+1)
							+ " is not available");
			} else
				setOutput(String.format("Can not get team %d", person + 1));
		} catch (Exception e) {
			setOutput("exception in who is near person ");
		}
		parent.resetButton();

	}

	@Override
	public void whoIsNearWaypointCommand(int person) {
		try {
			if (person >= 0 && person < Repository.checkPoints.size()) {
				CheckPoint cp = Repository.checkPoints.get(person);
				double minDist = Double.MAX_VALUE;
				LocationInfo minLoc = null;
				People minPeople = null;

				for (People ap : Repository.peopleList.values()) {

					LocationInfo aloc = ap.getLocation();
					if (aloc != null) {
						double dist = DataUtil.calDistance(cp.lat, cp.lon,
								aloc.latitude, aloc.longitude);
						if (dist < minDist) {
							minDist = dist;
							minPeople = ap;
							minLoc = aloc;
						}
					}
				}
				if (minPeople != null) {
					String dir = DataUtil.angle2String(DataUtil.calAngle(
							cp.lat, cp.lon, minLoc.latitude, minLoc.longitude));
					setOutput(String.format(
							"%s is %.1f meters %s of way point %d", minPeople
									.getName(), minDist, dir, person + 1));
					parent.mapView.getController().animateTo(
							new GeoPoint((int) (minLoc.latitude * 1E6),
									(int) (minLoc.longitude * 1E6)));
				} else
					setOutput("no one is near way point " + (person + 1));

			} else
				setOutput("way point is not available");
		} catch (Exception e) {
			setOutput("exception in who is near way point ");
		}
		parent.resetButton();

	}

	@Override
	public void closestObjectiveToMeCommand() {
		try {
			double minDist = Double.MAX_VALUE;
			double minLat = 0;
			double minLon = 0;
			CheckPoint minCp = null;
			Location loc = parent.getMyLocation();
			if (loc != null) {
				for (CheckPoint ap : Repository.checkPoints) {
					if (ap.isObj()) {

						double dist = DataUtil.calDistance(loc.getLatitude(),
								loc.getLongitude(), ap.lat, ap.lon);
						if (dist < minDist) {
							minDist = dist;
							minCp = ap;
							minLat = ap.lat;
							minLon = ap.lon;
						}
					}
				}

				if (minCp != null) {
					String dir = DataUtil
							.angle2String(DataUtil.calAngle(loc.getLatitude(),
									loc.getLongitude(), minLat, minLon));
					setOutput(String.format(
							"%s is closest way point, %.1f meters %s of you",
							minCp.id, minDist, dir));
					parent.mapView.getController().animateTo(
							new GeoPoint((int) (minLat * 1E6),
									(int) (minLon * 1E6)));
				} else
					setOutput("no way point is near you");
			} else
				setOutput("your location is not available");
		} catch (Exception e) {
			setOutput("exception in cloest objective to  me ");
		}
		parent.resetButton();

	}

	@Override
	public void closestRallyToMeCommand() {
		try {
			double minDist = Double.MAX_VALUE;
			double minLat = 0;
			double minLon = 0;
			RallyPoint minCp = null;
			Location loc = parent.getMyLocation();
			if (loc != null) {
				for (RallyPoint ap : Repository.rallyList) {
					double dist = DataUtil.calDistance(loc.getLatitude(), loc
							.getLongitude(), ap.lat, ap.lon);
					if (dist < minDist) {
						minDist = dist;
						minCp = ap;
						minLat = ap.lat;
						minLon = ap.lon;
					}
				}
				if (minCp != null) {
					String dir = DataUtil
							.angle2String(DataUtil.calAngle(loc.getLatitude(),
									loc.getLongitude(), minLat, minLon));
					setOutput(String.format(
							"%s is closest rally point, %.1f meters %s of you",
							minCp.id, minDist, dir));
					parent.mapView.getController().animateTo(
							new GeoPoint((int) (minLat * 1E6),
									(int) (minLon * 1E6)));
				} else
					setOutput("no rally point is near you");
			} else
				setOutput("your location is not available");
		} catch (Exception e) {
			setOutput("exception in closest rally to me ");
		}
		parent.resetButton();

	}

	@Override
	public void whereIsObjectiveCommand(int obj) {
		try {
			Location loc = parent.getMyLocation();
			if (loc != null) {
				if (Repository.checkPoints.size() == 0)
					setOutput("no objective found");
				else
					for (CheckPoint cp : Repository.checkPoints) {
						if (cp.isObj()) {
							double dist = DataUtil.calDistance(loc
									.getLatitude(), loc.getLongitude(), cp.lat,
									cp.lon);
							String direction = DataUtil.angle2String(DataUtil
									.calAngle(loc.getLatitude(), loc
											.getLongitude(), cp.lat, cp.lon));
							setOutput(String.format(
									"objective point is %.1f meters %s of you",
									dist, direction));
							parent.mapView.getController().animateTo(
									new GeoPoint((int) (cp.lat * 1E6),
											(int) (cp.lon * 1E6)));
							break;
						}
					}

			} else
				setOutput("your location is not available");
		} catch (Exception e) {
			setOutput("exception in where is objective ");
		}
		parent.resetButton();

	}

	@Override
	public void whereIsRallyCommand(int rally) {
		try {
			Location loc = parent.getMyLocation();
			if (loc != null) {
				if (rally >= 0 && rally < Repository.rallyList.size()) {
					RallyPoint cp = Repository.rallyList.get(rally);
					double dist = DataUtil.calDistance(loc.getLatitude(), loc
							.getLongitude(), cp.lat, cp.lon);
					String direction = DataUtil.angle2String(DataUtil.calAngle(
							loc.getLatitude(), loc.getLongitude(), cp.lat,
							cp.lon));
					setOutput(String.format(
							"rally point %d is %.1f meters %s of you",
							rally + 1, dist, direction));
					parent.mapView.getController().animateTo(
							new GeoPoint((int) (cp.lat * 1E6),
									(int) (cp.lon * 1E6)));
				} else
					setOutput("rally point is not available");
			} else
				setOutput("your location is not available");
		} catch (Exception e) {
			setOutput("exception in where is rally ");
		}

		parent.resetButton();
	}

	@Override
	public void whoIsNearObjectiveCommand(int obj) {
		try {
			CheckPoint query = null;
			for (CheckPoint cp : Repository.checkPoints) {
				if (cp.isObj()) {
					query = cp;
					break;
				}
			}

			double minDist = Double.MAX_VALUE;
			LocationInfo minLoc = null;
			People minPeople = null;
			if (query != null) {
				for (People ap : Repository.peopleList.values()) {
					LocationInfo aloc = ap.getLocation();
					if (aloc != null) {
						double dist = DataUtil.calDistance(query.lat,
								query.lon, aloc.latitude, aloc.longitude);
						if (dist < minDist) {
							minDist = dist;
							minPeople = ap;
							minLoc = aloc;
						}
					}
				}
				if (minPeople != null) {
					String dir = DataUtil.angle2String(DataUtil.calAngle(
							query.lat, query.lon, minLoc.latitude,
							minLoc.longitude));
					setOutput(String.format("%s is %.1f meters %s of %s",
							minPeople.getName(), minDist, dir, "objective"));
					parent.mapView.getController().animateTo(
							new GeoPoint((int) (minLoc.latitude * 1E6),
									(int) (minLoc.longitude * 1E6)));
				} else
					setOutput("no one is near objective");
			} else
				setOutput("objective is not available");
		} catch (Exception e) {
			setOutput("exception in who is near objective ");
		}
		parent.resetButton();

	}

	@Override
	public void whoIsNearRallyCommand(int rally) {
		try {
			if (rally >= 0 && rally < Repository.rallyList.size()) {
				RallyPoint query = Repository.rallyList.get(rally);

				double minDist = Double.MAX_VALUE;
				LocationInfo minLoc = null;
				People minPeople = null;
				if (query != null) {
					for (People ap : Repository.peopleList.values()) {
						LocationInfo aloc = ap.getLocation();
						if (aloc != null) {
							double dist = DataUtil.calDistance(query.lat,
									query.lon, aloc.latitude, aloc.longitude);
							if (dist < minDist) {
								minDist = dist;
								minPeople = ap;
								minLoc = aloc;
							}
						}
					}
					if (minPeople != null) {
						String dir = DataUtil.angle2String(DataUtil.calAngle(
								query.lat, query.lon, minLoc.latitude,
								minLoc.longitude));
						setOutput(String.format(
								"%s is %.1f meters %s of rally point %d",
								minPeople.getName(), minDist, dir, rally + 1));
						parent.mapView.getController().animateTo(
								new GeoPoint((int) (minLoc.latitude * 1E6),
										(int) (minLoc.longitude * 1E6)));
					} else
						setOutput("no one is near rally point" + (rally + 1));
				} else
					setOutput("rally point " + (rally + 1)
							+ " is not available");
			} else
				setOutput("rally point is not available");
		} catch (Exception e) {
			setOutput("exception in who is near rally");
		}
		parent.resetButton();

	}
} // class SpeechHandler
