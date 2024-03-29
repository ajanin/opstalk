package firetalk.operators.source;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;

import firetalk.db.Repository;
import firetalk.model.CheckPoint;
import firetalk.model.DBEvent;
import firetalk.model.Enemy;
import firetalk.model.Event;
import firetalk.model.IEDPoint;
import firetalk.model.People;
import firetalk.model.RallyPoint;
import firetalk.util.NetUtil;

public class UIStreamHandle extends Thread {
	private UIServer server = null;
	private Socket conn = null;
	// private SocketChannel sc = null;
	private InputStream is = null;
	private OutputStream out = null;
	public String userId;
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
	private long transTime = 0;
	private boolean isMainDisplay = false;

	public boolean isMainDisplay() {
		return isMainDisplay;
	}

	public void setMainDisplay(boolean isMainDisplay) {
		this.isMainDisplay = isMainDisplay;
		this.addEvent(new DBEvent(DBEvent.display_change, NetUtil.value2bytes(
				isMainDisplay ? 1 : 0, 1), userId));
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
				while (blinker == outputThread && status == Status.CONNECT) {
					Thread.yield();
					if (Thread.currentThread().isInterrupted()) {
						throw new InterruptedException(
								"Stopped by ifInterruptedStop()");
					}
					while (!events.isEmpty() && status == Status.CONNECT) {
						Event event = events.removeLast();
						UIStreamHandle.this.sendEvent(event);
					}
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				handleConnectionFailure();
			}
			System.out.println(UIStreamHandle.this.userId
					+ " < OutputHandle end > ");

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
						UIStreamHandle.this.stopThread();
					} catch (Exception e) {
						e.printStackTrace();
					}
					server.removeHandle(UIStreamHandle.this);
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
			System.out.println(UIStreamHandle.this.userId
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
			System.out.println(UIStreamHandle.this.userId
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

	public UIStreamHandle(UIServer server, Socket con) {
		this.server = server;
		this.conn = con;
		this.status = Status.CONNECT;
		this.userId = con.getInetAddress().getHostAddress();
		checkHandle.start();
		failHandle.start();

	}

	public String getPeopleId() {
		return this.userId;
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

	public void sendDataBase() {

	}

	/**
	 * @param p
	 *            : send p to the device
	 */
	public boolean sendEvent(Event event) {
		try {
			if (out != null && status == Status.CONNECT) {
				if (event.getEventType() == Event.DUMMY)
					out.write(NetUtil.value2bytes(event.getEventType(), 3));
				else if (event.getEventType() == Event.DB_SYNC) {
					out.write(NetUtil.value2bytes(event.getEventType(), 3));
					out.write(NetUtil.value2bytes(
							((DBEvent) event).getDbType(), 3));
					out.write(NetUtil
							.value2bytes(event.getContent().length, 10));
					out.write(event.getContent());
				} else if (event.getTransTime() > transTime) {
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
					if (eventType == Event.DB_SYNC) {
						int dbType = (int) NetUtil.readValue(is, 3);
						int contentLen = (int) NetUtil.readValue(is, 10);
						byte[] content = NetUtil.readBytes(is, contentLen);
						Repository.storeDB(dbType, content);
						DBEvent event = new DBEvent(dbType, content, userId);
						event.setContent(content);
						server.updateEvent(event);
					} else {
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
								IEDPoint mes = new IEDPoint(this.getPeopleId(),
										new String(content), validTime, lat,
										lon);
								Repository.addIED(mes);
								// server.updateCheckPoints();
								break;
							case Event.QUERY:
							case Event.LOCATION:
								// parse location, format:<speed direction>
								String str = new String(content);
								StringTokenizer st = new StringTokenizer(str,
										" " + NetUtil.delimiter);
								double speed = Double.parseDouble(st
										.nextToken());
								System.out.println("speed: " + speed);
								double direction = Double.parseDouble(st
										.nextToken());
								break;
							case Event.CONTEXT:
							case Event.IMAGE:
							case Event.CHECK_REACH:
								System.out
										.println("check point event received");
								String cpID = "";
								for (int i = 0; i < content.length; i++)
									cpID += (char) content[i];
								for (CheckPoint cp : Repository.checkPoints) {
									if (cp.id.equals(cpID))
										cp.setReached(true);
								}
								// server.updateCheckPoints();
								break;
							case Event.AUDIO:
								File file = new File("data/audio/" + userId
										+ "_" + validTime + ".pcm");
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
									DataOutputStream dos = new DataOutputStream(
											bos);
									dos.write(content, 0, content.length);
									dos.close();
									Repository.addAudio(this.userId, file);
								} catch (Throwable t) {
									System.out.println(t.getMessage());
								}
							}

						}
						Event event = new Event(eventType, this.getPeopleId(),
								validTime, transTime, lat, lon);
						event.setContent(content);
						// server.updateEvent(event);
						Repository.transTime.put(this.userId, transTime);
					}
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
			System.out.println(this.userId + "<stop StreamHandle>");
			if (outputHandle != null)
				outputHandle.stopThread();
			if (checkHandle != null)
				checkHandle.stopThread();
			Thread tmpBlinker = blinkerThread;
			blinkerThread = null;
			if (tmpBlinker != null) {
				tmpBlinker.interrupt();
				tmpBlinker.stop();
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
			is = conn.getInputStream();
			server.addStreamHandle(this);
			this.addEvent(new DBEvent(DBEvent.IED, Repository
					.retrieveDB(DBEvent.IED), this.userId));
			this.addEvent(new DBEvent(DBEvent.objPoint, Repository
					.retrieveDB(DBEvent.objPoint), this.userId));
			this.addEvent(new DBEvent(DBEvent.rally, Repository
					.retrieveDB(DBEvent.rally), this.userId));
			this.addEvent(new DBEvent(DBEvent.wayPoint, Repository
					.retrieveDB(DBEvent.wayPoint), this.userId));
			this.addEvent(new DBEvent(DBEvent.enemy, Repository
					.retrieveDB(DBEvent.enemy), this.userId));
			out = conn.getOutputStream();
			outputHandle.start();
			System.out.println(UIStreamHandle.this.userId
					+ " Connection is established for <" + this.userId + "> ");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			this.handleConnectionFailure();
			return;
		}
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
		System.out.println(UIStreamHandle.this.userId + "< StreamHandle end >");
		// handle pool

	}
}
