package combattalk.mobile;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import combattalk.mobile.data.Repository;

public class Preferences extends PreferenceActivity {

	public static boolean isReady = false;
	public static int connectFrec = 5000;
	public static int locUpdateFrec = 2000;
	public static boolean lockMyLoc = false;
	public static boolean showCompass = true;
	public static Context baseContext = null;
	public static boolean showLocation = true;
	public static String userId = "";
	public static String asrModelDirectory = "English-16K-040701-02-g48"; // The directory name of the asr model to use
	public static String helpStr = "hello";
	public static String serverIP = "";
	public static String defaultID = null;
	public static boolean saveBattery = true;
	public static int reachDist=20;  // distance that stands for reaching (in meter)
	public static boolean enableRemoteRecording = false;

	// NOTE: getPrefs() is called when the app starts up. onCreate() isn't called until the settings button is pressed.
	// The two had better be consistent.
	
	public static void getPrefs() {
		// Get the xml/preferences.xml preferences
		// Preferences.getPrefs();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(baseContext);

		String line = prefs.getString("connList", "5000");
		if (line != null)
			Preferences.connectFrec = Integer.parseInt(line);
		line = prefs.getString("locList", "2000");
		if (line != null)
			Preferences.locUpdateFrec = Integer.parseInt(line);
		Preferences.lockMyLoc = prefs.getBoolean("lockLocPref", false);
		Preferences.showCompass = prefs.getBoolean("showCompassPref", true);
		Preferences.showLocation = prefs.getBoolean("showLocationPref", true);
		Preferences.userId = prefs.getString("idList", "firetalk.t");
		Preferences.serverIP = prefs.getString("ipPref", "169.234.133.205");
		Preferences.saveBattery = prefs.getBoolean("batteryPref", true);
		Preferences.asrModelDirectory = prefs.getString("asrModelDirectory", "English-16K-040701-02-g48");
		Preferences.enableRemoteRecording = prefs.getBoolean("enableRemoteRecordingPref", false);
	}

	// @Override
	// protected void onStart() {
	// super.onStart();
	// this.getPrefs();
	// }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Preferences.isReady = true;
		addPreferencesFromResource(R.xml.preferences);
		ListPreference listP = (ListPreference) findPreference("idList");
		CharSequence[] ids = new CharSequence[Repository.peopleList.size()];
		CharSequence[] names = new CharSequence[ids.length];
		int i = 0;
		for (String id : Repository.peopleList.keySet()) {
			ids[i] = id;
			String name = Repository.peopleList.get(id).getName();
			names[i++] = name;
		}
		if (names.length > 0) {
			listP.setEntries(names);
			listP.setEntryValues(ids);
			listP.setDefaultValue(ids[0]);
			Preferences.defaultID = ids[0].toString();
		}
		
		// Add an entry to the asrModelDirectory preference for each directory under the
		// dsexample_data directory. There had better be nothing in dsexample_data except
		// valid model directories.
		// TODO Note the hard-coded "/dsexample_data". This should be changed.
				
		ListPreference asrModelDirectoryPreference = (ListPreference) findPreference("asrModelDirectory");
		File modelDirFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/dsexample_data");
		CharSequence[] models = modelDirFile.list();
		asrModelDirectoryPreference.setEntries(models);
		asrModelDirectoryPreference.setEntryValues(models);
		asrModelDirectoryPreference.setDefaultValue(models[0]);
		Preferences.asrModelDirectory = models[0].toString();
		
		//asrModelDirectoryPreference.setOnPreferenceChangeListener();
						
		// Get the custom preference
		// Preference customPref = (Preference) findPreference("customPref");
		// customPref
		// .setOnPreferenceClickListener(new OnPreferenceClickListener() {
		//
		// public boolean onPreferenceClick(Preference preference) {
		// Toast.makeText(getBaseContext(),
		// "The custom preference has been clicked",
		// Toast.LENGTH_LONG).show();
		// SharedPreferences customSharedPreference = getSharedPreferences(
		// "myCustomSharedPrefs", Activity.MODE_PRIVATE);
		// SharedPreferences.Editor editor = customSharedPreference
		// .edit();
		// editor.putString("myCustomPref",
		// "The preference has been clicked");
		// editor.commit();
		// return true;
		// }
		//
		// });
	}
}