package combattalk.mobile.network;

public class MyThread extends Thread {
	private volatile Thread blinker;

	public MyThread() {

	}

//	public void stopThread() {
//		Thread moribund = waiter;
//		waiter = null;
//		moribund.interrupt();
//	}

	public void startThread() {
		blinker = new Thread(this);
		blinker.start();
	}
}
