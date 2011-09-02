package combattalk.mobile.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import combattalk.mobile.CombatTalkView;
import combattalk.mobile.data.Event;
import combattalk.mobile.data.Message;
import combattalk.mobile.data.People;
import combattalk.mobile.data.Repository;
import combattalk.mobile.util.NetUtil;

public class Network extends Thread {
	private volatile Thread blinkerThread = null;
	private SocketChannel sc = null;

	// Socket s = null;

	public synchronized boolean isStopped() {
		return isStopped;
	}

	public synchronized void setStopped(boolean isStopped) {
		this.isStopped = isStopped;
	}

	public synchronized boolean isHandling() {
		return isHandling;
	}

	public synchronized void setHandling(boolean isHandling) {
		this.isHandling = isHandling;
	}

	public synchronized void setStatus(Status status) {
		this.status = status;
	}

	public synchronized Status getStatus() {
		return status;
	}

	CombatTalkView parent;
	OutputStream out = null;
	InputStream input = null;
	private boolean isStopped = true;
	private boolean isHandling = false; // whether it is handling connectFails
	private Status status = Status.LOST;
	// id of the device
	public String userId = null;
	// id of the other device
	final String otherId = "server";
	// up to when the server has received data from this device
	long serverTransTime = 0;
	// InputHandle inputHandle = new InputHandle();
	private LinkedList<Event> events = new LinkedList<Event>();

	private OutputHandle outputHandle = null;
	private CheckConnectivity checkConnectThread = null;
	private HandleConnectFail handleFailThread;
	private int netId;

	public int getNetId() {
		return netId;
	}

	public enum Status {
		LOST, CONNECTED,
	}

	public Network(String user, CombatTalkView parent, int netId) {
		this.userId = user;
		this.parent = parent;
		this.netId = netId;
	}

	public void addEvent(Event event) {
		try {
			if (this.getStatus() == Status.CONNECTED && !isStopped()) {
				event.setTransTime((new Date()).getTime());
				events.addFirst(event);
			}
		} catch (Exception e) {
			e.fillInStackTrace();
		}
	}

	class CheckConnectivity extends Thread {
		private volatile Thread blinker = null;

		public void stopThread() {
			Thread tmpBlinker = blinker;
			blinker = null;
			if (tmpBlinker != null) {
				tmpBlinker.interrupt();
			}
		}

		@Override
		public void run() {
			Thread checkThread = Thread.currentThread();
			blinker = checkThread;
			try {
				while (blinker == checkThread && !isStopped()) {
					Thread.yield();
					if (Thread.currentThread().isInterrupted()) {
						throw new InterruptedException(
								"Stopped by ifInterruptedStop()");
					}
					if (events.isEmpty())
						events.addFirst(new Event(Event.DUMMY));
					if (getStatus() != Status.CONNECTED
							&& !parent.isConnected()) {
						// parent.speak("network strength is poor");
						handleConnectFailure();
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (InterruptedException e) {
				//parent.speak("check interrupted");
			}
		}
	}

	class OutputHandle extends Thread {
		private volatile Thread blinker = null;

		public void stopThread() {
			Thread tmpBlinker = blinker;
			blinker = null;
			if (tmpBlinker != null) {
				tmpBlinker.interrupt();
			}
		}

		@Override
		public void run() {
			Thread outputThread = Thread.currentThread();
			blinker = outputThread;
			try {
				while (blinker == outputThread
						&& getStatus() == Status.CONNECTED) {
					Thread.yield();
					if (Thread.currentThread().isInterrupted()) {
						throw new InterruptedException(
								"Stopped by ifInterruptedStop()");
					}
					while (!events.isEmpty()) {
						Event event = events.removeLast();
						if (getStatus() == Status.CONNECTED) {
							Network.this.sendEvent(event);
						}
					}
					// if (s == null || !s.isConnected() || s.isClosed()
					// || s.isInputShutdown() || s.isOutputShutdown()) {
					// handleConnectFailure();
					// }
					Thread.sleep(1000);

				}
			} catch (InterruptedException e) {
			//	parent.speak("interrupted output");
				// throw new RuntimeException("Interrupted", e);
			}

		}
	}

	@Override
	public void run() {
		setStopped(false);
		this.setStatus(Status.LOST);
		(handleFailThread = new HandleConnectFail()).start();
		(checkConnectThread = new CheckConnectivity()).start();
		Thread connectThread = Thread.currentThread();
		blinkerThread = connectThread;
		// String ip = "128.195.53.240";
		String ip = "169.234.133.205";
		// String ip = "128.195.185.30";
		int port = 8989;
		try {
			sc = SocketChannel.open();
			sc.connect(new InetSocketAddress(ip, port));
			// s = new Socket(ip, port);
			// out = s.getOutputStream();
			// input = s.getInputStream();
			out = Channels.newOutputStream(sc);
			input = Channels.newInputStream(sc);
			out.write(NetUtil.string2bytes(userId, 20));
			// read id of the other device
			// this.otherUser = NetUtil.readString(input, 20);
			Long transTime = Repository.transTime.get(otherId); // transTime of
			// the other device
			this.serverTransTime = (long) NetUtil.readValue(input, 20);
			out.write(NetUtil
					.value2bytes(transTime == null ? 0 : transTime, 20));

			// send context to server
			// at this point connection is established,
			parent.speak("successfully connected");
			setStatus(Status.CONNECTED);
			for (Iterator<Event> it = Repository.events.iterator(); it
					.hasNext();) {
				this.addEvent(it.next());
			}
			// start sending thread
			outputHandle = new OutputHandle();
			outputHandle.start();
			// start the receiving thread

			while (blinkerThread == connectThread
					&& getStatus() == Status.CONNECTED) {
				Thread.yield();
				if (Thread.currentThread().isInterrupted()) {

					throw new InterruptedException(
							"Stopped by ifInterruptedStop()");
				}

				receiveEvent();
			}

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			//parent.speak("input interrupted");
		} catch (Exception e) {
			//parent.speak("connect exception ");
			this.handleConnectFailure();
		}
		// parent.speak("end connect");
	}

	/**
	 * stop network component
	 */
	public void stopThread() {
		try {
			// handleConnectFailure();
			setStopped(true); // indicate the network component is stopped
			this.setStatus(Status.LOST);
			// if (s != null && !s.isClosed()) {
			// s.close();
			// s = null;
			// }
			if (checkConnectThread != null)
				checkConnectThread.stopThread();
			if (outputHandle != null)
				outputHandle.stopThread();
			Thread tmpBlinker = blinkerThread;
			blinkerThread = null;
			if (tmpBlinker != null) {
				tmpBlinker.interrupt();
			}
			if (out != null) {
				out.close();
				out = null;
			}
			if (input != null) {
				input.close();
				input = null;
			}
			if (sc != null) {
				sc.close();
				sc = null;
			}
		//	parent.speak("Connection lost");
		} catch (Exception e) {
			//parent.speak("exception while stopping network component");
		}
	}

	public boolean isAllKilled() {
		return !(this.isAlive() || outputHandle != null
				&& outputHandle.isAlive() || this.handleFailThread != null
				&& this.handleFailThread.isAlive() || this.checkConnectThread != null
				&& this.checkConnectThread.isAlive());
	}

	class HandleConnectFail extends Thread {
		@Override
		public void run() {
			while (!isStopped()) {
				if (isHandling()) {
					setStatus(Status.LOST);
					setHandling(false);
					Network.this.stopThread();
					setStopped(true);
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	private void handleConnectFailure() {
		setHandling(true);
		if (this.getStatus() == Status.CONNECTED) {
			setStatus(Status.LOST);
			parent.speak("Connection lost");
		}

	}

	public boolean receiveEvent() {
		try {
			if (this.getStatus() == Status.CONNECTED) {
				// read event type (5)
				int eventType = (int) NetUtil.readValue(input, 5);
				if (eventType != Event.DUMMY) {
					String userId = NetUtil.readString(input, 20);
					// read valid time and trans time (20)
					long validTime = (long) NetUtil.readValue(input, 20);
					long transTime = (long) NetUtil.readValue(input, 20);
					// read lat and lon (20)
					double lat = NetUtil.readValue(input, 20);
					double lon = NetUtil.readValue(input, 20);
					// read content
					int contentLen = (int) NetUtil.readValue(input, 10);
					byte[] content = NetUtil.readBytes(input, contentLen);
					if (content != null) { // parse content
						switch (eventType) {
						case Event.MESSAGE:
							Message mes = new Message(userId, new String(
									content), validTime, lat, lon);
							Repository.messages.addFirst(mes);
							parent.updateMesOverlay();
							break;
						case Event.QUERY:
						case Event.AUDIO:
						case Event.LOCATION:
							// parse location, format:<speed direction>
							String str = new String(content);
							StringTokenizer st = new StringTokenizer(str, " "
									+ NetUtil.delimiter);
							double speed = 0;
							double direction = 0;
							speed = Double.parseDouble(st.nextToken());
							direction = Double.parseDouble(st.nextToken());
							People p = Repository.peopleList.get(userId);
							if (p == null) {
								p = new People();
								p.setId(userId);
								Repository.peopleList.put(userId, p);
							}
							parent.updateLocation(p.getId(), lat, lon, speed,
									direction);
							break;
						case Event.CONTEXT:
						case Event.IMAGE:
						}
					}
					Repository.transTime.put(this.otherId, transTime);
				}
				// this.serverTransTime = transTime;
				return true;

			}
		} catch (IOException e) {

			//parent.speak("receiveEvent exception");
			this.handleConnectFailure();
		} catch (Exception e) {
			//parent.speak("other exception in receiveEvent");
			this.handleConnectFailure();
		}
		return false;

	}

	public boolean sendEvent(Event event) {
		if (event.getEventType() == Event.MESSAGE)
			Repository.addEvent(event);
		if (getStatus() == Status.CONNECTED && out != null && event != null) {
			try {
				if (event.getEventType() == Event.DUMMY)
					out.write(NetUtil.value2bytes(event.getEventType(), 5));
				else if (event.getTransTime() > this.serverTransTime) {
					// if this event have not been seen by server

					// send event type (5)
					out.write(NetUtil.value2bytes(event.getEventType(), 5));
					// send valie (20) and trans time (20)
					out.write(NetUtil.value2bytes(event.getValidTime(), 20));
					out.write(NetUtil.value2bytes(event.getTransTime(), 20));
					// send location
					out.write(NetUtil.value2bytes(event.getLatitude(), 20));
					out.write(NetUtil.value2bytes(event.getLongitude(), 20));
					// send context (depends on type)
					if (event.getContent() != null) {
						out.write(NetUtil.value2bytes(
								event.getContent().length, 10));
						out.write(event.getContent());
					} else
						out.write(NetUtil.value2bytes(0, 10));
				}
				return true; // successfully send location
			} catch (IOException e) {
				this.handleConnectFailure();
			} catch (Exception e) {
				//parent.speak("other exception in sendEvent");
				this.handleConnectFailure();
			}
		}
		return false; // fail or not send location

	}

}
