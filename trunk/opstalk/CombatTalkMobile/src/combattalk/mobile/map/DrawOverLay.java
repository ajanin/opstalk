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
import combattalk.mobile.data.CheckPoint;
import combattalk.mobile.data.People;
import combattalk.mobile.data.RallyPoint;
import combattalk.mobile.data.Repository;

public class DrawOverLay extends Overlay {
	CombatTalkView parent;
	Bitmap pImg = null;
	Bitmap wayImg = null;
	Bitmap objImg = null;
	Bitmap wayImg2 = null;
	Bitmap objImg2 = null;
	Bitmap rallyImg=null;
//	People people = null;

	public DrawOverLay(CombatTalkView parent) {
		this.parent = parent;
//		this.people = people;
		pImg = BitmapFactory.decodeResource(parent.getResources(),
				R.drawable.friend);
		wayImg = BitmapFactory.decodeResource(parent.getResources(),
				R.drawable.waypoint);
		objImg = BitmapFactory.decodeResource(parent.getResources(),
				R.drawable.objpoint);
		wayImg2 = BitmapFactory.decodeResource(parent.getResources(),
				R.drawable.waypoint2);
		objImg2 = BitmapFactory.decodeResource(parent.getResources(),
				R.drawable.objpoint2);
		rallyImg = BitmapFactory.decodeResource(parent.getResources(),
				R.drawable.rallypoint);
		// bmp = BitmapFactory.decodeResource(parent.getResources(), bmpId);
	}

	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
			long when) {
		try {
			super.draw(canvas, mapView, shadow);
			Point screenPts = new Point();
			int markerSize = 10;
			// if (people != null) {
			for (People people : Repository.peopleList.values()) {
				// ---translate the GeoPoint to screen pixels---

				GeoPoint point = people.getGeoLocation();
				if (point != null) {
					mapView.getProjection().toPixels(point, screenPts);
					double direction = people.getLocation().direction;
					// ---set paints----
					Paint drawPaint = new Paint();
					drawPaint.setColor(Color.BLACK);
					drawPaint.setStyle(Style.STROKE);
					drawPaint.setStrokeWidth(2);
					Paint arrowPaint = new Paint();
					arrowPaint.setColor(Color.BLUE);
					arrowPaint.setStrokeWidth(2);

					// ---add the marker---
					// pImg =
					// BitmapFactory.decodeResource(parent.getResources(),
					// people.getIconId());
					canvas.drawBitmap(
							pImg,
							null,
							new RectF(screenPts.x - markerSize, screenPts.y
									- markerSize * pImg.getHeight()
									/ pImg.getWidth(),
									screenPts.x + markerSize, screenPts.y
											+ markerSize * pImg.getHeight()
											/ pImg.getWidth()), null);
					canvas.drawText(people.getRankName(), screenPts.x,
							screenPts.y, drawPaint);
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
					canvas.drawLine(
							topx,
							topy,
							(float) (topx + arrowLength / 2
									* Math.cos(arrowAngle1)),
							(float) (topy - arrowLength / 2
									* Math.sin(arrowAngle1)), arrowPaint);
					canvas.drawLine(
							topx,
							topy,
							(float) (topx + arrowLength / 2
									* Math.cos(arrowAngle2)),
							(float) (topy - arrowLength / 2
									* Math.sin(arrowAngle2)), arrowPaint);
				}
			}
			// draw way points
			Paint yellowPaint = new Paint();
			Paint greenPaint = new Paint();
			yellowPaint.setColor(Color.YELLOW);
			greenPaint.setColor(Color.GREEN);
			for (CheckPoint cp : Repository.checkPoints) {
				GeoPoint gp = new GeoPoint((int) (cp.lat * 1E6),
						(int) (cp.lon * 1E6));
				parent.mapView.getProjection().toPixels(gp, screenPts);
				// canvas.drawRect(screenPts.x - 10, screenPts.y - 4,
				// screenPts.x + 10, screenPts.y + 4,
				// cp.isReached() ? greenPaint : yellowPaint);
				if (cp.isObj()) {
					if (cp.isReached())
						canvas.drawBitmap(objImg2, null, new RectF(screenPts.x
								- markerSize, screenPts.y - markerSize,
								screenPts.x + markerSize, screenPts.y
										+ markerSize), null);
					else
						canvas.drawBitmap(objImg, null, new RectF(screenPts.x
								- markerSize, screenPts.y - markerSize,
								screenPts.x + markerSize, screenPts.y
										+ markerSize), null);
				} else {
					if (cp.isReached())
						canvas.drawBitmap(wayImg2, null, new RectF(screenPts.x
								- markerSize, screenPts.y - markerSize,
								screenPts.x + markerSize, screenPts.y
										+ markerSize), null);
					else
						canvas.drawBitmap(wayImg, null, new RectF(screenPts.x
								- markerSize, screenPts.y - markerSize,
								screenPts.x + markerSize, screenPts.y
										+ markerSize), null);
				}
			}
			//draw rally point
			for(RallyPoint rp:Repository.rallyList){
				GeoPoint gp = new GeoPoint((int) (rp.lat * 1E6),
						(int) (rp.lon * 1E6));
				parent.mapView.getProjection().toPixels(gp, screenPts);
				canvas.drawBitmap(rallyImg, null, new RectF(screenPts.x
						- markerSize, screenPts.y - markerSize,
						screenPts.x + markerSize, screenPts.y
								+ markerSize), null);
				
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}