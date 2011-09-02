package combattalk.mobile.map;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import combattalk.mobile.CombatTalkView;
import combattalk.mobile.Preferences;
import combattalk.mobile.data.Event;
import combattalk.mobile.util.DataUtil;
import combattalk.mobile.util.NetUtil;

public class LocationOverlay extends MyLocationOverlay {
	private CombatTalkView parent = null;
	private MapController ct;
	private double finalAngle = 0;
	private double speed = 0;
	private LinkedList<Location> history = new LinkedList<Location>();
	private LinkedList<Float> orientations = new LinkedList<Float>();
	private int historySize = 100;
	private FrequencyControl fc = new FrequencyControl();
	private Thread fcThread = null;
	private boolean allowSend = false;

	public LocationOverlay(Context context, MapView mapView,
			CombatTalkView parent) {
		super(context, mapView);
		ct = mapView.getController();
		this.parent = parent;
		this.enableMyLocation();
		this.enableCompass();
		fc.start();

	}

	@Override
	public void onSensorChanged(int sensor, float[] values) {
		super.onSensorChanged(sensor, values);
		// orientations.addFirst(values[0]);
		// if (orientations.size() > historySize)
		// orientations.removeLast();
		calDirection();
	}

	@Override
	public void onLocationChanged(Location location) {
		super.onLocationChanged(location);
		if (!allowSend)
			return;
		try {
			this.speed = location.getSpeed();
			history.addFirst(location); // add to location history
			if (history.size() > historySize) {
				history.removeLast();
			}
			// calDirection(); // calculate direction of
			Event event = new Event(Event.LOCATION, (new Date()).getTime(),
					location.getLatitude(), location.getLongitude());
			event.setContent(NetUtil.string2bytes(this.speed + " " + finalAngle
					+ "#", 30));
			parent.addEvent(event);

			// parent.updateMyLocation(location, (int) finalAngle);
			parent.updateLocation(parent.account, location.getLatitude(),
					location.getLongitude(), location.getSpeed(), finalAngle);
			// parent.mapView.getController().animateTo(this.getMyLocation());

			Point pixel = new Point();
			parent.mapView.getProjection()
					.toPixels(this.getMyLocation(), pixel);
			int width = parent.mapView.getWidth();
			int height = parent.mapView.getHeight();
			if (pixel.x < width / 5 || pixel.x > width * 4 / 5
					|| pixel.y < height / 5 || pixel.y > height * 4 / 5)
				// ct.animateTo(myLocation);
				if (Preferences.lockMyLoc) {
					ct.animateTo(this.getMyLocation());
				}
			allowSend = false;
		} catch (Exception e) {
			parent.speak("exception in " + "onLocationChanged");
		}
	}

	private void calAngle() {
		float ave = 0;
		int count = 0;
		float totalW = 0;
		double weight = 1;
		for (Iterator<Float> it = orientations.iterator(); it.hasNext();) {
			ave += it.next() * weight;
			totalW += weight;
			weight /= 3;
			count++;
			if (count > 0)
				break;
		}
		ave = 360 - (ave / totalW - 90); // convert to degree from x axis
		ave = ave > 360 ? ave - 360 : ave;
		finalAngle = ave / 360 * 2 * Math.PI;

	}

	class FrequencyControl extends Thread {
		@Override
		public void run() {
			fcThread = Thread.currentThread();
			while (true) {
				allowSend = true;
				try {
					Thread.sleep(Preferences.locUpdateFrec);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
	}

	private void calDirection() {
		try {
			Iterator<Location> it = history.iterator();
			if (it.hasNext()) {
				Location first = it.next();
				Location sec = null;
				while (it.hasNext()) {
					sec = it.next();
					// Point screenPts1 = new Point();
					// Point screenPts2 = new Point();
					// parent.mapView.getProjection().toPixels(
					// new GeoPoint((int) (first.getLatitude() * 1E6),
					// (int) (first.getLongitude() * 1E6)),
					// screenPts1);
					// parent.mapView.getProjection().toPixels(
					// new GeoPoint((int) (sec.getLatitude() * 1E6),
					// (int) (sec.getLongitude() * 1E6)),
					// screenPts2);
					float distance = DataUtil.calDistance(sec.getLatitude(),
							sec.getLongitude(), first.getLatitude(), first
									.getLongitude());
					if (distance > 2)
						break;
				}
				if (sec != null) {
					float value = DataUtil.calBearing(sec.getLatitude(), sec
							.getLongitude(), first.getLatitude(), first
							.getLongitude());
					orientations.addFirst(value);
					if (orientations.size() > historySize)
						orientations.removeLast();
					calAngle();
					// finalAngle = DataUtil.calAngle(sec.getAltitude(), sec
					// .getLongitude(), first.getLatitude(), first
					// .getLongitude());
				}
			}
		} catch (Exception e) {
			parent.speak("exception in " + "calDirection");
		}

	}

	public void stop() {

		fcThread.interrupt();
		fcThread.stop();
	}

	// @Override
	// protected void drawMyLocation(Canvas canvas, MapView mapView,
	// Location lastFix, GeoPoint myLocation, long when) {
	// super.drawMyLocation(canvas, mapView, lastFix, myLocation, when);
	// Point pixel = new Point();
	// mapView.getProjection().toPixels(myLocation, pixel);
	// int width = mapView.getWidth();
	// int height = mapView.getHeight();
	// if (pixel.x < width / 4 || pixel.x > width * 3 / 4
	// || pixel.y < height / 4 || pixel.y > height * 3 / 4)
	// //ct.animateTo(myLocation);
	// ct.setCenter(myLocation);
	// }

}
