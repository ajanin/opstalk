package combattalk.mobile.map;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import com.google.android.maps.GeoPoint;

import combattalk.mobile.CombatTalkView;
import combattalk.mobile.data.Event;
import combattalk.mobile.util.DataUtil;
import combattalk.mobile.util.NetUtil;

import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GeoUpdateHandler implements LocationListener {
	private double finalAngle = 0;
	private double speed = 0;
	private LinkedList<Location> history = new LinkedList<Location>();
	private int historySize = 100;
	private LocationManager locationManager;
	private CombatTalkView parent;

	public GeoUpdateHandler(CombatTalkView parent) {
		// TODO Auto-generated constructor stub
		this.parent = parent;
		locationManager = (LocationManager) parent
				.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				1, this);
		history.addLast(locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER));
	}

	public Location getLocation() {
		return history.isEmpty() ? null : history.getLast();
	}

	public double getAngle() {
		return finalAngle;
	}

	public void stop() {
		locationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		try {
			this.speed = location.getSpeed();
			history.addLast(location); // add to location history
			if (history.size() > historySize) {
				history.removeFirst();
			}
			calDirection(); // calculate direction of
			Event event = new Event(Event.LOCATION, (new Date()).getTime(),
					location.getLatitude(), location.getLongitude());
			event.setContent(NetUtil.string2bytes(this.speed + " " + finalAngle
					+ "#", 30));
			parent.addEvent(event);
			parent.updateLocation(parent.account, location.getLatitude(),
					location.getLongitude(), location.getSpeed(), finalAngle);
		} catch (Exception e) {
			parent.speak("exception in " + "onLocationChanged");
		}
	}

	private void calDirection() {
		try {
			Iterator<Location> it = history.iterator();
			if (it.hasNext()) {
				Location first = it.next();
				if (it.hasNext()) {
					Location sec = it.next();
					Point screenPts1 = new Point();
					Point screenPts2 = new Point();
					parent.mapView.getProjection().toPixels(
							new GeoPoint((int) (first.getLatitude() * 1E6),
									(int) (first.getLongitude() * 1E6)),
							screenPts1);
					parent.mapView.getProjection().toPixels(
							new GeoPoint((int) (sec.getLatitude() * 1E6),
									(int) (sec.getLongitude() * 1E6)),
							screenPts2);
					finalAngle = DataUtil.calAngle(sec.getAltitude(), sec
							.getLongitude(), first.getLatitude(), first
							.getLongitude());
				}
			}
		} catch (Exception e) {
			parent.speak("exception in " + "calDirection");
		}

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