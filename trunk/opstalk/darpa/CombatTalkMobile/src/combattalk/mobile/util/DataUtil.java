package combattalk.mobile.util;

import combattalk.mobile.data.Config;

import android.location.Location;

public class DataUtil {
	public static String[] Directions = new String[] { "east", "northeast", "north",
		"northwest", "west", "southwest", "south", "southeast" };
	public static String angle2String(double angle) {
		int index = (int) Math
				.floor((angle - Math.PI / 8)
						/ Math.PI * 4);
		index = index < 7 ? index + 1 : 0;
		return Directions[index];
		
		
	}
	public static double calAngle(double x1, double y1, double x2, double y2) {
		float[] locResult=new float[2];
		Location.distanceBetween(x1, y1, x2, y2,
				locResult);
		double ave= locResult[1];
		ave = 360 - (ave - 90); // convert to degree from x axis
		ave = ave > 360 ? ave - 360 : ave;
		return ave / 360 * 2 * Math.PI;
	}
	public static float calDistance(double lat1, double lon1,double lat2,double lon2){
		float[] locResult=new float[1];
		Location.distanceBetween(lat1, lon1, lat2, lon2,
				locResult);
		return locResult[0];
	}
	public static float calBearing(double lat1, double lon1,double lat2,double lon2){
		float[] locResult=new float[2];
		Location.distanceBetween(lat1, lon1, lat2, lon2,
				locResult);
		return locResult[1];
	}
}
