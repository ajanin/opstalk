package combattalk.mobile.map;

import java.util.ArrayList;
import java.util.Iterator;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import combattalk.mobile.data.Message;
import combattalk.mobile.data.Repository;

public class HelloItemizedOverlay extends ItemizedOverlay {
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

	public HelloItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		addOverlay(new OverlayItem(new GeoPoint(0,0),
				"", ""));
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
		for (Iterator<Message> it = Repository.messages.iterator(); it
				.hasNext();) {
			Message mes = it.next();
			OverlayItem overlayitem = new OverlayItem(new GeoPoint((int) (mes
					.getLatitude() * 1E6), (int) (mes.getLongitude() * 1E6)),
					"", "");
			this.addOverlay(overlayitem);
		}
	}

}
