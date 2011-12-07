package firetalk.UI;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.StringTokenizer;

import firetalk.db.Repository;
import firetalk.db.UIRepository;
import firetalk.model.CheckPoint;
import firetalk.model.DBEvent;
import firetalk.model.Enemy;
import firetalk.model.Event;
import firetalk.model.IEDPoint;
import firetalk.model.People;
import firetalk.util.NetUtil;

public class UIClient extends Thread {
	private volatile Thread blinkerThread = null;
	private Socket s = null;

	public boolean isStopped() {
		return isStopped;
	}

	public void setStopped(boolean isStopped) {
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

	MainWindow parent;
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
	// private CheckConnectivity checkConnectThread = null;
	private HandleConnectFail handleFailThread;
	private int netId;

	public int getNetId() {
		return netId;
	}

	public enum Status {
		LOST, CONNECTED,
	}

	public UIClient(MainWindow parent) {
		this.parent = parent;
	}

	public void addEvent(Event event) {
		try {
			if (this.getStatus() == Status.CONNECTED && !isStopped()) {
				event.setTransTime(System.currentTimeMillis());
				events.addFirst(event);
			}
		} catch (Exception e) {
			e.fillInStackTrace();
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
							UIClient.this.sendEvent(event);

						}
					}
					Thread.sleep(1000);

				}
			} catch (InterruptedException e) {
				handleConnectFailure();
			}

		}
	}

	@Override
	public void run() {
		setStopped(false);
		this.setStatus(Status.LOST);
		(handleFailThread = new HandleConnectFail()).start();
		// (checkConnectThread = new CheckConnectivity()).start();
		Thread connectThread = Thread.currentThread();
		blinkerThread = connectThread;
		// String ip = "128.195.53.240";
		String ip = "169.234.133.205";
		// String ip = "128.195.185.30";
		int port = 9001;
		try {

			s = new Socket(ip, port);
			userId = s.getLocalAddress().getHostAddress();
			out = s.getOutputStream();
			input = s.getInputStream();
			outputHandle = new OutputHandle();
			outputHandle.start();
			this.setStatus(Status.CONNECTED);
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

		} catch (Exception e) {
			// parent.speak("connect exception ");
			this.handleConnectFailure();
		}
	}

	/**
	 * stop network component
	 */
	public void stopThread() {
		try {
			setStopped(true); // indicate the network component is stopped
			this.setStatus(Status.LOST);
			if (outputHandle != null)
				outputHandle.stopThread();
			Thread tmpBlinker = blinkerThread;
			blinkerThread = null;
			if (tmpBlinker != null) {
				tmpBlinker.interrupt();
				// tmpBlinker.stop();
			}
			if (s != null && !s.isClosed()) {
				s.close();
				s = null;
			}
			if (out != null) {
				out.close();
				out = null;
			}
			if (input != null) {
				input.close();
				input = null;
			}
			if (outputHandle != null && outputHandle.isAlive())
				outputHandle.stop();
			this.stop();
		} catch (Exception e) {
			handleConnectFailure();
		}
	}

	public boolean isAllKilled() {
		return !(this.isAlive() || outputHandle != null
				&& outputHandle.isAlive() || this.handleFailThread != null
				&& this.handleFailThread.isAlive());
	}

	class HandleConnectFail extends Thread {
		@Override
		public void run() {
			while (!isStopped()) {
				if (isHandling()) {
					setStatus(Status.LOST);
					setHandling(false);
					UIClient.this.stopThread();
					setStopped(true);
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private void handleConnectFailure() {
		setHandling(true);
		if (this.getStatus() == Status.CONNECTED) {
			setStatus(Status.LOST);

		}

	}

	private void updateDB(int dbType, byte[] content) {
		try {
			UIRepository.storeDB(dbType, content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (dbType == DBEvent.IED) {
			parent.updateIEDList();
		} else if (dbType == DBEvent.rally) {
			parent.updateRallyList();
		} else if (dbType == DBEvent.objPoint || dbType == DBEvent.wayPoint) {
			parent.updateObjList();
		}

	}

	private boolean receiveEvent() {
		try {
			if (this.getStatus() == Status.CONNECTED) {
				// read event type (5)
				int eventType = (int) NetUtil.readValue(input, 3);
				if (eventType != Event.DUMMY) {
					if (eventType == Event.DB_SYNC) {
						int dbType = (int) NetUtil.readValue(input, 3);
						int contentLen = (int) NetUtil.readValue(input, 10);
						byte[] content = NetUtil.readBytes(input, contentLen);
						this.updateDB(dbType, content);
					} else {
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
								IEDPoint mes = new IEDPoint(userId, new String(
										content), validTime, lat, lon);
								if (mes.getMes().equals("$enemy$"))
									UIRepository
											.addEnemy(new Enemy(mes
													.getLatitude(), mes
													.getLongitude()));
								else
									UIRepository.addIED(mes);
								parent.updateIEDList();
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
								People user = UIRepository.peopleList
										.get(userId);
								user.addLocation(lon, lat, speed, direction);
								break;
							case Event.CONTEXT:
							case Event.IMAGE:
							case Event.CHECK_REACH:
								System.out
										.println("check point event received");
								String cpID = "";
								for (int i = 0; i < content.length; i++)
									cpID += (char) content[i];
								for (CheckPoint cp : UIRepository.checkPoints) {
									if (cp.id.equals(cpID))
										cp.setReached(true);
								}
								parent.updateObjList();
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
									UIRepository.addAudio(this.userId, file);
								} catch (Throwable t) {
									System.out.println(t.getMessage());
								}
							}

						}
						Event event = new Event(eventType, userId, validTime,
								transTime, lat, lon);
						event.setContent(content);
						parent.addEvent2UI(event);
						UIRepository.transTime.put(this.otherId, transTime);
					}
				}
				// this.serverTransTime = transTime;
				return true;

			}
		} catch (IOException e) {

			// parent.speak("receiveEvent exception");
			this.handleConnectFailure();
		} catch (Exception e) {
			// parent.speak("other exception in receiveEvent");
			this.handleConnectFailure();
		}
		return false;

	}

	public boolean sendEvent(Event event) {
		if (getStatus() == Status.CONNECTED && out != null && event != null) {
			try {
				if (event.getEventType() == Event.DUMMY)
					out.write(NetUtil.value2bytes(event.getEventType(), 3));
				else if (event.getEventType() == Event.DB_SYNC) {
					out.write(NetUtil.value2bytes(event.getEventType(), 3));
					out.write(NetUtil.value2bytes(
							((DBEvent) event).getDbType(), 3));
					out.write(NetUtil
							.value2bytes(event.getContent().length, 10));
					out.write(event.getContent());
				} else if (event.getTransTime() > this.serverTransTime) {
					// if this event have not been seen by server

					// send event type (2)
					out.write(NetUtil.value2bytes(event.getEventType(), 3));
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
				// parent.speak("other exception in sendEvent");
				this.handleConnectFailure();
			}
		}
		return false; // fail or not send location

	}

}
