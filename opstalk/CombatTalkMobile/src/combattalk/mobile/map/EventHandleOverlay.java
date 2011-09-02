package combattalk.mobile.map;

import java.util.Date;
import java.util.Iterator;

import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import combattalk.mobile.CombatTalkView;
import combattalk.mobile.R;
import combattalk.mobile.data.Message;
import combattalk.mobile.data.People;
import combattalk.mobile.data.Repository;
import combattalk.mobile.data.People.LocationInfo;

public class EventHandleOverlay extends Overlay {

	private CombatTalkView parent;

	public EventHandleOverlay(CombatTalkView parent) {
		this.parent = parent;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		// ---when user lifts his finger---
		try {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				Message selectMes = null;
				double minDist = 100;
				for (Iterator<Message> it = Repository.messages.iterator(); it
						.hasNext();) {
					Message mes = it.next();
					Point pt = parent.gps2Pixel(mes.getLatitude(), mes
							.getLongitude());
					double tDist = (event.getX() - pt.x)
							* (event.getX() - pt.x) + (event.getY() - pt.y)
							* (event.getY() - pt.y);
					if (tDist < minDist) {
						minDist = tDist;
						selectMes = mes;
					}
				}
				People selectPeople = null;
				for (People apeople : Repository.peopleList.values()) {
					LocationInfo loc = apeople.getLocation();
					if (loc != null) {
						Point pt = parent
								.gps2Pixel(loc.latitude, loc.longitude);
						double tDist = (event.getX() - pt.x)
								* (event.getX() - pt.x) + (event.getY() - pt.y)
								* (event.getY() - pt.y);
						if (tDist < minDist) {
							minDist = tDist;
							selectPeople = apeople;
						}
					}
				}
				if (selectPeople != null) {
					GeoPoint point = mapView.getProjection().fromPixels(
							(int) event.getX(), (int) event.getY());
					String mes = ("Name: "+selectPeople.getName()) + "\n"
							+ "ID: "+selectPeople.getId();
					parent.popUpMessage(mes, point);
				} else if (selectMes != null) {
					parent.addToSpeak(selectMes.getMes());
					GeoPoint point = mapView.getProjection().fromPixels(
							(int) event.getX(), (int) event.getY());
					String mes = (new Date(selectMes.getValidTime())) + "\n"
							+ selectMes.getUserId() + "\n" + selectMes.getMes();
					parent.popUpMessage(mes, point);
				}
				return true;
			} else
				return false;
		} catch (Exception e) {
			return false;
		}
	}
}