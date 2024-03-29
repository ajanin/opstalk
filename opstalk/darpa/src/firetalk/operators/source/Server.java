package firetalk.operators.source;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import firetalk.UI.MainWindow;
import firetalk.db.Repository;
import firetalk.model.Event;
import firetalk.model.People;
import firetalk.util.Parameter;

public class Server extends Thread {
	final int maxConnection = 100;
	ServerManagerUI parent=null;

//	public MainWindow parent;

	// ContextManager contextManager = new ContextManager();
//	public Server(MainWindow parent) {
//		this.parent = parent;
//	}

//	public void updateCheckPoints() {
//		parent.updateMarkers();
//		parent.updateList();
//	}

	public Server(ServerManagerUI parent) {
		this.parent=parent;
	}

	@Override
	public void run() {
		try {
			InetAddress addr = InetAddress.getByName(Parameter.serverIP);
			// ServerSocketChannel ssChannel1 = ServerSocketChannel.open();
			// ssChannel1.socket().bind(new InetSocketAddress(addr, 8989));

			ExecutorService service = Executors
					.newFixedThreadPool(maxConnection);
			int id = 0;
			// service.execute(contextManager);
			ServerSocket ss = new ServerSocket(8989);
			while (true) {
				// SocketChannel sChannel = ssChannel1.accept();
				Socket s = ss.accept();
				// System.out.println("new connection: " + sChannel);
				StreamHandle handle = new StreamHandle(this, s, id);
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

	public void removeHandle(StreamHandle handle) {
		System.out.println("<Connection Lost>: " + handle.getPeopleId());
		// contextManager.removeStreamHandle(handle);
		// handle.stopAll();
		String id = handle.getUserId();
		if (id != null && Repository.androidHandles.containsValue(handle)) {
			Repository.androidHandles.remove(id);
			parent.updateAndroidList();
		}
	}

	public synchronized void addStreamHandle(StreamHandle handle) {
		int prevN = Repository.androidHandles.size();
		String id = handle.getUserId();
		StreamHandle h = null;
		if (id != null) {
			h = Repository.androidHandles.get(id);
			if (h != null) {
				System.out.println("Thread " + h.getHandleId() + "is to end");
				h.stopThread();
				Repository.androidHandles.remove(id);
			}

			try {
				if (h != null && !h.isAllKilled())
					Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (Repository.androidHandles.get(id) != null)
				System.out.println("<Replace happens>");
			Repository.androidHandles.put(id, handle);
			parent.updateAndroidList();
			System.out.println(handle.getHandleId() + " < Add " + id
					+ " to handles > ");
		}
		if (h != null) {
			if (prevN != Repository.androidHandles.size())
				System.out
						.println("\n*******************\nhandles error: Prev: "
								+ prevN + "after: " + Repository.androidHandles.size()
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
//		if (event.getEventType() == Event.MESSAGE)
//			Repository.events.addFirst(event);
		System.out.print("<notifyAll>: " + event);
		for (StreamHandle handle : Repository.androidHandles.values()) {
			if (handle != null)
				handle.addEvent(event);
		}
		for(UIStreamHandle handle:Repository.uiHandles.values()){
			if(handle!=null)
				handle.addEvent(event);
		}
		//parent.addEvent2UI(event);

	}
}
