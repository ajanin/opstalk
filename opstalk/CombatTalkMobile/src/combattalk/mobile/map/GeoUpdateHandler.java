package combattalk.mobile.map;

import java.util.Iterator;
import java.util.LinkedList;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;

import combattalk.mobile.CombatTalkView;
import combattalk.mobile.Preferences;
import combattalk.mobile.data.CheckPoint;
import combattalk.mobile.data.Event;
import combattalk.mobile.data.Message;
import combattalk.mobile.data.Repository;
import combattalk.mobile.map.LocationOverlay.FrequencyControl;
import combattalk.mobile.util.DataUtil;
import combattalk.mobile.util.NetUtil;

import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GeoUpdateHandler implements LocationListener {
	private CombatTalkView parent = null;
	private MapController ct;
	private double finalAngle = 0;
	private double speed = 0;
	private LinkedList<Location> history = new LinkedList<Location>();
	private LinkedList<Float> orientations = new LinkedList<Float>();
	private int historySize = 100;
	private LocationManager locationManager;
	private boolean start = true;

	public GeoUpdateHandler(CombatTalkView parent) {
		// TODO Auto-generated constructor stub
		this.parent = parent;
		ct = parent.mapView.getController();
		locationManager = (LocationManager) parent
				.getSystemService(Context.LOCATION_SERVICE);
		if (Preferences.saveBattery) {
			this.start();
			start = true;
			Location lastKnown = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (lastKnown != null) {
				parent.updateLocation(parent.account, lastKnown.getLatitude(),
						lastKnown.getLongitude(), lastKnown.getSpeed(),
						finalAngle);
			}
			history.addFirst(lastKnown);
		}
	}

	public Location getLocation() {
		return history.isEmpty() ? null : history.getFirst();
	}

	public double getAngle() {
		return finalAngle;
	}

	public void stop() {
		locationManager.removeUpdates(this);
		start = false;
	}

	public void start() {
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				Preferences.locUpdateFrec, 1, this);
		start = true;
	}

	public boolean isStarted() {
		return start;
	}

	public GeoPoint getGeoLocation() {
		Location location = getLocation();
		if (location == null)
			return null;
		else
			return new GeoPoint((int) (location.getLatitude() * 1E6),
					(int) (location.getLongitude() * 1E6));
	}

	private void checkNewLocation(double lat, double lon) {
		double reachDist = Preferences.reachDist;
		for (CheckPoint apoint : Repository.checkPoints) {
			double aDist = DataUtil.calDistance(lat, lon, apoint.lat,
					apoint.lon);
			if (aDist < reachDist) {
				if (apoint.increaseAndCheck()) {
					double angle = DataUtil.calAngle(lat, lon, apoint.lat,
							apoint.lon);

					parent.addToSpeak(String.format(
							"Check point %s is reached, %.1f meters %s of you",
							apoint.id, aDist, DataUtil.angle2String(angle)));
					Event mesEvent = new Event(Event.CHECK_REACH, System
							.currentTimeMillis(), lat, lon);
					mesEvent.setContent(apoint.id.getBytes());
					parent.addEvent(mesEvent);
				}
			}
		}
		for (Message aMes : Repository.messages) {
			if (!aMes.isSpoken()) {
				double aDist = DataUtil.calDistance(lat, lon, aMes
						.getLatitude(), aMes.getLongitude());
				// parent.speak(String.format(".1f", aDist));
				if (aDist < reachDist) {
					if (aMes.increaseAndCheck()) {
						double angle = DataUtil.calAngle(lat, lon, aMes
								.getLatitude(), aMes.getLongitude());
						parent.addToSpeak(String.format(
								"IED point is %.1f meters %s of you. %s",
								aDist, DataUtil.angle2String(angle), aMes
										.getMes()));

					}
				}
			}
		}
	}

	public void onLocationChanged(Location location) {
		try {
			this.checkNewLocation(location.getLatitude(), location
					.getLongitude());
			this.speed = location.getSpeed();
			history.addFirst(location); // add to location history
			if (history.size() > historySize) {
				history.removeLast();
			}
			calDirection(); // calculate direction of
			Event event = new Event(Event.LOCATION, System.currentTimeMillis(),
					location.getLatitude(), location.getLongitude());
			event.setContent(NetUtil.string2bytes(String.format("%.3f %.3f#",
					this.speed, finalAngle), 20));
			parent.addEvent(event);

			// parent.updateMyLocation(location, (int) finalAngle);
			parent.updateLocation(parent.account, location.getLatitude(),
					location.getLongitude(), location.getSpeed(), finalAngle);
			// parent.mapView.getController().animateTo(this.getMyLocation());

			GeoPoint point = new GeoPoint((int) (location.getLatitude() * 1E6),
					(int) (location.getLongitude() * 1E6));
			Point pixel = new Point();
			parent.mapView.getProjection().toPixels(point, pixel);
			int width = parent.mapView.getWidth();
			int height = parent.mapView.getHeight();
			if (pixel.x < width / 5 || pixel.x > width * 4 / 5
					|| pixel.y < height / 5 || pixel.y > height * 4 / 5)
				// ct.animateTo(myLocation);
				if (Preferences.lockMyLoc) {
					ct.animateTo(point);
				}

		} catch (Exception e) {
			parent.addToSpeak("exception in " + "onLocationChanged");
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
			parent.addToSpeak("exception in " + "calDirection");
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

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

}