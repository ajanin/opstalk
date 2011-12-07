package firetalk.operators.source;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import firetalk.UI.MainWindow;
import firetalk.db.Repository;
import firetalk.model.Event;

public class UIServer extends Thread {
	final int maxConnection = 100;
	private ServerManagerUI parent;

	// public MainWindow parent;

	// ContextManager contextManager = new ContextManager();
	// public UIServer(MainWindow parent) {
	// this.parent = parent;
	// }

	// public void updateCheckPoints() {
	// parent.updateMarkers();
	// parent.updateList();
	// }

	public UIServer(ServerManagerUI parent) {
		this.parent = parent;
	}

	@Override
	public void run() {
		try {
			InetAddress addr = InetAddress.getByName("169.234.133.205");
			// ServerSocketChannel ssChannel1 = ServerSocketChannel.open();
			// ssChannel1.socket().bind(new InetSocketAddress(addr, 8989));

			ExecutorService service = Executors
					.newFixedThreadPool(maxConnection);
			int id = 0;
			// service.execute(contextManager);
			ServerSocket ss = new ServerSocket(9001);
			while (true) {
				// SocketChannel sChannel = ssChannel1.accept();
				Socket s = ss.accept();
				// System.out.println("new connection: " + sChannel);
				UIStreamHandle handle = new UIStreamHandle(this, s);
				id++;
				// contextManager.addStreamHandle(handle); // add stream handle
				// to
				// stream handle pool
				service.execute(handle);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void removeHandle(UIStreamHandle handle) {
		System.out.println("<Connection Lost>: " + handle.getPeopleId());
		// contextManager.removeStreamHandle(handle);
		// handle.stopAll();
		String id = handle.getUserId();
		if (id != null && Repository.handles.containsValue(handle)) {
			Repository.handles.remove(id);
		}
	}

	public synchronized void addStreamHandle(UIStreamHandle handle) {
		int prevN = Repository.handles.size();
		String id = handle.userId;
		UIStreamHandle h = null;
		if (id != null) {
			h = Repository.handles.get(id);
			if (h != null) {
				System.out.println("Thread " + h.getHandleId() + "is to end");
				h.stopThread();
				Repository.handles.remove(id);
			}

			try {
				if (h != null && !h.isAllKilled())
					Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (Repository.handles.get(id) != null)
				System.out.println("<Replace happens>");
			Repository.handles.put(id, handle);
			parent.updateUIList();
			System.out.println(handle.getHandleId() + " < Add " + id
					+ " to Repository.handles > ");
		}
		if (h != null) {
			if (prevN != Repository.handles.size())
				System.out
						.println("\n*******************\nRepository.handles error: Prev: "
								+ prevN
								+ "after: "
								+ Repository.handles.size()
								+ "\n***************");
			if (!h.isAllKilled())
				System.out.println("<<<<<Not all killed>>>>");
		}
	}

	/**
	 * @param people
	 *            : people whose information is updated notify other devices
	 *            that this people is updated
	 */
	public void updateEvent(Event event) {
		if (event.getEventType() == Event.DB_SYNC) {
			System.out.print("<notifyAll>: " + event);
			for (Iterator<UIStreamHandle> it = Repository.handles.values()
					.iterator(); it.hasNext();) {
				UIStreamHandle handle = it.next();
				if (handle != null && !handle.userId.equals(event.getId()))
					handle.addEvent(event);
			}
		}

	}
}
