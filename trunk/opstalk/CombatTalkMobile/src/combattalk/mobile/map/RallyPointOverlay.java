package combattalk.mobile.map;

import java.util.ArrayList;
import java.util.Iterator;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

import combattalk.mobile.data.CheckPoint;
import combattalk.mobile.data.Message;
import combattalk.mobile.data.RallyPoint;
import combattalk.mobile.data.Repository;

public class RallyPointOverlay extends ItemizedOverlay {
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

	public RallyPointOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		addOverlay(new OverlayItem(new GeoPoint(0, 0), "", ""));
		// TODO Auto-generated constructor stub
	}

	public void clear() {
		mOverlays.clear();
	}

	public void addOverlay(OverlayItem overlay) {
		mOverlays.add(overlay);
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return mOverlays.size();
	}

	public void updateInfo() {
		this.clear();
		for (RallyPoint cp : Repository.rallyList) {
			OverlayItem overlayitem = new OverlayItem(new GeoPoint(
					(int) (cp.lat * 1E6), (int) (cp.lon * 1E6)), "", "");
			this.addOverlay(overlayitem);
		}
	}

}
