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

	public UIServer(ServerManagerUI parent) {
		this.parent = parent;
	}

	@Override
	public void run() {
		try {
			InetAddress addr = InetAddress.getByName("169.234.133.205");

			ExecutorService service = Executors
					.newFixedThreadPool(maxConnection);
			int id = 0;
			// service.execute(contextManager);
			ServerSocket ss = new ServerSocket(9001);
			while (true) {
				Socket s = ss.accept();
				UIStreamHandle handle = new UIStreamHandle(this, s);
				id++;
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
		if (id != null && Repository.uiHandles.containsValue(handle)) {
			Repository.uiHandles.remove(id);
			if (handle.isMainDisplay()) {
				if (Repository.uiHandles.size() > 0) {
					UIStreamHandle h = Repository.uiHandles.values().iterator()
							.next();
					h.setMainDisplay(true);
				}
			}
			parent.updateUIList();
		}
	}

	public synchronized void addStreamHandle(UIStreamHandle handle) {
		int prevN = Repository.uiHandles.size();
		String id = handle.userId;
		UIStreamHandle h = null;
		if (id != null) {
			h = Repository.uiHandles.get(id);
			if (h != null) {
				System.out.println("Thread " + h.userId + "is to end");
				h.stopThread();
				Repository.uiHandles.remove(id);
			}

			try {
				if (h != null && !h.isAllKilled())
					Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (Repository.uiHandles.get(id) != null)
				System.out.println("<Replace happens>");
			Repository.uiHandles.put(id, handle);
			parent.updateUIList();
			handle.setMainDisplay(Repository.uiHandles.size() == 1);
			System.out.println(handle.userId+ " < Add " + id
					+ " to Repository.handles > ");
		}
		if (h != null) {
			if (prevN != Repository.uiHandles.size())
				System.out
						.println("\n*******************\nRepository.handles error: Prev: "
								+ prevN
								+ "after: "
								+ Repository.uiHandles.size()
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
			for (Iterator<UIStreamHandle> it = Repository.uiHandles.values()
					.iterator(); it.hasNext();) {
				UIStreamHandle handle = it.next();
				if (handle != null && !handle.userId.equals(event.getId()))
					handle.addEvent(event);
			}
		}

	}
}
