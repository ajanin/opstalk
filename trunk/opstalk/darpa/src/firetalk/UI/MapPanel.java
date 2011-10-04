package firetalk.UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import Samples.Part1.SampleApp;
import Task.AbstractTask;
import Task.AutoShutdownSignals;
import Task.SimpleTask;
import Task.SimpleTaskHandler;
import Task.TaskException;
import Task.TaskExecutorAdapter;
import Task.TaskExecutorIF;
import Task.Manager.TaskManager;
import Task.ProgressMonitor.ProgressMonitorUtils;
import Task.ProgressMonitor.SwingUIHookAdapter;
import Task.Support.CoreSupport.ByteBuffer;
import Task.Support.CoreSupport.HttpUtils;
import Task.Support.GUISupport.ImageUtils;
import firetalk.db.Repository;
import firetalk.map.MapLookup;
import firetalk.map.MapMarker;
import firetalk.model.CheckPoint;
import firetalk.model.Enemy;
import firetalk.model.Location;
import firetalk.model.ObjPoint;
import firetalk.model.People;
import firetalk.model.IEDPoint;
import firetalk.model.RallyPoint;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import java.awt.Font;
import java.awt.SystemColor;
import com.sun.java.swing.plaf.windows.WindowsButtonUI;
import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;
import com.sun.java.swing.plaf.windows.WindowsProgressBarUI;

/**
 * @author jeffrey MapPanel Class: handle the task of drawing the map to be
 *         displayed, including basemap and overlays
 */
public class MapPanel extends JPanel {
	private BufferedImage _baseMap; // the base layer of google static map
	private BufferedImage _bufMap;
	private SimpleTask<ByteBuffer> _task = null; // @jve:decl-index=0:
	private double _lon; // longitude of center of base map
	private double _lat; // latitude of center of base map
	private int _zoom; // zoom in level (0-19) 0 is entire earth.
	private int _sizeX; // image size in X
	private int _sizeY;// image size in Y
	private final double _offset = 268435456; // half of height of map in zoom
	// level 21, in pixel
	private final double _radius = _offset / Math.PI; // radius in pixel in zoom
	private CheckPoint selectedSensor = null; // @jve:decl-index=0:
	private IEDPoint selectedMes = null;
	private String mapType = "roadMap"; // @jve:decl-index=0:
	// private Image soldier = null; // @jve:decl-index=0:
	// private Image teamLead = null; // @jve:decl-index=0:
	// private Image squadLead = null; // @jve:decl-index=0:
	private Image enemyImg = null;
	private Image friendImg = null;
	private Image warningImg = null; // @jve:decl-index=0:
	private Image rallyImg = null;
	private Image objImg = null;
	private Image objImg2 = null;
	private Image wayImg = null;
	private Image wayImg2 = null;
	public MainWindow parent = null;
	private int pRadius = 8;
	private boolean isExecuting = false;
	private boolean hasExeRequest = false;

	synchronized public void scheduleExecute() {
		System.out.println(_task.getState());
		if (_task != null && !isExecuting) {
			try {
				isExecuting = true;
				_task.execute();
				hasExeRequest = false;

			} catch (TaskException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			hasExeRequest = true;
		}

	}

	private void executeTaskIfAny() {
		isExecuting = false;
		if (hasExeRequest)
			scheduleExecute();
	}

	public void initializeImages() throws IOException {
		// soldier = ImageIO.read(new File("img/friend.jpg")).getScaledInstance(
		// pRadius * 2, pRadius * 2, Image.SCALE_DEFAULT);
		// teamLead = ImageIO.read(new
		// File("img/friend.jpg")).getScaledInstance(
		// pRadius * 2, pRadius * 2, Image.SCALE_DEFAULT);
		// squadLead = ImageIO.read(new
		// File("img/friend.jpg")).getScaledInstance(
		// pRadius * 2, pRadius * 2, Image.SCALE_DEFAULT);
		friendImg = ImageIO.read(new File("img/friend.jpg")).getScaledInstance(
				pRadius * 2, pRadius, Image.SCALE_DEFAULT);

		enemyImg = ImageIO.read(new File("img/enemy.jpg")).getScaledInstance(
				pRadius * 2, pRadius * 2, Image.SCALE_DEFAULT);
		this.warningImg = ImageIO.read(new File("img/warning.jpg"))
				.getScaledInstance(pRadius * 2, pRadius * 2,
						Image.SCALE_DEFAULT);
		this.rallyImg = ImageIO.read(new File("img/rallyPoint.jpg"))
				.getScaledInstance(pRadius * 2, pRadius * 2,
						Image.SCALE_DEFAULT);
		this.wayImg = ImageIO.read(new File("img/waypoint.jpg"))
				.getScaledInstance(pRadius * 2, pRadius * 2,
						Image.SCALE_DEFAULT);
		this.wayImg2 = ImageIO.read(new File("img/waypoint2.jpg"))
				.getScaledInstance(pRadius * 2, pRadius * 2,
						Image.SCALE_DEFAULT);
		this.objImg = ImageIO.read(new File("img/objpoint.jpg"))
				.getScaledInstance(pRadius * 2, pRadius * 2,
						Image.SCALE_DEFAULT);
		this.objImg2 = ImageIO.read(new File("img/objpoint2.jpg"))
				.getScaledInstance(pRadius * 2, pRadius * 2,
						Image.SCALE_DEFAULT);

	}

	/**
	 * 
	 */
	private void drawLocations() {
		if (_baseMap == null) {
			System.out.println("base map is not avabile");
			return;
		}
		Graphics g2d = _baseMap.getGraphics();
		g2d.drawImage(_bufMap, 0, 0, null); // draw base map
		// draw way points
		// for (CheckPoint cp : Repository.checkPoints) {
		// int x = (int) this.longtoX(cp.lon);
		// int y = (int) this.lattoY(cp.lat);
		// g2d.setColor(cp.isReached() ? Color.GREEN : Color.yellow);
		// int height = 12;
		// int width = 19;
		// int[] xPoints = new int[] { x, x - 4, x - width, x - width,
		// x + width, x + width, x };
		// int[] yPoints = new int[] { y, (int) (y - height * 0.6),
		// (int) (y - height * 0.6), y - height * 2, y - height * 2,
		// (int) (y - height * 0.6), (int) (y - height * 0.6) };
		// g2d.fillPolygon(xPoints, yPoints, xPoints.length);
		// g2d.setColor(Color.BLACK);
		// g2d.drawPolygon(xPoints, yPoints, xPoints.length);
		// if (cp.isObj())
		// g2d.drawString("O" + cp.userID, x - width + 2,
		// (int) (y - height));
		// else
		// g2d.drawString("w" + cp.id, x - width + 2, (int) (y - height));
		//
		// }
		g2d.setColor(Color.blue);
		g2d.setFont(new Font("times", Font.BOLD, 9));
		for (CheckPoint cp : Repository.checkPoints) {
			int x = (int) this.longtoX(cp.lon);
			int y = (int) this.lattoY(cp.lat);
			if (cp.isObj()) {
				if (cp.isReached())
					g2d.drawImage(objImg2, x - pRadius, y - pRadius, null);
				else
					g2d.drawImage(objImg, x - pRadius, y - pRadius, null);
			} else {
				if (cp.isReached())
					g2d.drawImage(wayImg2, x - pRadius, y - pRadius, null);
				else
					g2d.drawImage(wayImg, x - pRadius, y - pRadius, null);
			}
			if (cp.isObj())
				g2d.drawString("O" + cp.userID, x - pRadius + 2,
						(int) (y - pRadius));
			else
				g2d.drawString("w" + cp.id, x - pRadius + 2,
						(int) (y - pRadius));

		}
		// draw rally points
		for (RallyPoint p : Repository.rallyList) {
			int x = (int) this.longtoX(p.lon);
			int y = (int) this.lattoY(p.lat);
			g2d.drawImage(rallyImg, x - 10, y - 10, 20, 20, null);
		}
		// draw IED POINTS
		for (IEDPoint p : Repository.IEDList) {
			int x = (int) this.longtoX(p.getLongitude());
			int y = (int) this.lattoY(p.getLatitude());
			g2d.drawImage(warningImg, x - 10, y - 10, 20, 20, null);
		}
		// draw selected sensor
		CheckPoint cp = this.getSelectedSensor();
		if (cp != null) {
			int x = (int) this.longtoX(cp.lon);
			int y = (int) this.lattoY(cp.lat);
			g2d.setColor(Color.red);
			g2d.drawRect(x - 8, y - 8, 16, 16);
		}

		// draw selected sensor
		IEDPoint mes = this.getSelectedMes();
		if (mes != null) {
			int x = (int) this.longtoX(mes.getLongitude());
			int y = (int) this.lattoY(mes.getLatitude());
			g2d.setColor(Color.red);
			g2d.drawRect(x - 5, y - 5, 10, 10);
		}
		for (Enemy e : Repository.enemyList) {
			int x = (int) this.longtoX(e.getLongitude());
			int y = (int) this.lattoY(e.getLatitude());
			g2d.drawImage(enemyImg, x - pRadius, y - pRadius, null);
		}
		for (Iterator<String> it = Repository.peopleList.keySet().iterator(); it
				.hasNext();) {
			String user = it.next();
			People p = Repository.peopleList.get(user);
			Location loc = p.getLocation();
			if (loc != null) {
				int x = (int) this.longtoX(loc.lon);
				int y = (int) this.lattoY(loc.lat);
				g2d.setColor(Color.red);
				if (p.isSelected())
					g2d.drawOval(x - pRadius * 2, y - pRadius * 2, pRadius * 4,
							pRadius * 4);
				g2d.setColor(Color.black);
				g2d.setFont(new Font("times", Font.BOLD, 9));
				g2d.drawImage(friendImg, x - pRadius, y - pRadius, null);
				g2d.drawString(p.getRandName(), x - pRadius, y - pRadius);
				//
				// if (p.getLevel().equalsIgnoreCase("1")) {
				// g2d.drawImage(squadLead, x - pRadius, y - pRadius, null);
				// g2d.drawString("PL " + p.getId(), x - pRadius, y - pRadius);
				// }
				//
				// else if (p.getLevel().equalsIgnoreCase("2")) {
				// g2d.drawImage(teamLead, x - pRadius, y - pRadius, null);
				// g2d.drawString("SQ " + p.getId(), x - pRadius, y - pRadius);
				// }
				//
				// else if (p.getLevel().equalsIgnoreCase("3")) {
				// g2d.drawImage(soldier, x - pRadius, y - pRadius, null);
				// g2d.drawString("FT " + p.getId(), x - pRadius, y - pRadius);
				// }

				// --- calculate points for drawing arrow
				g2d.setColor(Color.black);
				float arrowLength = 17;
				int topx = (int) (x + arrowLength * Math.cos(loc.direction));
				int topy = (int) (y - arrowLength * Math.sin(loc.direction));
				float arrowAngle1 = (float) (loc.direction + Math.PI * 3 / 4);
				float arrowAngle2 = (float) (loc.direction - Math.PI * 3 / 4);
				g2d.drawLine(x, y, topx, topy);
				g2d.drawLine(topx, topy,
						(int) (topx + arrowLength / 2 * Math.cos(arrowAngle1)),
						(int) (topy - arrowLength / 2 * Math.sin(arrowAngle1)));
				g2d.drawLine(topx, topy,
						(int) (topx + arrowLength / 2 * Math.cos(arrowAngle2)),
						(int) (topy - arrowLength / 2 * Math.sin(arrowAngle2)));

				// Trajecotry calculations
				final int smoothSize = 10;
				final int tolerance = 3; // times
				double aveDiffx = Double.MAX_VALUE;
				double aveDiffy = Double.MAX_VALUE;
				LinkedList<Location> locates = p.getHistory();
				ListIterator<Location> lt = locates.listIterator();
				LinkedList<Double> diffLat = new LinkedList<Double>();
				LinkedList<Double> diffLon = new LinkedList<Double>();

				if (locates.size() > 1) {
					g2d.setColor(Color.red);
					Location first = lt.next();
					for (; lt.hasNext();) {
						Location second = lt.next();
						double diffy = second.lat - first.lat;
						double diffx = second.lon - first.lon;
						System.out.print("<" + diffx + "," + diffy + ">");
						if (diffLat.size() < smoothSize
								|| (diffy != 0 || diffx != 0)
								&& diffy < aveDiffy * tolerance
								&& diffx < aveDiffx * tolerance) {
							diffLat.addLast(diffy);
							diffLon.addLast(diffx);
							if (diffLat.size() > smoothSize) {
								diffLat.removeFirst();
								diffLon.removeFirst();
							}
							if (diffLat.size() >= smoothSize) {
								// cal average diff
								aveDiffx = 0;
								aveDiffy = 0;
								for (Double v : diffLat)
									aveDiffy += v;
								for (Double v : diffLon)
									aveDiffx += v;
								aveDiffx /= diffLon.size();
								aveDiffy /= diffLat.size();
							}
							int x1 = (int) this.longtoX(first.lon);
							int y1 = (int) this.lattoY(first.lat);
							int x2 = (int) this.longtoX(second.lon);
							int y2 = (int) this.lattoY(second.lat);
							g2d.drawLine(x1, y1, x2, y2);
							first = second;
							System.out.println(" ");
						} else {
							System.out.println(" x");
						}

					}
				}
			}
		}

		this.mapContainer.repaint();
	}

	/**
	 * draw extra information on the img,(e.g. spatial message, people location)
	 */
	public void drawExtraInfo() {
		this.drawLocations();
	}

	public CheckPoint getSelectedSensor() {
		return selectedSensor;
	}

	public void setSelectedSensor(CheckPoint selectedSensor) {
		this.selectedSensor = selectedSensor;
	}

	public IEDPoint getSelectedMes() {
		return selectedMes;
	}

	public void setSelectedMes(IEDPoint selectedMes) {
		this.selectedMes = selectedMes;
	}

	private void sout(String msg) {
		System.out.println(msg);
	}

	public int getWidth() {
		return _sizeX;
	}

	public int getHeight() {
		return _sizeY;
	}

	/**
	 * move image pixels by (diffx, diffy)
	 */
	public void moveImage(int diffx, int diffy) {
		if (_baseMap == null) {
			System.out.println("base map is not avabile");
			return;
		}
		BufferedImage img = _baseMap.getSubimage(-Math.min(diffx, 0),
				-Math.min(diffy, 0), _sizeX - Math.abs(diffx),
				_sizeY - Math.abs(diffy));
		Graphics g = _baseMap.getGraphics();
		g.setColor(Color.white);
		g.drawRect(0, 0, _sizeX, _sizeY);
		g.drawImage(img, Math.max(0, diffx), Math.max(0, diffy), null);
		mapContainer.repaint();
	}

	public void setMapInfo(int zoom) {

	}

	/**
	 * @param lat
	 *            : latitude
	 * @return y position in the base map
	 */
	public double lattoY(double lat) {
		double y = this.lattoGlobalY(lat);
		double yc = this.lattoGlobalY(this._lat);
		return this._sizeY / 2.0 + (y - yc) * Math.pow(2.0, this._zoom - 21);
	}

	/**
	 * @param y
	 *            : y position in the base map
	 * @return latitude
	 */
	public double ytoLat(double y) {
		double yc = this.lattoGlobalY(this._lat);
		double ystar = yc + (y - this._sizeY / 2.0) * Math.pow(2.0, 21 - _zoom);
		return this.globalYtoLat(ystar);
	}

	/**
	 * @param y
	 *            : y position in the map of level 21, (latitude 0) corresponds
	 *            to (_offset)
	 * @return latitude
	 */
	private double globalYtoLat(double y) {
		double temp = Math.pow(Math.E, (this._offset - y) / this._radius * 2);
		return Math.asin((temp - 1) / (temp + 1)) * 180 / Math.PI;

	}

	/**
	 * @param lat
	 *            : latitude
	 * @return y position in the map of level 21, (latitude 0) corresponds to
	 *         (_offset)
	 */
	private double lattoGlobalY(double lat) {
		return this._offset
				- this._radius
				* Math.log((1 + Math.sin(lat * Math.PI / 180))
						/ (1 - Math.sin(lat * Math.PI / 180))) / 2;
	}

	/**
	 * @param lon
	 *            : longtitude
	 * @return x position in the base map
	 */
	public double longtoX(double lon) {
		double lonPerX = 360.0 / 256 / Math.pow(2, _zoom);
		return (lon - _lon) / lonPerX + _sizeX / 2.0;
	}

	/**
	 * @param x
	 *            : x position in the base map
	 * @return lontitude
	 */
	public double xtoLong(double x) {
		double lonPerX = 360.0 / 256 / Math.pow(2, _zoom);
		return _lon + (x - _sizeX / 2.0) * lonPerX;
	}

	@SuppressWarnings("unchecked")
	private void _setupTask() {
		TaskExecutorIF<ByteBuffer> functor = new TaskExecutorAdapter<ByteBuffer>() {
			public ByteBuffer doInBackground(Future<ByteBuffer> swingWorker,
					SwingUIHookAdapter hook) throws Exception {
				_initHook(hook);
				// set the license key
				MapLookup.setLicenseKey("");
				// get the uri for the static map
				Vector<MapMarker> markers = new Vector<MapMarker>();
				// for (CheckPoint s : Repository.checkPoints) {
				// MapMarker marker = new MapMarker(s.lat, s.lon, s
				// .isReached() ? MapMarker.MarkerColor.green
				// : MapMarker.MarkerColor.yellow, 'p');
				// markers.add(marker);
				// }
				// for (ObjPoint s : Repository.objPoints.values()) {
				// MapMarker marker = new MapMarker(s.lat, s.lon, s
				// .isReached() ? MapMarker.MarkerColor.green
				// : MapMarker.MarkerColor.yellow, 'o');
				// markers.add(marker);
				// }
				String uri = MapLookup.getMap(_lat, _lon, _sizeX, _sizeY,
						_zoom, markers, mapType);
				sout("Google Maps URI=" + uri);
				// get the map from Google
				GetMethod get = new GetMethod(uri);
				new HttpClient().executeMethod(get);
				ByteBuffer data = HttpUtils.getMonitoredResponse(hook, get);
				try {
					_baseMap = ImageUtils.toCompatibleImage(ImageIO.read(data
							.getInputStream()));
					_bufMap = ImageUtils.toCompatibleImage(ImageIO.read(data
							.getInputStream()));
					sout("converted downloaded data to image...");
				} catch (Exception e) {
					_baseMap = null;
					sout("The URI is not an image. Data is downloaded, can't display it as an image.");
				}
				return data;
			}

			@Override
			public String getName() {
				return _task.getName();
			}
		};

		_task = new SimpleTask(new TaskManager(), functor, "HTTP GET Task",
				"Download an image from a URL", AutoShutdownSignals.Daemon);

		_task.setTaskHandler(new SimpleTaskHandler<ByteBuffer>() {
			@Override
			public void beforeStart(AbstractTask task) {
				// sout(":: taskHandler - beforeStart");
			}

			@Override
			public void started(AbstractTask task) {
				// sout(":: taskHandler - started ");
			}

			/**
			 * {@link SampleApp#_initHook} adds the task status listener, which
			 * is removed here
			 */
			@Override
			public void stopped(long time, AbstractTask task) {
				task.getUIHook().clearAllStatusListeners();
				executeTaskIfAny();
			}

			@Override
			public void interrupted(Throwable e, AbstractTask task) {
				executeTaskIfAny();
			}

			@Override
			public void ok(ByteBuffer value, long time, AbstractTask task) {
				// sout(":: taskHandler [" + task.getName() + "]- ok - size="
				// + (value == null ? "null" : value.toString()));
				if (_baseMap != null) {
					setToolTipText(MessageFormat
							.format("<html>Image downloaded from URI<br>size: w={0}, h={1}</html>",
									_baseMap.getWidth(), _baseMap.getHeight()));
					MapPanel.this.drawExtraInfo();
					updateMap();

				} else
					System.out.println("base map is not avabile");// _displayRespStrInFrame();
				executeTaskIfAny();
			}

			@Override
			public void error(Throwable e, long time, AbstractTask task) {
				executeTaskIfAny();
			}

			@Override
			public void cancelled(long time, AbstractTask task) {
				executeTaskIfAny();
			}
		});
	}

	public void zoomIn() {
		if (_zoom < 21) {
			_zoom++;
			jSlider.setValue(_zoom);
			this.scheduleExecute();
		}
	}

	public void zoomOut() {
		if (_zoom > 0) {
			_zoom--;
			jSlider.setValue(_zoom);
			this.scheduleExecute();
		}
	}

	public void resetMap(double lon, double lat) {
		this._lon = lon;
		this._lat = lat;
		reloadMap();
	}

	public void reloadMap() {
		this.scheduleExecute();
	}

	private void updateMap() {
		mapContainer.setImage(_baseMap);
	}

	private SwingUIHookAdapter _initHook(SwingUIHookAdapter hook) {
		hook.enableRecieveStatusNotification(true);
		// hook.enableSendStatusNotification(checkboxSendStatus.isSelected());

		// hook.setProgressMessage(ttfProgressMsg.getText());

		PropertyChangeListener listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				SwingUIHookAdapter.PropertyList type = ProgressMonitorUtils
						.parseTypeFrom(evt);
				int progress = ProgressMonitorUtils.parsePercentFrom(evt);
				String msg = ProgressMonitorUtils.parseMessageFrom(evt);

				jProgressBar.setValue(progress);
				jProgressBar.setString(type.toString());

				sout(msg);
			}
		};

		hook.addRecieveStatusListener(listener);
		hook.addSendStatusListener(listener);
		hook.addUnderlyingIOStreamInterruptedOrClosed(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				// sout(evt.getPropertyName() + " fired!!!");
			}
		});

		return hook;
	}

	public void initMapInfo(double lat, double lon, int sizeX, int sizeY,
			int zoom) {
		this._lon = lon;
		this._lat = lat;
		this._zoom = zoom;
		this._sizeX = sizeX;
		this._sizeY = sizeY;
		this.jSlider.setValue(_zoom);
		this.scheduleExecute();
	}

	private static final long serialVersionUID = 1L;
	private JPanel jPanel = null;
	private JButton jButton = null;
	private JButton jButton1 = null;
	private JProgressBar jProgressBar = null;
	private JScrollPane jScrollPane = null;
	private MapContainer mapContainer = null;
	private JSlider jSlider = null;
	private JComboBox jComboBox = null;
	private JPanel jPanel1 = null;

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(java.awt.FlowLayout.LEFT);
			jPanel = new JPanel();
			jPanel.setLayout(flowLayout);
			jPanel.add(getJButton(), null);
			jPanel.add(getJSlider(), null);
			jPanel.add(getJButton1(), null);
			jPanel.add(getJComboBox(), null);
			jPanel.add(getJProgressBar(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("");
			jButton.setUI(new WindowsButtonUI());
			jButton.setPreferredSize(new Dimension(15, 15));
			ImageIcon icon = new ImageIcon("data/icon/minus.png");
			jButton.setIcon(icon);
			Dimension maximumSize = new Dimension(icon.getIconWidth(),
					icon.getIconHeight());
			jButton.setMaximumSize(maximumSize);
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					zoomOut();
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jButton1
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setPreferredSize(new Dimension(15, 15));
			jButton1.setUI(new WindowsButtonUI());
			ImageIcon icon = new ImageIcon("data/icon/plus.png");
			jButton1.setIcon(icon);
			Dimension maximumSize = new Dimension(icon.getIconWidth(),
					icon.getIconHeight());
			jButton1.setMaximumSize(maximumSize);
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					zoomIn();
				}
			});
		}
		return jButton1;
	}

	/**
	 * This method initializes jProgressBar
	 * 
	 * @return javax.swing.JProgressBar
	 */
	private JProgressBar getJProgressBar() {
		if (jProgressBar == null) {
			jProgressBar = new JProgressBar();
			jProgressBar.setStringPainted(true);
			jProgressBar.setString("progress %");
			jProgressBar.setUI(new WindowsProgressBarUI());
			jProgressBar.setToolTipText("% progress is displayed here");
		}
		return jProgressBar;
	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			mapContainer = new MapContainer();
			mapContainer.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			mapContainer.setParent(this);
			jScrollPane = new JScrollPane();
			jScrollPane
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			jScrollPane
					.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPane.setViewportView(mapContainer);
		}
		return jScrollPane;
	}

	public void reCenter(double lon, double lat) {
		this._lon = lon;
		this._lat = lat;
		zoomIn();
	}

	/**
	 * This method initializes jSlider
	 * 
	 * @return javax.swing.JSlider
	 */
	private JSlider getJSlider() {
		if (jSlider == null) {
			jSlider = new JSlider();
			jSlider.setMinimum(0);
			jSlider.setPreferredSize(new Dimension(150, 16));
			jSlider.setMaximum(21);
			jSlider.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					_zoom = jSlider.getValue();
					scheduleExecute();
				}
			});
		}
		return jSlider;
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBox() {
		if (jComboBox == null) {
			jComboBox = new JComboBox();
			jComboBox.setUI(new WindowsComboBoxUI());
			jComboBox.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					mapType = e.getItem().toString();
					scheduleExecute();
				}
			});
			jComboBox.addItem("roadMap");
			jComboBox.addItem("satellite");
			jComboBox.addItem("hybird");

		}
		return jComboBox;
	}

	/**
	 * This is the default constructor
	 */
	public MapPanel() {
		super();
		this._setupTask();
		initialize();
		try {
			initializeImages();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.initMapInfo(38.882690483333334, -77.09945643333333, 512, 512, 16);

		// this.initMapInfo(37.87013435694719, -122.2714068423748, 512, 512,
		// 15);
	}

	public void setParent(MainWindow parent) {
		this.parent = parent;
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setBorder(BorderFactory.createTitledBorder(null, "Map",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION,
				new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
		BorderLayout borderLayout = new BorderLayout();
		borderLayout.setHgap(1);
		borderLayout.setVgap(1);
		this.setLayout(borderLayout);
		this.add(getJPanel(), BorderLayout.NORTH);
		this.add(getJPanel1(), BorderLayout.CENTER);
	}

	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.weightx = 1.0;
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.setBorder(BorderFactory
					.createBevelBorder(BevelBorder.LOWERED));
			jPanel1.add(getJScrollPane(), gridBagConstraints);
		}
		return jPanel1;
	}

} // @jve:decl-index=0:visual-constraint="10,10"
