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
import combattalk.mobile.data.Repository;

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
					double minDist=100;
					for (Iterator<Message> it = Repository.messages.iterator(); it
							.hasNext();) {
						Message mes = it.next();
						Point pt = parent.gps2Pixel(mes.getLatitude(), mes
								.getLongitude());
						double tDist=(event.getX() - pt.x) * (event.getX() - pt.x)
						+ (event.getY() - pt.y) * (event.getY() - pt.y);
						if (tDist<minDist) {
							minDist=tDist;
							selectMes = mes;
						}
					}
//					parent.animatePoint();
					if (selectMes != null) {
						parent.speak(selectMes.getMes());
						GeoPoint point = mapView.getProjection().fromPixels(
								(int) event.getX(), (int) event.getY());
						String mes=(new Date(selectMes.getValidTime()))+"\n"+selectMes.getUserId()+"\n"+selectMes.getMes();
						parent.popUpMessage(mes, point);
				
						
						//

						// animatePoint();
						// // GeoPoint p = mapView.getProjection().fromPixels(
						// // (int) event.getX(), (int) event.getY());
						// //
						// // Geocoder geoCoder = new Geocoder(getBaseContext(),
						// Locale
						// // .getDefault());
						// // try {
						// // List<Address> addresses = geoCoder
						// // .getFromLocation(p.getLatitudeE6() / 1E6, p
						// // .getLongitudeE6() / 1E6, 1);
						// //
						// // String add = "";
						// // if (addresses.size() > 0) {
						// // for (int i = 0; i < addresses.get(0)
						// // .getMaxAddressLineIndex(); i++)
						// // add += addresses.get(0).getAddressLine(i) + "\n";
						// // }
						// //
						// // Toast.makeText(getBaseContext(),
						// ""+count+"\n"+add,
						// // Toast.LENGTH_SHORT)
						// // .show();
						// } catch (IOException e) {
						// e.printStackTrace();
						// }
					}
					return true;
				} else
					return false;
			} catch (Exception e) {
				return false;
			}
		}
	}