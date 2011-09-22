package firetalk.operators.source;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;
import firetalk.db.Repository;
import firetalk.model.CheckPoint;
import firetalk.model.Event;
import firetalk.model.ObjPoint;
import firetalk.model.People;
import firetalk.model.IEDPoint;
import firetalk.model.RallyPoint;
import firetalk.util.NetUtil;

public class StreamHandle extends Thread {
	private Server server = null;
	private Socket conn = null;
	// private SocketChannel sc = null;
	private InputStream is = null;
	private OutputStream out = null;
	private String userId;
	private People user = null;
	private Status status;
	private boolean isStopped = false;
	private OutputHandle outputHandle = new OutputHandle();
	private CheckConnectivity checkHandle = new CheckConnectivity();
	private HandleConnectFail failHandle = new HandleConnectFail();
	String filename = null;
	int readInbytes = 0;// number of bytes read in the buffer
	private LinkedList<Event> events = new LinkedList<Event>(); // events to
	private boolean isHandling;
	private volatile Thread blinkerThread;
	private int id;
	private long transTime = 0;

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
				while (blinker == outputThread && status == Status.CONNECT) {
					Thread.yield();
					if (Thread.currentThread().isInterrupted()) {
						throw new InterruptedException(
								"Stopped by ifInterruptedStop()");
					}
					while (!events.isEmpty() && status == Status.CONNECT) {
						Event event = events.removeLast();
						StreamHandle.this.sendEvent(event);
					}
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				handleConnectionFailure();
			}
			System.out.println(StreamHandle.this.id + " < OutputHandle end > ");

		}
	}

	@Override
	public String toString() {
		return this.userId;
	}

	class HandleConnectFail extends Thread {
		@Override
		public void run() {
			while (!isStopped) {
				if (isHandling) {
					System.out.println("Handling connection fail");
					status = Status.LOST;
					isHandling = false;
					try {
						StreamHandle.this.stopThread();
					} catch (Exception e) {
						e.printStackTrace();
					}
					server.removeHandle(StreamHandle.this);
					isStopped = true;
					break;
				}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println(StreamHandle.this.id
					+ " < HandleConnectFail end >");
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
				while (blinker == checkThread && status == Status.CONNECT
						&& !isStopped) {
					Thread.yield();
					if (Thread.currentThread().isInterrupted()) {
						throw new InterruptedException(
								"Stopped by ifInterruptedStop()");
					}
					// if (events.isEmpty())
					// events.addFirst(new Event(Event.DUMMY));

					Thread.sleep(4000);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(StreamHandle.this.id
					+ " < Check conectivity end >");
		}
	}

	public boolean isAllKilled() {
		return !(this.isAlive() || outputHandle != null
				&& outputHandle.isAlive() || this.failHandle != null
				&& this.failHandle.isAlive() || this.checkHandle != null
				&& this.checkHandle.isAlive());
	}

	public enum Status {
		CONNECT, LOST
	}

	public int getHandleId() {
		return this.id;
	}

	public StreamHandle(Server server, Socket con, int id) {
		this.server = server;
		// this.sc = con;
		this.conn = con;
		this.status = Status.CONNECT;
		this.id = id;
		checkHandle.start();
		failHandle.start();

	}

	public String getPeopleId() {
		return user == null ? null : user.getId();
	}

	public void addEvent(Event event) {
		if (status == Status.CONNECT) {
			event.setTransTime(System.currentTimeMillis());
			events.addFirst(event);
			if (!event.getId().equals(this.userId))
				System.out.println("Add event from other device: " + event);
		}
	}

	public String getUserId() {
		return userId;
	}

	/**
	 * @param p
	 *            : send p to the device
	 */
	public boolean sendEvent(Event event) {
		try {
			if (out != null && status == Status.CONNECT
					&& this.getPeopleId() != null
					&& !this.getPeopleId().equals(event.getId())) {
				if (event.getEventType() == Event.DUMMY)
					out.write(NetUtil.value2bytes(event.getEventType(), 3));
				else if (event.getTransTime() > transTime) {
					System.out.print("<sendEvent>: to " + this.getPeopleId()
							+ " [" + event);
					// send event type
					out.write(NetUtil.value2bytes(event.getEventType(), 3));
					// send id of device who generate the event
					out.write(NetUtil.string2bytes(event.getId(), 20));
					// send valid time
					out.write(NetUtil.value2bytes(event.getValidTime(), 20));
					// send trans time
					out.write(NetUtil.value2bytes(event.getTransTime(), 20));
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
				System.out.println("finish sending");
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.handleConnectionFailure();

		}
		return false;
	}

	private synchronized void handleConnectionFailure() {
		isHandling = true;
		status = Status.LOST;
	}

	private void receiveEvent() {
		try {
			// Db.storeEvent(Event event);
			if (this.status == Status.CONNECT) {
				// read event type (5)
				int eventType = (int) NetUtil.readValue(is, 3);
				// read valid time and trans time (20)
				if (eventType != Event.DUMMY) {
					long validTime = (long) NetUtil.readValue(is, 20);
					long transTime = (long) NetUtil.readValue(is, 20);
					// read lat and lon (20)
					double lat = NetUtil.readValue(is, 20);
					double lon = NetUtil.readValue(is, 20);
					// read content
					int contentLen = (int) NetUtil.readValue(is, 10);
					byte[] content = NetUtil.readBytes(is, contentLen);
					if (content != null) { // parse content
						switch (eventType) {
						case Event.MESSAGE:
							IEDPoint mes = new IEDPoint(this.user.getId(),
									new String(content), validTime, lat, lon);
							Repository.addIED(mes);
							server.updateCheckPoints();
							break;
						case Event.QUERY:
						case Event.LOCATION:
							// parse location, format:<speed direction>
							String str = new String(content);
							StringTokenizer st = new StringTokenizer(str, " "
									+ NetUtil.delimiter);
							double speed = Double.parseDouble(st.nextToken());
							System.out.println("speed: " + speed);
							double direction = Double.parseDouble(st
									.nextToken());
							this.user.addLocation(lon, lat, speed, direction);
							break;
						case Event.CONTEXT:
						case Event.IMAGE:
						case Event.CHECK_REACH:
							System.out.println("check point event received");
							String cpID = "";
							for (int i = 0; i < content.length; i++)
								cpID += (char) content[i];
							for (CheckPoint cp : Repository.checkPoints) {
								if (cp.id.equals(cpID))
									cp.setReached(true);
							}
							server.updateCheckPoints();
							break;
						case Event.AUDIO:
							System.out.println("audio received");
							File file = new File("data/audio/" + userId + "_"
									+ validTime + ".wav");
							// Delete any previous recording.
							if (file.exists())
								file.delete();

							// Create the new file.
							try {
								file.createNewFile();
							} catch (IOException e) {
								System.out.println("Failed to create "
										+ file.toString());
								break;
							}
							try {
								OutputStream os = new FileOutputStream(file);
								BufferedOutputStream bos = new BufferedOutputStream(
										os);
								DataOutputStream dos = new DataOutputStream(bos);
								dos.write(content);
								dos.close();
							} catch (Throwable t) {
								System.out.println(t.getMessage());
							}
						}

					}
					Event event = new Event(eventType, this.user.getId(),
							validTime, transTime, lat, lon);
					event.setContent(content);
					server.updateEvent(event);
					server.parent.addEventResponse(event);
					Repository.transTime.put(this.userId, transTime);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.handleConnectionFailure();
			// e.printStackTrace();
		}
	}

	public void receiveContext() {
		/*
		 * receive context from device and save
		 */
	}

	public synchronized void stopThread() {
		try {
			this.status = Status.LOST;
			System.out.println(this.id + "<stop StreamHandle>");
			if (outputHandle != null)
				outputHandle.stopThread();
			if (checkHandle != null)
				checkHandle.stopThread();
			Thread tmpBlinker = blinkerThread;
			blinkerThread = null;
			if (tmpBlinker != null) {
				tmpBlinker.interrupt();
				// tmpBlinker.stop();
			}
			if (conn != null && !conn.isClosed()) {
				conn.close();
				conn = null;
			}
			if (out != null) {
				out.close();
				out = null;
			}
			if (is != null) {
				is.close();
				is = null;
			}

			// if (sc != null) {
			// sc.close();
			// sc = null;
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		Thread handleThread = Thread.currentThread();
		blinkerThread = handleThread;
		try {
			this.events.clear();
			// is = Channels.newInputStream(sc);
			is = conn.getInputStream();
			userId = NetUtil.readString(is, 20); // get id of the device
			server.addStreamHandle(this);
			// out = Channels.newOutputStream(sc);
			out = conn.getOutputStream();
			// out = conn.getOutputStream();
			Long transTime = Repository.transTime.get(userId);
			out.write(NetUtil
					.value2bytes(transTime == null ? 0 : transTime, 20));
			this.transTime = (long) NetUtil.readValue(is, 20);

			// send fixed points, including obj, way, rally point
			Vector<CheckPoint> cpForUser = new Vector<CheckPoint>();
			for (CheckPoint cp : Repository.checkPoints) {
				if (cp.userID.equals(this.userId)) {
					cpForUser.add(cp);
				}
			}
			out.write(NetUtil.value2bytes(cpForUser.size(), 10));
			for (CheckPoint cp : cpForUser) {
				out.write(NetUtil.string2bytes(cp.id, 10));
				out.write(NetUtil.string2bytes("random", 10));
				out.write(NetUtil.value2bytes(cp.lat, 20));
				out.write(NetUtil.value2bytes(cp.lon, 20));
				out.write(NetUtil.string2bytes(cp.isObj() ? "1" : "0", 2));
				out.write(NetUtil.string2bytes(cp.isReached() ? "1" : "0", 2));
			}
			Vector<RallyPoint> rallyForUser = new Vector<RallyPoint>();
			for (RallyPoint cp : Repository.rallyList) {
				if (cp.userID.equals(this.userId)) {
					rallyForUser.add(cp);
				}
			}
			out.write(NetUtil.value2bytes(rallyForUser.size(), 10));
			for (RallyPoint cp : rallyForUser) {
				out.write(NetUtil.string2bytes(cp.id, 10));
				out.write(NetUtil.string2bytes("random", 10));
				out.write(NetUtil.value2bytes(cp.lat, 20));
				out.write(NetUtil.value2bytes(cp.lon, 20));
				out.write(NetUtil.string2bytes(cp.isReached() ? "1" : "0", 2));
			}
			receiveContext();
			for (IEDPoint p : Repository.IEDList) {
				Event event = new Event(Event.MESSAGE, p.getUserId(),
						p.getValidTime(), System.currentTimeMillis(),
						p.getLatitude(), p.getLongitude());
				String mes = p.getMes();
				byte[] content = null;
				if (mes != null) {
					content = new byte[mes.length()];
					for (int i = 0; i < mes.length(); i++) {
						content[i] = (byte) mes.charAt(i);
					}
				}
				event.setContent(content);
				this.addEvent(event);

			}
			// // send dynamic info, including IED points
			// for (Iterator<Event> it = Repository.events.iterator(); it
			// .hasNext();)
			// this.events.addFirst(it.next());
			// connection is established
			outputHandle.start();
			System.out.println(StreamHandle.this.id
					+ " Connection is established for <" + userId + "> ");
			try {
				FileWriter fw = new FileWriter(new File("log.txt"), true);
				fw.write(StreamHandle.this.id
						+ " Connection is established for <" + userId + "> \n");
			} catch (Exception e) {

			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			this.handleConnectionFailure();
			return;
		}
		People p = Repository.peopleList.get(userId);
		if (p == null) {
			Repository.peopleList.put(userId, new People());
			p = Repository.peopleList.get(userId);
			p.setId(userId);
		}
		this.user = p;
		try {
			while (blinkerThread == handleThread && status == Status.CONNECT) {
				Thread.yield();
				if (Thread.currentThread().isInterrupted()) {
					throw new InterruptedException(
							"Stopped by ifInterruptedStop()");
				}
				receiveEvent();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(StreamHandle.this.id + "< StreamHandle end >");
		// handle pool

	}
}
