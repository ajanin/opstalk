package combattalk.mobile;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Typeface;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import combattalk.mobile.data.Config;
import combattalk.mobile.data.Event;
import combattalk.mobile.data.Message;
import combattalk.mobile.data.People;
import combattalk.mobile.data.Repository;
import combattalk.mobile.data.People.LocationInfo;
import combattalk.mobile.data.People.Position;
import combattalk.mobile.map.DrawOverLay;
import combattalk.mobile.map.EventHandleOverlay;
import combattalk.mobile.map.HelloItemizedOverlay;
import combattalk.mobile.map.LocationOverlay;
import combattalk.mobile.network.Network;
import combattalk.mobile.network.Network.Status;
import combattalk.mobile.util.DataUtil;
import combattalk.mobile.util.NetUtil;
import combattalk.speech.SpeechHandler;
import combattalk.speech.SpeechSynthesis;

public class CombatTalkView extends MapActivity {
	private EventHandleOverlay eventHandleOverlay;
	private HelloItemizedOverlay mesOverlay;
	private boolean isActivityRunning = false;
	// MyMapView mapView;
	private List<Overlay> mapOverlays;
	private BaloonLayout noteBaloon;
	private MapController ct;
	public String account;
	// private GeoUpdateHandler locationManager;
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	private static final int MY_DATA_CHECK_CODE = 1111;
	private static final int PREFERENCE_CODE = 1333;
	private boolean SRPresent; // whether speech recognizer is avaiable in the
	private SpeechSynthesis mTts;
	private LocationOverlay myLocationOverlay = null;
	private boolean isTaskRunning = false;

	public LocationOverlay getMyLocationOverlay() {
		return myLocationOverlay;
	}

	public void setMyLocationOverlay(LocationOverlay myLocationOverlay) {
		this.myLocationOverlay = myLocationOverlay;
	}

	private HashMap<String, DrawOverLay> drawOverlays = new HashMap();
	private boolean isSpokenQuery = false;
	public MapView mapView;
	private ConnectThread connectThread = null;
	private ConnectivityManager cm = null;
	private boolean debugBool = true;
	private SpeechHandler speechHandler;
	// private TextView mesBoard = null;

	private int netId = 0;
	private static final int DIALOG_HELP_ID = 3333;

	// @Override
	// public void onResume() {
	// super.onResume();
	// if (!myLocationOverlay.isMyLocationEnabled())
	// myLocationOverlay.enableMyLocation();
	// if (!myLocationOverlay.isCompassEnabled())
	// myLocationOverlay.enableCompass();
	// }
	//
	// @Override
	// public void onPause() {
	// super.onPause();
	// if (myLocationOverlay.isMyLocationEnabled())
	// myLocationOverlay.disableMyLocation();
	// if (myLocationOverlay.isCompassEnabled())
	// myLocationOverlay.disableCompass();
	// }

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			if (!this.isActivityRunning) {
				this.isActivityRunning = true;
				super.onCreate(savedInstanceState);
				getWindow().setFormat(PixelFormat.TRANSPARENT);
				setContentView(R.layout.main);
				// mesBoard = (TextView) this.findViewById(R.id.text_view);
				Config.defaultConfig();
				// --- read preferences ---
				Preferences.baseContext = this.getBaseContext();
				Preferences.getPrefs();
				Repository.init(); // read data from db
				// --- speech recognizer ---
				// Check to see if a recognition activity is present
				PackageManager pm = getPackageManager();
				List<ResolveInfo> activities = pm
						.queryIntentActivities(new Intent(
								RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
				SRPresent = activities.size() != 0;
				speechHandler = new SpeechHandler(this);

				// --- TTS ---
				Intent checkIntent = new Intent();
				checkIntent
						.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
				startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

				// --- Customize balloon layout
				LayoutInflater layoutInflater = (LayoutInflater) this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				noteBaloon = (BaloonLayout) layoutInflater.inflate(
						R.layout.baloon, null);
				RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
						200, 100);
				layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
				layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
				noteBaloon.setLayoutParams(layoutParams);
				((ImageView) (noteBaloon.findViewById(R.id.close_button)))
						.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								noteBaloon.setVisibility(View.GONE);
								mapView.setEnabled(true);
							}
						});
				TextView textView = ((TextView) noteBaloon
						.findViewById(R.id.note_text));
				textView.setTextColor(Color.BLACK);
				textView.setTypeface(Typeface.DEFAULT_BOLD);

				// --- get account---
				// AccountManager manager = AccountManager.get(this);
				// Account[] accounts = manager.getAccountsByType("com.google");
				// if (accounts.length > 1)
				// account = accounts[1].name.substring(0, 10);
				// else
				// account = accounts[0].name.substring(0, 10);
				account = Preferences.userId;
				this.setTitle("user: " + account);

				// --- customize mapview ---
				mapView = (MapView) findViewById(R.id.mapview);
				mapView.setBuiltInZoomControls(true);
				mapView.setSatellite(false);
				ct = mapView.getController();
				ct.setZoom(16);
				mapOverlays = mapView.getOverlays();
				myLocationOverlay = new LocationOverlay(this, mapView, this);
				mapOverlays.add(myLocationOverlay);
				eventHandleOverlay = new EventHandleOverlay(this);
				mapOverlays.add(eventHandleOverlay);
				this.mesOverlay = new HelloItemizedOverlay(this.getResources()
						.getDrawable(R.drawable.mes2));
				if (this.mesOverlay.size() != 0)
					mapOverlays.add(this.mesOverlay);
				for (Iterator<String> it = Repository.peopleList.keySet()
						.iterator(); it.hasNext();) {
					String id = it.next();
					if (!id.equalsIgnoreCase(this.account)) {
						DrawOverLay overlay = new DrawOverLay(this,
								Repository.peopleList.get(id));
						drawOverlays.put(id, overlay);
						mapOverlays.add(overlay);
					}
				}

				// --- start network finding thread ---
				cm = (ConnectivityManager) getBaseContext().getSystemService(
						Context.CONNECTIVITY_SERVICE);

				connectThread = new ConnectThread();
				if (!connectThread.isAlive())
					connectThread.start();
				// timer.scheduleAtFixedRate(connectThread, 0,
				// Preferences.connectFrec);
				// while (true) {
				// String line = this.getMes();
				// if (line != null)
				// mesBoard.append(line + "\n");
				// Thread.sleep(1000);
				// }
				// connectThread.start();

				// locationManager = new GeoUpdateHandler(this);
				// updateMyLocation(locationManager.getLocation(),
				// locationManager
				// .getAngle());
			}
		} catch (Exception e) {
			this.speak("exception in " + "onCreate");
		}

	}

	// check whether status of network connectivity is connected
	public boolean isConnected() {
		// return debugBool;
		if (cm == null)
			return false;
		NetworkInfo mobileInfo = cm.getNetworkInfo(cm.TYPE_MOBILE);
		NetworkInfo wifiInfo = cm.getNetworkInfo(cm.TYPE_WIFI);
		if ((mobileInfo != null) && mobileInfo.isConnectedOrConnecting()
				|| wifiInfo != null && wifiInfo.isConnectedOrConnecting())
			return true && debugBool;
		else
			return false;
	}

	public void popUpMessage(String mes, GeoPoint point) {
		mapView.removeView(noteBaloon);
		noteBaloon.setVisibility(View.VISIBLE);
		// mapController.animateTo(noteOverlay.getTapPoint());

		((TextView) noteBaloon.findViewById(R.id.note_text)).setText(mes);
		mapView.addView(noteBaloon, new MapView.LayoutParams(200, 200, point,
				MapView.LayoutParams.BOTTOM_CENTER));
		// mapView.setEnabled(false);
	}

	public void speak(String mes) {
		try {
			if (this.mTts != null)
				this.mTts.speak(mes);
		} catch (Exception e) {
			e.fillInStackTrace();
		}
	}

	/**
	 * Fire an intent to start the speech recognition activity.
	 */
	private void startVoiceRecognitionActivity() {
		if (this.SRPresent) {
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
					"Speech recognition demo");
			startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Toast.makeText(this.getBaseContext(), "speech",Toast.LENGTH_SHORT);
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE
				&& resultCode == RESULT_OK) {

			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			// Location location = locationManager.getLocation();
			Location loc = myLocationOverlay.getLastFix();
			if (loc == null)
				speak("Your location is not available");
			else {
				String result = matches.get(0);
				if (result != null) {
					if (this.isSpokenQuery) { // it is a spoken query
						speechHandler.doCommand(result);
						// StringTokenizer st = new StringTokenizer(result,
						// " ");
						// String word = st.nextToken();
						// if (word.equals("where")) { // if it is spoken query
						// // query local repository
						// while (st.hasMoreTokens())
						// word = st.nextToken();
						// People matchPeople = null;
						// for (Iterator<People> it = Repository.peopleList
						// .values().iterator(); it.hasNext();) {
						// People p = it.next();
						// if (p.getName().equalsIgnoreCase(word))
						// matchPeople = p;
						// }
						// if (matchPeople != null) {
						// if (matchPeople.hasValidLocation()) {
						//
						// float[] locResult = new float[2];
						// double x1 = loc.getLatitude();
						// double y1 = loc.getLongitude();
						// double x2 = matchPeople.getLocation().latitude;
						// double y2 = matchPeople.getLocation().longitude;
						// Location.distanceBetween(x1, y1, x2, y2,
						// locResult);
						//
						// double angle = DataUtil.calAngle(x1, y1,
						// x2, y2);
						// int index = (int) Math
						// .floor((angle - Math.PI / 8)
						// / Math.PI * 4);
						// index = index < 7 ? index + 1 : 0;
						//
						// this.speak(String.format(
						// "%s is %.1f meters to %s of you ",
						// word, locResult[0],
						// Config.Directions[index]));
						//
						// } else
						// this.speak("Don't know where " + word
						// + " is");
						// } else {
						// this.speak(word + " does not exist");
						// }
						// }
					} else if (loc != null) { // if it is a spoken message
						Message mes = new Message(this.account, result,
								(new Date()).getTime(), loc.getLatitude(), loc
										.getLongitude());
						Repository.messages.addFirst(mes);
						updateMesOverlay();
						Event mesEvent = new Event(Event.MESSAGE, (new Date())
								.getTime(), mes.getLatitude(), mes
								.getLongitude());
						mesEvent.setContent(result.getBytes());
						Repository.addEvent(mesEvent);
						this.addEvent(mesEvent);
					}
				}
			}
		}
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				mTts = new SpeechSynthesis(this);
			} else {
				 Intent installIntent = new Intent();
				 installIntent
				 .setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				 startActivityForResult(installIntent, PREFERENCE_CODE);
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	class ConnectThread extends Thread {
		private boolean isRunning() {
			return isTaskRunning;
		}

		private void setRunning(boolean isRunning) {
			isTaskRunning = isRunning;
		}

		private Network network = null;

		// private volatile Thread blinker = null;
		public ConnectThread() {
			network = new Network(CombatTalkView.this.account,
					CombatTalkView.this, netId);
		}

		public void addEvent(Event event) {
			if (!connectThread.isRunning() && network != null
					&& network.getStatus() == Status.CONNECTED) {
				network.addEvent(event);
			}

		}

		@Override
		public void run() {
			// Thread thisThread = Thread.currentThread();
			// blinker = thisThread;
			// try {
			// network = new Network(CombatTalkView.this.account,
			// CombatTalkView.this);
			// while (blinker == thisThread) {
			// Thread.yield();
			// if (Thread.currentThread().isInterrupted()) {
			// throw new InterruptedException(
			// "Stopped by ifInterruptedStop()");
			// }
			try {
				while (true) {
					if (!isRunning()) {
						setRunning(true);
						if (network != null
								&& network.getStatus() == Status.LOST
								&& network.isStopped() && network.isAllKilled()) {
							// speak("try connecting");
							if (!network.isAllKilled())
								speak("network not killed");
							else {
								network = null;
								netId++;
								// currentNet=1-currentNet;
								network = new Network(
										CombatTalkView.this.account,
										CombatTalkView.this, netId);
								network.start();
							}
						}
						setRunning(false);
					}
					Thread.sleep(Preferences.connectFrec);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			//
			// Thread.sleep(10000);
			// }
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}

		// public void stopThread() {
		// Thread tmpBlinker = blinker;
		// blinker = null;
		// if (tmpBlinker != null) {
		// tmpBlinker.interrupt();
		// }
		//
		// }

	}

	@Override
	public void onResume() {
		super.onResume();
		if (Preferences.isReady) {
			Preferences.getPrefs();
			if (this.myLocationOverlay != null) {
				if (Preferences.showCompass)
					this.myLocationOverlay.enableCompass();
				else
					this.myLocationOverlay.disableCompass();
				if (Preferences.showLocation)
					this.myLocationOverlay.enableMyLocation();
				else
					this.myLocationOverlay.disableMyLocation();
			}
			if (!this.account.equals(Preferences.userId)) {
				this.setTitle("Use ID changed, please restart the application");
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	private void exit() {
		// locationManager.stop();
		if (myLocationOverlay.isMyLocationEnabled())
			myLocationOverlay.disableMyLocation();
		if (myLocationOverlay.isCompassEnabled())
			myLocationOverlay.disableCompass();
		this.myLocationOverlay.stop();
		// debugBool=false;
		// connectThread.stopThread();
		// timer.cancel();
		if (mTts != null)
			mTts.stop();
		int pid = android.os.Process.myPid();
		android.os.Process.killProcess(pid);
		finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_exit:
			exit();
			break;
		case R.id.menu_test:
			// Location l = locationManager.getLocation();
			Location l = myLocationOverlay.getLastFix();
			Toast.makeText(
					CombatTalkView.this,
					"Network: " + connectThread.network.getStatus()
							+ "\nLocation: " + l, Toast.LENGTH_LONG).show();
			if (connectThread.network.getStatus() == Status.CONNECTED
					&& l != null) {
				Event event = new Event(Event.LOCATION, (new Date()).getTime(),
						l.getLatitude(), l.getLongitude());
				event.setContent(NetUtil.string2bytes(l.getSpeed() + " " + 0,
						30));
				this.addEvent(event);
			}
			break;
		case R.id.menu_sr:
			this.isSpokenQuery = false;
			startVoiceRecognitionActivity();
			break;
		case R.id.menu_query:
			this.isSpokenQuery = true;
			startVoiceRecognitionActivity();
			break;
		case R.id.menu_myloc:
			GeoPoint loc = myLocationOverlay.getMyLocation();
			if (loc == null)
				speak("My location is not available");
			else
				ct.animateTo(loc);
			break;
		case R.id.menu_setting:
			// this.startActivity(new Intent(this, HelloListView.class));
			Intent settingsActivity = new Intent(getBaseContext(),
					Preferences.class);
			startActivity(settingsActivity);
			break;
		case R.id.menu_help:
			
		//	this.showDialog(this.DIALOG_HELP_ID);
			break;
		}
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog=null;
		switch (id) {
		case DIALOG_HELP_ID:
			// //--- custom information dialog
//			dialog = new Dialog(this.getApplicationContext());
//			dialog.setContentView(R.layout.custom_dialog);
//			dialog.setTitle("Help");
//			ImageView image = (ImageView) dialog.findViewById(R.id.dialog_image);
//			image.setImageResource(R.drawable.menu_help);
//			TextView dialogText = (TextView) dialog
//					.findViewById(R.id.dialog_text);
//			dialogText.setText(Preferences.helpStr);
			AlertDialog.Builder builder;

			Context mContext = getApplicationContext();
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.custom_dialog,
			                               (ViewGroup) findViewById(R.id.layout_root));

			TextView text = (TextView) layout.findViewById(R.id.dialog_text);
			text.setText("Hello, this is a custom dialog!");
			ImageView image = (ImageView) layout.findViewById(R.id.dialog_image);
			image.setImageResource(R.drawable.menu_help);

			builder = new AlertDialog.Builder(mContext);
			builder.setView(layout);
			dialog = builder.create();
			//dialog.show();
			break;
		}
		return dialog;
	}

	/**
	 * update overlays using data from Repository
	 */
	public void updateMesOverlay() {
		this.mesOverlay.updateInfo();
	}

	/**
	 * @param location
	 *            : location used to update my location
	 * @param angle
	 *            : angle used to update my direction update my location data in
	 *            the repository
	 */
	public void updateLocation(String userId, double latitude,
			double longitude, double speed, double angle) {
		try {
			// if (!userId.equalsIgnoreCase(this.account)) {
			People p = Repository.peopleList.get(userId);
			if (p != null) {
				p.addLocation(p.new LocationInfo(latitude, longitude, speed,
						angle));

			}
			// }
		} catch (Exception e) {
			this.speak("exception in " + "updateMyLocation");
		}
	}

	@Override
	public void onBackPressed() {
		exit();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public Point gps2Pixel(double lat, double lon) {
		GeoPoint gp = new GeoPoint((int) (lat * 1E6), (int) (lon * 1E6));
		Point point = new Point();
		mapView.getProjection().toPixels(gp, point);
		return point;
	}

	public void addEvent(Event event) {
		connectThread.addEvent(event);

	}
}