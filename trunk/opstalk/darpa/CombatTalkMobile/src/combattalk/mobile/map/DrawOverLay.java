package combattalk.mobile.map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Paint.Style;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import combattalk.mobile.CombatTalkView;
import combattalk.mobile.R;
import combattalk.mobile.data.People;

public class DrawOverLay extends Overlay {
	// GeoPoint p = null;
	// int color;
	// double direction = 0; // [0,360), angle from +x axis
	CombatTalkView parent;
	Bitmap bmp = null;
	People people = null;

	public DrawOverLay(CombatTalkView parent, People people) {
		this.parent = parent;
		this.people = people;
		// bmp = BitmapFactory.decodeResource(parent.getResources(), bmpId);
	}

	// public void setDirection(double direction) {
	// this.direction = direction;
	// }
	//
	// public void setPoint(GeoPoint point) {
	// this.p = point;
	// }
	//
	// public void setPoint(double latitude, double longitude) {
	//
	// int lat = (int) (latitude * 1E6);
	// int lng = (int) (longitude * 1E6);
	// this.p = new GeoPoint(lat, lng);
	// }
	private GeoPoint getLocation() {
		if (people != null) {
			People.LocationInfo loc = people.getLocation();
			if (loc != null) {
				double lat = loc.latitude;
				double lon = loc.longitude;
				return new GeoPoint((int)(lat*1E6),(int)(lon*1E6));
			}
		}
		return null;
	}

	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
			long when) {
		try {
			super.draw(canvas, mapView, shadow);
			if (people != null) {
				// ---translate the GeoPoint to screen pixels---
				Point screenPts = new Point();
				GeoPoint point=this.getLocation();
				mapView.getProjection().toPixels(point, screenPts);
				double direction=people.getLocation().direction;
				// ---set paints----
				Paint drawPaint = new Paint();
				drawPaint.setColor(Color.BLACK);
				drawPaint.setStyle(Style.STROKE);
				drawPaint.setStrokeWidth(2);
				Paint arrowPaint = new Paint();
				arrowPaint.setColor(Color.BLUE);
				arrowPaint.setStrokeWidth(2);

				// ---add the marker---
				bmp=  BitmapFactory.decodeResource(parent.getResources(), people.getIconId());
				canvas.drawBitmap(bmp, null, new RectF(screenPts.x - 7,
						screenPts.y - 7, screenPts.x + 7, screenPts.y + 7),
						null);

				// --- calculate points for drawing arrow
				float arrowLength = 17;
				float topx = (int) (screenPts.x + arrowLength
						* Math.cos(direction));
				float topy = (int) (screenPts.y - arrowLength
						* Math.sin(direction));
				float arrowAngle1 = (float) (direction + Math.PI * 3 / 4);
				float arrowAngle2 = (float) (direction - Math.PI * 3 / 4);
				canvas.drawLine(screenPts.x, screenPts.y, topx, topy,
						arrowPaint);
				canvas.drawLine(topx, topy, (float) (topx + arrowLength / 2
						* Math.cos(arrowAngle1)), (float) (topy - arrowLength
						/ 2 * Math.sin(arrowAngle1)), arrowPaint);
				canvas.drawLine(topx, topy, (float) (topx + arrowLength / 2
						* Math.cos(arrowAngle2)), (float) (topy - arrowLength
						/ 2 * Math.sin(arrowAngle2)), arrowPaint);

				// canvas.drawCircle(screenPts.x, screenPts.y, 4, fillPaint);
				// canvas.drawCircle(screenPts.x, screenPts.y, 4, drawPaint);

				return true;
			} else
				return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}