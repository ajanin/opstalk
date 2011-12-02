package combattalk.sr;

import java.util.HashMap;
import java.util.Locale;

import android.location.Location;

// Parse American English spoken numbers.
//
// E.g. ParseNum.txt2long("three thousand twenty two") return 3022.
//
// NOTE: Uses American conventions, so no "thousand million" for "billion".
//
// NOTE: Currently, "a" is exactly the same as "one". This makes
// handling "a hundred and three" easy, but will incorrectly pass
// "seven a two" as 712.
//
// It allows seldom spoken patterns like "eleven four" for 114.
//

public class ParseNum {
	
	// Variables and methods to supporting parsing of raw numbers.
	static HashMap<String, Long> digits = new HashMap<String, Long>();
	static HashMap<String, Long> teens = new HashMap<String, Long>();
	static HashMap<String, Long> decades = new HashMap<String, Long>();
	static HashMap<String, Long> exponents = new HashMap<String, Long>();

	static {
		digits.put("zero", 0L);
		digits.put("oh", 0L);
		digits.put("a", 1L);    // NOTE: This will allow some ill formed strings like "a three"
		digits.put("one", 1L);
		digits.put("two", 2L);
		digits.put("three", 3L);
		digits.put("four", 4L);
		digits.put("five", 5L);
		digits.put("six", 6L);
		digits.put("seven", 7L);
		digits.put("eight", 8L);
		digits.put("nine", 9L);
		digits.put("niner", 9L);  // Military speak.

		teens.put("ten", 10L);
		teens.put("eleven", 11L);
		teens.put("twelve", 12L);
		teens.put("thirteen", 13L);
		teens.put("fourteen", 14L);
		teens.put("fifteen", 15L);
		teens.put("sixteen", 16L);
		teens.put("seventeen", 17L);
		teens.put("eightteen", 18L);
		teens.put("nineteen", 19L);

		decades.put("twenty", 20L);
		decades.put("thirty", 30L);
		decades.put("forty", 40L);
		decades.put("fifty", 50L);
		decades.put("sixty", 60L);
		decades.put("seventy", 70L);
		decades.put("eighty", 80L);
		decades.put("ninety", 90L);

		// "hundred" is handled specially since it can occur in multiple places.
		exponents.put("thousand", 1000L);
		exponents.put("million", 1000000L);
		exponents.put("billion", 1000000000L);

	} // End of static initialization

	public static long text2long(String instring) throws NumberFormatException {
		instring = instring.replaceAll("\\band\\b", "").toLowerCase(Locale.ENGLISH);
		instring = instring.trim();
		String[] words = instring.split("\\s+");
		return array2long(words, 0);
	}
	
	// Parse the array of strings starting at element ii
	private static long array2long(String[] words, int startii) throws NumberFormatException {
		long val = 0;
		long cur;
		for (int ii = startii; ii < words.length; ii++) {
			if (isdigit(words[ii])) {
				cur = digits.get(words[ii]);
				if (ii == startii) {
					// three
					val = cur;
				} else {
					if (isdigit(words[ii-1]) || isteen(words[ii-1])) {
						// one three
						// twelve three (uncommon, but could happen)
						val = val * 10 + cur;
					} else if (isdecade(words[ii-1]) || ishundred(words[ii-1])) {
						// twenty three
						// three hundred three
						val = val + cur;
					} else {
						throw new NumberFormatException(words[ii-1], "Unknown or unexpected type before digit");
					}
				}
			} else if (isteen(words[ii]) || isdecade(words[ii])) {
				if (isteen(words[ii])) {
					cur = teens.get(words[ii]);
				} else {
					cur = decades.get(words[ii]);
				}
				if (ii == startii) {
					// thirteen
					val = cur;
				} else {
					if (isdigit(words[ii-1]) || isteen(words[ii-1]) || isdecade(words[ii-1])) {
						// one thirteen
						// nineteen thirteen
						// twenty thirteen
						val = val * 100 + cur;
					} else if (ishundred(words[ii-1])) {
						// two hundred thirteen
						val = val + cur;
					} else {
						throw new NumberFormatException(words[ii-1], "Unknown or unexpected type before teen or decade");
					}
				}
			} else if (ishundred(words[ii])) {
				if (ii == startii) {
					// *hundred
					val = 100;
				} else {
					val = val * 100;
				}
			} else if (isexponent(words[ii])) {
				cur = exponents.get(words[ii]);
				// NOTE: recursive call plus an early return
				if (ii == startii) {
					// *thousand
					val = cur;
				} else {
					val = val * cur;
				}
				return val+array2long(words, ii+1);
			} else {
				throw new NumberFormatException(words[ii], "Unknown type");
			}
		}
		return val;
	}
	
	private static boolean ishundred(String val) {
		return val.equals("hundred");
	}
	
	private static boolean isdigit(String val) {
		return digits.containsKey(val);
	}
	
	private static boolean isteen(String val) {
		return teens.containsKey(val);
	}
	
	private static boolean isdecade(String val) {
		return decades.containsKey(val);
	}
	
	private static boolean isexponent(String val) {
		return exponents.containsKey(val);
	}
	
	// Variables and methods to supporting parsing of directions
	
	static HashMap<String, Double> bearings = new HashMap<String, Double>();

	static {
		bearings.put("N", 0.00);
		bearings.put("NNE", 22.50);
		bearings.put("NE", 45.00);
		bearings.put("ENE", 67.50);
		bearings.put("E", 90.00);
		bearings.put("ESE", 112.50);
		bearings.put("SE", 135.00);
		bearings.put("SSE", 157.50);
		bearings.put("S", 180.00);
		bearings.put("SSW", 202.50);
		bearings.put("SW", 225.00);
		bearings.put("WSW", 247.50);
		bearings.put("W", 270.00);
		bearings.put("WNW", 292.50);
		bearings.put("NW", 315.00);
		bearings.put("NNW", 337.50);
	}
	
	public static boolean isBearing(String in) {
		return bearings.containsKey(in);
	}
	
	public static double bearingStringToDegrees(String in) {
		return bearings.get(in);
	}
	
	private static double deg2rad(double v) {
		return v*Math.PI/180.0;
	}
	
	private static double rad2deg(double v) {
		return v*180.0/Math.PI;
	}
	
	public static Location addBearingDistance(Location inloc,
											   double distanceInMeters, double bearingDegreesEastofNorth) {
		double rdistance = distanceInMeters / 6371000.0; // Mean spherical radius of Earth in meters
		double rbearing = -deg2rad(bearingDegreesEastofNorth);
		double rinlat = deg2rad(inloc.getLatitude());
		double rinlon = deg2rad(inloc.getLongitude());
		double routlat = Math.asin(Math.sin(rinlat)*Math.cos(rdistance) + 
						Math.cos(rinlat)*Math.sin(rdistance)*Math.cos(rbearing));
		double routlon;
		if (Math.abs(Math.cos(routlat)) < 0.00001) {  // Endpoint a pole
			routlon=rinlon;      
		} else {
			routlon = ((rinlon-Math.asin(Math.sin(rbearing)*Math.sin(rdistance)/Math.cos(routlat))
					+Math.PI) % (2*Math.PI)) - Math.PI;
		}
		Location outloc = new Location(inloc);
		outloc.setLatitude(rad2deg(routlat));
		outloc.setLongitude(rad2deg(routlon));
		return outloc;
	}
}

// Serializability is not yet supported, so just suppress warning.
@SuppressWarnings("serial")
class NumberFormatException extends Exception {
	public String object;
	public String error_message;
	
	public NumberFormatException(String obj, String errmsg) {
		super(errmsg);
		object = obj;
		error_message = errmsg;
	}
}
