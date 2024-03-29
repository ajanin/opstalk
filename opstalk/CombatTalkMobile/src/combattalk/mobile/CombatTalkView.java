package combattalk.mobile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import combattalk.mobile.map.DrawOverLay;
import combattalk.mobile.map.EventHandleOverlay;
import combattalk.mobile.map.GeoUpdateHandler;
import combattalk.mobile.map.LocationOverlay;
import combattalk.mobile.map.MessageOverlay;
import combattalk.mobile.network.Network;
import combattalk.mobile.network.Network.Status;
import combattalk.speech.SpeechCapture;
import combattalk.speech.SpeechSynthesis;
import combattalk.sr.SpeechHandler;

public class CombatTalkView extends MapActivity {
	private EventHandleOverlay eventHandleOverlay;
	private MessageOverlay mesOverlay;
	private DrawOverLay drawOverlay;
	// private CheckPointOverlay checkOverlay;
	// private RallyPointOverlay rallyOverlay;
	// MyMapView mapView;
	private List<Overlay> mapOverlays;
	private BaloonLayout noteBaloon;
	private MapController ct;
	private TextView mesView = null;
	private Button speechButton = null;
	public String account;
	public GeoUpdateHandler locationHandler = null;
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	private static final int MY_DATA_CHECK_CODE = 1111;
	private static final int PREFERENCE_CODE = 1333;
	private boolean SRPresent; // whether speech recognizer is avaiable in the
	private SpeechSynthesis mTts;
	private LinkedList<String> speakQueue = new LinkedList<String>();
	public LocationOverlay myLocationOverlay = null;
	private SpeechCapture speechCap=null;
	private boolean isTaskRunning = false;
	private boolean isSpeaking = false;

	public void setMyLocationOverlay(LocationOverlay myLocationOverlay) {
		// this.myLocationOverlay = myLocationOverlay;
	}

	// private HashMap<String, DrawOverLay> drawOverlays = new HashMap();
	private boolean isSpokenQuery = false;
	public MapView mapView;
	private ConnectThread connectThread = null;
	private ConnectivityManager cm = null;
	private boolean debugBool = true;
	public SpeechHandler speechHandler;
	// private TextView mesBoard = null;
	// public GeoUpdateHandler locationHandler = null;
	private int netId = 0;
	// private SpeakThread speakThread;
	private static final int DIALOG_HELP_ID = 3333;

	@Override
	public void onPause() {
		super.onPause();
		// if (myLocationOverlay.isMyLocationEnabled())
		// myLocationOverlay.disableMyLocation();
		// if (myLocationOverlay.isCompassEnabled())
		// myLocationOverlay.disableCompass();
	}

	public void resetButton() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (speechButton != null) {
					speechButton.setText("Start Recording");
					speechButton.setEnabled(true);
				}
			}
		});
	}

	public Location getMyLocation() {
		if (Preferences.saveBattery)
			return this.locationHandler == null ? null : this.locationHandler
					.getLocation();
		else
			return this.myLocationOverlay == null ? null
					: this.myLocationOverlay.getLastFix();
	}
	
	public void enableCapture() {
		if (speechCap != null) {
			try {
				speechCap.start();
			} catch (Exception e) {
				addToSpeak("Unable to start speech capture");
			}
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			Repository.initDB(); // read data from db
			super.onCreate(savedInstanceState);
			setContentView(R.layout.main);
			// mesBoard = (TextView) this.findViewById(R.id.text_view);
			Config.defaultConfig();
			// --- read preferences ---
			Preferences.baseContext = this.getBaseContext();
			Preferences.getPrefs();
			// --- speech recognizer ---
			// Check to see if a recognition activity is present
			PackageManager pm = getPackageManager();
			List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
					RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
			SRPresent = activities.size() != 0;
			speechHandler = new SpeechHandler(this);
			speechButton = (Button) this.findViewById(R.id.recording);
			speechButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (speechHandler.canListen()) {
						if (speechCap != null) {
							speechCap.stop();
						}
						speechButton.setText("Listening...");
						speechButton.setEnabled(false);
						speechHandler.start();
					} else {
						addToSpeak("Recognizer not ready");
					}
				}
			});
			mesView = (TextView) this.findViewById(R.id.mes_view);
			// --- TTS ---
			Intent checkIntent = new Intent();
			checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

			// --- Customize balloon layout
			LayoutInflater layoutInflater = (LayoutInflater) this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			noteBaloon = (BaloonLayout) layoutInflater.inflate(R.layout.baloon,
					null);
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
			textView.setTextSize(16);
			textView.setTextColor(Color.BLACK);
			textView.setTypeface(Typeface.DEFAULT_BOLD);
			account = Preferences.userId;
			if (Repository.peopleList.get(account) != null)
				this.setTitle(account + " "
						+ Repository.peopleList.get(account).getName());

			// --- customize mapview ---
			mapView = (MapView) findViewById(R.id.mapview);
			mapView.setBuiltInZoomControls(true);
			mapView.setSatellite(false);
			ct = mapView.getController();
			ct.setZoom(16);
			mapOverlays = mapView.getOverlays();
			eventHandleOverlay = new EventHandleOverlay(this);
			mapOverlays.add(eventHandleOverlay);
			this.mesOverlay = new MessageOverlay(this.getResources()
					.getDrawable(R.drawable.warning_small));
			if (this.mesOverlay.size() != 0)
				mapOverlays.add(this.mesOverlay);
			drawOverlay=new DrawOverLay(this);
			mapOverlays.add(drawOverlay);

			locationHandler = new GeoUpdateHandler(this);
			myLocationOverlay = new LocationOverlay(this, mapView, this);
			mapOverlays.add(myLocationOverlay);

			// --- start network finding thread ---
			cm = (ConnectivityManager) getBaseContext().getSystemService(
					Context.CONNECTIVITY_SERVICE);
			// (speakThread = new SpeakThread()).start();
			connectThread = new ConnectThread();
			if (!connectThread.isAlive())
				connectThread.start();
			if (Preferences.enableRemoteRecording == true) {
				speechCap=new SpeechCapture(this);
				speechCap.start();
			} else {
				speechCap = null;
			}

		} catch (Exception e) {
			this.addToSpeak("exception in " + "onCreate");
		}

	}

	// check whether status of network connectivity is connected
	public boolean isConnected() {
		try {
			// return debugBool;
			if (cm == null)
				return false;
			NetworkInfo mobileInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if ((mobileInfo != null) && mobileInfo.isConnectedOrConnecting()
					|| wifiInfo != null && wifiInfo.isConnectedOrConnecting())
				return true && debugBool;
			else
				return false;
		} catch (Exception e) {
			this.addToSpeak("exception in isconnected");
			return false;
		}
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

	private void speak(String mes) {
		try {
			if (this.mTts != null && mes != null) {
				this.mTts.speak(mes);
				this.isSpeaking = true;
			}
		} catch (Exception e) {
			e.fillInStackTrace();
		}
	}

	public void addToSpeak(String mes) {
		Repository.speakLine = mes;
		this.speak(mes);
		// this.speakQueue.addFirst(mes);
	}

	/**
	 * Fire an intent to start the speech recognition activity.
	 */
	public void startVoiceRecognitionActivity() {
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
			Location loc = this.getMyLocation();
			if (loc == null)
				addToSpeak("Your location is not available");
			else {

				String result = matches.get(0);
				if (result != null) {
					if (this.isSpokenQuery) { // it is a spoken query

					} else if (loc != null) { // if it is a spoken message
						Message mes = new Message(this.account, result,
								System.currentTimeMillis(), loc.getLatitude(),
								loc.getLongitude());
						Repository.messages.add(mes);
						updateMesOverlay();
						Event mesEvent = new Event(Event.MESSAGE,
								System.currentTimeMillis(), mes.getLatitude(),
								mes.getLongitude());
						mesEvent.setContent(result.getBytes());
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
			} else if (event.getEventType() == Event.MESSAGE
					|| event.getEventType() == Event.CHECK_REACH)
				Repository.addEvent(event);

		}

		@Override
		public void run() {
			try {
				while (true) {
					// showTitle("connect thread ok");
					// Thread.sleep(1000);
					if (!isRunning()) {

						setRunning(true);
						if (network != null
								&& network.getStatus() == Status.LOST
								&& network.isStopped()) {
							// speak("try connecting");
							// if (!network.isAllKilled())
							// addToSpeak("network not killed");
							// else {
							network = null;
							netId++;
							// currentNet=1-currentNet;
							network = new Network(CombatTalkView.this.account,
									CombatTalkView.this, netId);
							network.start();
							// showTitle("network thread ok");
							// }
						}

						setRunning(false);
					}
					Thread.sleep(Preferences.connectFrec);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onResume() {
		try {
			super.onResume();
			if (Preferences.isReady) {
				Preferences.getPrefs();
				if (this.myLocationOverlay != null) {
					if (Preferences.showCompass)
						this.myLocationOverlay.enableCompass();
					else
						this.myLocationOverlay.disableCompass();

					// if (Preferences.showLocation)
					// this.myLocationOverlay.enableMyLocation();
					// else
					// this.myLocationOverlay.disableMyLocation();
				}
				if (Preferences.saveBattery) {
					this.myLocationOverlay.disableMyLocation();
					if (!this.locationHandler.isStarted())
						this.locationHandler.start();
				} else {
					this.myLocationOverlay.enableMyLocation();
					if (this.locationHandler.isStarted())
						this.locationHandler.stop();
				}
				if (Preferences.showLocation) {
					if (Preferences.saveBattery) {
						if (!this.locationHandler.isStarted())
							this.locationHandler.start();
					} else
						this.myLocationOverlay.enableMyLocation();
				} else {
					if (Preferences.saveBattery) {
						if (this.locationHandler.isStarted())
							this.locationHandler.stop();
					} else
						this.myLocationOverlay.disableMyLocation();

				}

				if (!this.account.equals(Preferences.userId)) {
					this.setTitle("Use ID changed, please restart the application");
				}
				
				// If you're currently recording, and preference is to not record,
				// stop recording.
				if (speechCap != null && Preferences.enableRemoteRecording == false) {
					speechCap.stop();
					speechCap = null;
					
				// If you're not recording, and preference is to record, create a new
				// speechCap and start it up.
				} else if (speechCap == null && Preferences.enableRemoteRecording == true) {
					speechCap=new SpeechCapture(this);
					speechCap.start();					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	private void exit() {
		Repository.storeDB();
		if (myLocationOverlay != null) {
			if (myLocationOverlay.isCompassEnabled())
				myLocationOverlay.disableCompass();
			this.myLocationOverlay.stop();
		}
		if (locationHandler != null)
			this.locationHandler.stop();
		if (this.connectThread != null && this.connectThread.isAlive())
			this.connectThread.stop();
		if (mTts != null)
			mTts.stop();
		int pid = android.os.Process.myPid();
		android.os.Process.killProcess(pid);
		finish();
	}

	public String getAddress(Location loc) {
		Geocoder gcd = new Geocoder(this, Locale.getDefault());
		try {
			List<Address> addresses = gcd.getFromLocation(loc.getLatitude(),
					loc.getLongitude(), 1);
			if (addresses != null && addresses.size() > 0)
				return addresses.get(0).getAddressLine(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_exit:
			exit();
			break;
		case R.id.menu_test:
			// for (CheckPoint apoint : Repository.checkPoints) {
			// Event mesEvent = new Event(Event.CHECK_REACH, System
			// .currentTimeMillis(), 0, 0);
			// mesEvent.setContent(apoint.id.getBytes());
			// this.addEvent(mesEvent);
			//
			// }
			//
			// Location l = Preferences.saveBattery ? locationHandler
			// .getLocation() : myLocationOverlay.getLastFix();
			// Toast.makeText(
			// CombatTalkView.this,
			// "Network: " + connectThread.network.getStatus()
			// + "\nLocation: " + l, Toast.LENGTH_LONG).show();
			// if (connectThread.network.getStatus() == Status.CONNECTED
			// && l != null) {
			// Event event = new Event(Event.LOCATION, System
			// .currentTimeMillis(), l.getLatitude(), l.getLongitude());
			// event.setContent(NetUtil.string2bytes(l.getSpeed() + " " + 0,
			// 30));
			// this.addEvent(event);
			// }
			this.speechHandler.update_grammar_from_web();
			break;
		case R.id.menu_sr:
			this.isSpokenQuery = false;
			startVoiceRecognitionActivity();
			break;
		case R.id.menu_myloc:
			GeoPoint loc = Preferences.saveBattery ? locationHandler
					.getGeoLocation() : myLocationOverlay.getMyLocation();
			if (loc == null)
				addToSpeak("My location is not available");
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
			Repository.messages.clear();
			Repository.checkPoints.clear();
			this.updateMesOverlay();
			// this.updateCheckOverlay();
			break;
		}
		return true;
	}

	/**
	 * update overlays using data from Repository
	 */
	public void updateMesOverlay() {
		this.mesOverlay.updateInfo();
	}

	// public void updateCheckOverlay() {
	// this.checkOverlay.updateInfo();
	// }
	//
	// public void updateRallyOverlay() {
	// this.rallyOverlay.updateInfo();
	// }

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
//			 mapView.postInvalidate();
			// }
		} catch (Exception e) {
			this.addToSpeak("exception in " + "updateMyLocation");
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

	public void showMessage(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mesView.setText(str);
			}
		});
	}

	public void showTitle(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				setTitle(str);
			}
		});
	}

	public void addEvent(Event event) {

		connectThread.addEvent(event);

	}
}