package combattalk.mobile;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;

public class Preferences extends PreferenceActivity {

	public static boolean isReady = false;
	public static int connectFrec = 5000;
	public static int locUpdateFrec = 5000;
	public static boolean lockMyLoc = false;
	public static boolean showCompass = true;
	public static Context baseContext = null;
	public static boolean showLocation = true;
	public static String userId="";
	public static String helpStr="hello";
	public static void getPrefs() {
		// Get the xml/preferences.xml preferences
		// Preferences.getPrefs();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(baseContext);

		String line = prefs.getString("connList", "5000");
		if (line != null)
			Preferences.connectFrec = Integer.parseInt(line);
		line = prefs.getString("locList", "5000");
		if (line != null)
			Preferences.locUpdateFrec = Integer.parseInt(line);
		Preferences.lockMyLoc = prefs.getBoolean("lockLocPref", false);
		Preferences.showCompass = prefs.getBoolean("showCompassPref", true);
		Preferences.showLocation = prefs.getBoolean("showLocationPref", true);
		Preferences.userId  = prefs.getString("idList", "firetalk.t");
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
		// ListPreference listP=(ListPreference)findPreference("listPref");
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