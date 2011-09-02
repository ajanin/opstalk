package speech.client;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GPSLocationListener implements LocationListener {
	public double latid=0;
	public double longid=0;
	public float accuracyd;
	public boolean gpsEnabled=false;
	public boolean netEnabled=false;
	private LocationManager lm;  //location manager
	public GPSLocationListener(LocationManager locm){
      this.lm=locm;
      lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000,
    		  3,  this);  //update location for every 2 second, 3 meters
	}
	public  Location getLocation(){
		return  lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	}
	public void onLocationChanged(Location loc) {
		if (loc != null) {
			if(loc.getProvider().equals(LocationManager.GPS_PROVIDER)){
				gpsEnabled=true;
				latid = loc.getLatitude();
			    longid = loc.getLongitude();
			}
			if(loc.getProvider().equals(LocationManager.NETWORK_PROVIDER)){
				netEnabled=true;
				if(!gpsEnabled){
					latid = loc.getLatitude();
				    longid = loc.getLongitude();
				}
			}
		    accuracyd = loc.getAccuracy(); 
		   }
	}
	public void onProviderDisabled(String provider) {
		if(provider.equals(LocationManager.GPS_PROVIDER)){
			gpsEnabled=false;
		}
		if(provider.equals(LocationManager.GPS_PROVIDER)){
			netEnabled=false;
		}
		
	}
	public void onProviderEnabled(String provider) {
		if(provider.equals(LocationManager.GPS_PROVIDER)){
			gpsEnabled=true;
		}
		if(provider.equals(LocationManager.GPS_PROVIDER)){
			netEnabled=true;
		}
		
	}
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}

}
