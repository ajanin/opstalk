package firetalk.UI;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import firetalk.db.UIRepository;
import firetalk.model.CheckPoint;
import firetalk.model.IEDPoint;

/**
 * @author jeffrey MapContainer Class: deal with interactivity between user and
 *         map
 */
public class MapContainer extends JLabel {
	private MapPanel parent; // Mappanel that contains this map
	private int curr_x;
	private int curr_y;
	private int press_x;
	private int press_y;

	public void setParent(MapPanel panel) {
		this.parent = panel;
	}

	/**
	 * @param (x, y): position
	 * @param messages
	 *            : sensors to select from
	 * @return nearest sensor to (x,y)
	 */
	private CheckPoint sensorSelected(int x, int y, LinkedList<CheckPoint> sensors) {
		double minD = 200;
		int ind = -1;
		CheckPoint minP = null;
		for (CheckPoint point : sensors) {
			int sx = (int) parent.longtoX(point.lon);
			int sy = (int) parent.lattoY(point.lat);
			double dist = (x - sx) * (x - sx) + (y - sy) * (y - sy);
			if (dist < minD) {
				minD = dist;
				minP = point;
			}
		}
		return minP;
	}

	/**
	 * @param (x, y): positioin
	 * @param messages
	 *            : spatial messages to select from
	 * @return nearest spatial message to (x,y)
	 */
	private IEDPoint mesSelected(int x, int y,
			LinkedList<IEDPoint> messages) {
		double minD = 200;
		int ind = -1;
		for (int i = 0; i < messages.size(); i++) {
			int sx = (int) parent.longtoX(messages.get(i).getLongitude());
			int sy = (int) parent.lattoY(messages.get(i).getLatitude());
			double dist = (x - sx) * (x - sx) + (y - sy) * (y - sy);
			if (dist < minD) {
				minD = dist;
				ind = i;
			}
		}
		return minD < 50 ? messages.get(ind) : null;
	}

	public MapContainer() {
		this.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {

				// if (e.isAltDown()) {
				// TODO Auto-generated method stub
				if (e.getUnitsToScroll() < 0)
					parent.zoomIn();
				if (e.getUnitsToScroll() > 0)
					parent.zoomOut();
				// }

			}
		});
		this.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				int x = e.getX();
				int y = e.getY();
				int diffx = x - curr_x;
				int diffy = y - curr_y;
				curr_x = x;
				curr_y = y;
				parent.moveImage(diffx, diffy);

			}

			@Override
			public void mouseMoved(MouseEvent e) {

				int x = e.getPoint().x;
				int y = e.getPoint().y;
				double rlon = parent.xtoLong(x);
				double rlat = parent.ytoLat(y);
				setToolTipText("(longitude, latitude): (" + rlon + ", " + rlat
						+ ")");
				CheckPoint sensor = sensorSelected(x, y, UIRepository.checkPoints);
				IEDPoint mes = mesSelected(x, y, UIRepository.IEDList);
				parent.setSelectedSensor(sensor);
				parent.setSelectedMes(mes);
				if (sensor != null && mes != null) {
					parent.setSelectedMes(null); // sensor has high priority
				}
				parent.drawExtraInfo();
			}
		});

		this.addMouseListener(new MouseListener() {

			public void mousePressed(MouseEvent e) {

				press_x = curr_x = e.getPoint().x;
				press_y = curr_y = e.getPoint().y;
				// double tlat=38.921099,tlon=-77.3689;
				// int x=(int) Math.round(longtoX(tlon));
				// int y=(int) Math.round(lattoY(tlat));
				double rlon = parent.xtoLong(curr_x);
				double rlat = parent.ytoLat(curr_y);
				// Graphics2D gd = _baseMap.createGraphics();
				// gd.fillRect(x-2, y-2, 5,5);
				System.out.println("(longitude, latitude): (" + rlon + ", "
						+ rlat + ")");
				// File file = new File("images.jpg");
				// Image image = null ;
				// try {
				// image = ImageIO.read(file);
				// } catch (IOException e1) {
				// // TODO Auto-generated catch block
				// e1.printStackTrace();
				// }
				// //ImageObserver ob=new ImageObserver();
				// gd.drawImage(image, 10, 10, 100, 100, null);
				repaint();
			}

			public void mouseReleased(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				if (x != press_x || y != press_y) {
					double rlon = parent.xtoLong(parent.getWidth() / 2 - x
							+ press_x);
					double rlat = parent.ytoLat(parent.getHeight() / 2 - y
							+ press_y);
					parent.resetMap(rlon, rlat);
				}
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1) {
					CheckPoint s = parent.getSelectedSensor();
					if (s != null)
						for (int i = 0; i < UIRepository.checkPoints.size(); i++) {
							if (UIRepository.checkPoints.get(i).equals(s)) {
								parent.parent.selectListItem(i);
							}
						}
					IEDPoint m = parent.getSelectedMes();
					if (m != null) {
						parent.parent.setInfoPanel(m.toString());
					}
				}
				if (e.getClickCount() == 2) {

					parent.parent.addNewCheckPointFields(parent
							.ytoLat(e.getY()), parent.xtoLong(e.getX()));
					CheckPoint s = parent.getSelectedSensor();
					if (s != null) {
						int i = 0;
						for (CheckPoint cp : UIRepository.checkPoints) {
							if (cp.equals(s)) {
								parent.parent.selectListItem(i);
							}
							i++;
						}
					}
				}
			}
		});
	}

	public void setImage(BufferedImage img) {
		this.setIcon(new ImageIcon(img));
		repaint();
	}
}
