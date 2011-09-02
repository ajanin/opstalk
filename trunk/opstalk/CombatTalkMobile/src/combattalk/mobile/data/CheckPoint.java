package combattalk.mobile.data;

public class CheckPoint {
	public String id;
	public double lat;
	public double lon;
	private int reachNumber = 0; // how many times this message is reached
	private boolean isObj = false;
	private boolean isReached = false;

	public boolean isObj() {
		return isObj;
	}

	public void setObj(boolean isObj) {
		this.isObj = isObj;
	}

	public void setReached(boolean isReached) {
		this.isReached = isReached;
	}

	private final int triggerN = 3;

	public CheckPoint(String id, String name, double lat, double lon,
			boolean isObj, boolean isReached) {

		this.id = id;
		this.lat = lat;
		this.lon = lon;
		this.isObj = isObj;
		this.isReached = isReached;
	}

	public void resetReachNumber() {
		reachNumber = 0;
	}

	public boolean isReached() {
		return isReached;
	}

	public boolean increaseAndCheck() {
		if (isReached)
			return false; // if already reached ,do nothing
		reachNumber++;
		if (reachNumber >= triggerN) {
			this.isReached = true;
			resetReachNumber();
			return true;
		} else
			return false;
	}
}
