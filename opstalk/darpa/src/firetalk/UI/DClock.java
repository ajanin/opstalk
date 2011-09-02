package firetalk.UI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Polygon;
import java.util.Date;

import javax.swing.JPanel;

public class DClock extends JPanel implements Runnable {

	private static final long serialVersionUID = 1L;
	Thread clockThread = null;
	int sleepTime;
	private LEDScreen ledPanel;
	private Polygon b1, b2, b3, b4;
	public boolean inApplet = true;
	Image offScreenBuffer;

	/**
	 * This is the default constructor
	 */
	public DClock() {
		super();
		initialize();
		this.init();
		this.start();
		this.run();
	}

	public void init() {

		LEDScreen.fg = Color.blue;

		LEDScreen.bg = Color.white;

		// LEDScreen.width = 20;
		//
		// LEDScreen.height = 10;
		//
		// LEDScreen.thickness = 5;

		ledPanel = new LEDScreen();

		this.setBackground(LEDScreen.bg);

		sleepTime = 500;
		int w = this.minimumSize().width;
		int h = this.minimumSize().height;
		int thick = 4;

		b1 = new Polygon();
		b1.addPoint(0, 0);
		b1.addPoint(w, 0);
		b1.addPoint(w - thick, thick);
		b1.addPoint(thick, thick);
		b1.addPoint(0, 0);

		b2 = new Polygon();
		b2.addPoint(w, 0);
		b2.addPoint(w, h);
		b2.addPoint(w - thick, h - thick);
		b2.addPoint(w - thick, thick);
		b2.addPoint(w, 0);

		b3 = new Polygon();
		b3.addPoint(0, h);
		b3.addPoint(w, h);
		b3.addPoint(w - thick, h - thick);
		b3.addPoint(thick, h - thick);
		b3.addPoint(0, h);

		b4 = new Polygon();
		b4.addPoint(0, 0);
		b4.addPoint(thick, thick);
		b4.addPoint(thick, h - thick);
		b4.addPoint(0, h);
		b4.addPoint(0, 0);

	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		update(g);
	}

	public void update(Graphics g) {
		Date now = new Date();
		int h = now.getHours();
		int m = now.getMinutes();
		int s = now.getSeconds();

		if (offScreenBuffer == null
				|| (!(offScreenBuffer.getWidth(this) == this.size().width && offScreenBuffer
						.getHeight(this) == this.size().height))) {
			offScreenBuffer = this.createImage(this.size().width,
					this.size().height);
		}

		Graphics gr = offScreenBuffer.getGraphics();

		if ((ledPanel.getSize().width != this.size().width)
				|| (ledPanel.getSize().height != this.size().height)) {
			LEDScreen.setSpecs(this.size().width, this.size().height, ledPanel
					.getSize().width, ledPanel.getSize().height, gr);
			ledPanel.setUp();
			gr.setColor(LEDScreen.bg);
			gr.fillRect(0, 0, this.size().width, this.size().height);
		}

		ledPanel.upDate(h, m, s, gr);

		g.drawImage(offScreenBuffer, 0, 0, this);

		now = null;
		gr = null;

	}

	public void start() {
		if (clockThread == null) {
			clockThread = new Thread(this, "Clock");
			clockThread.start();
		}
	}

	public void run() {
		while (Thread.currentThread() == clockThread) {
			repaint();
			if (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public void stop() {
		clockThread = null;
	}

	public Dimension minimumSize() {
		return LEDScreen.getSize();
	}

	public Dimension maximumSize() {
		return LEDScreen.getSize();
	}

	public Dimension preferredSize() {
		return minimumSize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(100, 30);
		this.setLayout(new GridBagLayout());
		this.setMaximumSize(new Dimension(100, 30));
		this.setPreferredSize(new Dimension(100, 30));
		this.setMinimumSize(new Dimension(100, 30));
	}

}

class Segment {
	public int h;
	public int w;
	public int t, tby2;
	public int x0, y0;
	public Polygon shape;
	static Color fg;
	static Color bg;

	public Segment() {
		shape = new Polygon();

	}

	public static void setColors(Color FG, Color BG) {
		fg = FG;
		bg = BG;
	}

	public Segment(int X0, int Y0, int H, int W, int T, int p) {
		x0 = X0;
		y0 = Y0;
		h = H;
		w = W;
		t = T;
		tby2 = (int) (t * 0.5);
		shape = new Polygon();

		this.setUp(p);

	}

	public void Draw(Graphics g, Color fg) {
		g.setColor(fg);
		g.fillPolygon(shape);
	}

	public boolean setUp(int X0, int Y0, int H, int W, int T, int p) {
		x0 = X0;
		y0 = Y0;
		h = H;
		w = W;
		t = T;
		tby2 = (int) (t * 0.5);
		tby2 = (tby2 == 0) ? 1 : tby2;

		return this.setUp(p);

	}

	public boolean setUp(int p) {

		if (shape != null)
			shape = null;
		shape = new Polygon();

		switch (p) {
		case 0: // Top
			shape.addPoint(x0, y0);
			shape.addPoint(x0 + w, y0);
			shape.addPoint(x0 + w - t, y0 + t);
			shape.addPoint(x0 + t, y0 + t);
			shape.addPoint(x0, y0);
			break;

		case 1: // Right top
		case 2: // Right bottom
			shape.addPoint(x0, y0);
			shape.addPoint(x0, y0 + h);
			shape.addPoint(x0 - t, y0 + h - t);
			shape.addPoint(x0 - t, y0 + t);
			shape.addPoint(x0, y0);
			break;

		case 3: // Bottom
			shape.addPoint(x0, y0);
			shape.addPoint(x0 + t, y0 - t);
			shape.addPoint(x0 + w - t, y0 - t);
			shape.addPoint(x0 + w, y0);
			shape.addPoint(x0, y0);
			break;

		case 4: // Left bottom
		case 5: // Left top
			shape.addPoint(x0, y0);
			shape.addPoint(x0 + t, y0 + t);
			shape.addPoint(x0 + t, y0 + h - t);
			shape.addPoint(x0, y0 + h);
			shape.addPoint(x0, y0);
			break;

		case 6: // Middle
			shape.addPoint(x0, y0);
			shape.addPoint(x0 + tby2, y0 + tby2);
			shape.addPoint(x0 + tby2 + w, y0 + tby2);
			shape.addPoint(x0 + t + w, y0);
			shape.addPoint(x0 + tby2 + w, y0 - tby2);
			shape.addPoint(x0 + tby2, y0 - tby2);
			shape.addPoint(x0, y0);
			break;

		}

		return true;

	}

}

class Column {
	public int t;
	public int h;
	public int x0, y0;
	public int g;
	public Polygon dot1, dot2;
	public static Color fg;

	public void draw(Graphics g) {
		g.setColor(fg);
		g.fillPolygon(dot1);
		g.fillPolygon(dot2);
	}

	public static void setColors(Color FG) {
		fg = FG;
	}

	public boolean setUp(int X0, int Y0, int H, int T) {
		x0 = X0;
		y0 = Y0;
		h = H;
		t = T;

		return this.setUp();
	}

	public boolean setUp() {
		if (dot1 != null)
			dot1 = null;
		dot1 = new Polygon();

		dot1.addPoint(x0, y0);
		dot1.addPoint(x0 + t, y0);
		dot1.addPoint(x0 + t, y0 + t);
		dot1.addPoint(x0, y0 + t);

		y0 += h;

		if (dot2 != null)
			dot2 = null;
		dot2 = new Polygon();

		dot2.addPoint(x0, y0);
		dot2.addPoint(x0 + t, y0);
		dot2.addPoint(x0 + t, y0 + t);
		dot2.addPoint(x0, y0 + t);

		return true;

	}

}

class LEDScreen {
	Digit h1;
	Digit h2;
	Column column1, column2;
	Digit m1;
	Digit m2;
	Digit s1;
	Digit s2;
	static int maxWidth, maxHeight;

	// Default settings.
	static int xpos = 0;
	static int ypos = 0;
	static int height = 6;
	static int width = 7;
	static int pad = (width > height) ? height : width;
	static int gap = 1;
	static int thickness = 2;
	static int fill = (int) (new Integer(pad).floatValue() * 0.5);

	static Color fg = new Color(0, 0, 0);
	static Color bg = new Color(255, 255, 255);

	public LEDScreen() {
		h1 = new Digit();
		h2 = new Digit();
		column1 = new Column();
		column2 = new Column();
		m1 = new Digit();
		m2 = new Digit();
		s1 = new Digit();
		s2 = new Digit();
		maxHeight = 50;
		maxWidth = 200;

		// Setup the panel with this parameters.
		this.setUp();

		Digit.setColors(fg, bg);
		Segment.setColors(fg, bg);
		Column.setColors(fg);
	}

	public boolean setUp() {
		int xloc, yloc;
		int xdiff = width + fill + 2 * gap;

		double scale = 1.0;

		xloc = xpos + pad;
		yloc = ypos + pad;

		h1.setUp(xloc, yloc, width, height, thickness, gap);

		xloc += xdiff;

		h2.setUp(xloc, yloc, width, height, thickness, gap);

		xloc += xdiff;

		int hby2 = (int) (height * 0.5);

		column1.setUp(xloc, yloc + gap + hby2, height, thickness);

		xloc += (int) (xdiff * 0.5);

		m1.setUp(xloc, yloc, width, height, thickness, gap);

		xloc += xdiff;

		m2.setUp(xloc, yloc, width, height, thickness, gap);

		xloc += xdiff;

		column2.setUp(xloc, yloc + gap + hby2, height, thickness);
		xloc += (int) (xdiff * 0.5);

		s1.setUp(xloc, yloc, (int) (width * scale), (int) (height * scale),
				(int) (thickness * scale), (int) (gap * scale));

		xloc += (int) xdiff * scale;

		s2.setUp(xloc, yloc, (int) (width * scale), (int) (height * scale),
				(int) (thickness * scale), (int) (gap * scale));

		maxWidth = xloc + xdiff + pad + 2 * thickness;
		maxHeight = yloc + 2 * height + 3 * gap + pad + 2 * thickness;

		return true;
	}

	// Change settings on resize.
	public static void setSpecs(int w, int h, int maxWidth, int maxHeight,
			Graphics g) {
		boolean reset = false;

		if (maxWidth != w) {
			// Recompute segment width.
			width = (int) (width * ((new Integer(w).doubleValue()) / (new Integer(
					maxWidth).doubleValue())));

			reset = true;
		}
		if (maxHeight != h) {
			// Recompute segment height.
			height = (int) (height * ((new Integer(h).doubleValue()) / (new Integer(
					maxHeight).doubleValue())));
			reset = true;
		}

		if (reset) {
			pad = (width > height) ? height : width;
			fill = (int) (new Integer(pad).doubleValue() * 0.5);

			g.clearRect(xpos, ypos, w, h);
		}
	}

	public static Dimension getSize() {
		return new Dimension(maxWidth, maxHeight);
	}

	public boolean upDate(int hours, int minutes, int seconds, Graphics g) {
		String hourStr = (new Integer(hours).toString());
		String minStr = (new Integer(minutes).toString());
		String secStr = (new Integer(seconds).toString());

		if (hours > 9) {
			h1.draw(hourStr.charAt(0), g);
			h2.draw(hourStr.charAt(1), g);
		} else {
			h1.draw('0', g);
			h2.draw(hourStr.charAt(0), g);
		}

		column1.draw(g);

		if (minutes > 9) {
			m1.draw(minStr.charAt(0), g);
			m2.draw(minStr.charAt(1), g);
		} else {
			m1.draw('0', g);
			m2.draw(minStr.charAt(0), g);
		}

		if (seconds > 9) {
			s1.draw(secStr.charAt(0), g);
			s2.draw(secStr.charAt(1), g);
		} else {
			s1.draw('0', g);
			s2.draw(secStr.charAt(0), g);
		}
		column2.draw(g);

		return true;
	}

}

class Digit {
	private Segment[] segments;
	int x0, y0, h, w, t, gap, value;
	static Color fg;
	static Color bg;

	public Digit() {
		segments = new Segment[7];

		for (int i = 0; i < 7; i++) {
			segments[i] = new Segment();
		}
	}

	public static void setColors(Color FG, Color BG) {
		fg = FG;
		bg = BG;
	}

	public boolean setUp(int X0, int Y0, int W, int H, int T, int g) {
		x0 = X0;
		y0 = Y0;
		h = H;
		w = W;
		t = T;
		gap = g;

		// Top
		segments[0].setUp(x0 + gap, y0, h, w, t, 0);

		// Right top
		segments[1].setUp(x0 + 2 * gap + w, y0 + gap, h, w, t, 1);

		// Right bottom
		segments[2].setUp(x0 + 2 * gap + w, y0 + gap + h + gap, h, w, t, 2);

		// Bottom
		segments[3].setUp(x0 + gap, y0 + 3 * gap + 2 * h, h, w, t, 3);

		// Left bottom
		segments[4].setUp(x0, y0 + gap + h + gap, h, w, t, 4);

		// Left top
		segments[5].setUp(x0, y0 + gap, h, w, t, 5);

		// Middle
		int gby2 = (int) (gap * 0.5);

		segments[6].setUp(x0 + g, y0 + g + h + gby2, h, w - t - gby2, t, 6);

		return true;

	}

	public void draw(char c, Graphics g) {
		g.setColor(fg);

		switch (c) {
		case '0':
			segments[0].Draw(g, fg);
			segments[1].Draw(g, fg);
			segments[2].Draw(g, fg);
			segments[3].Draw(g, fg);
			segments[4].Draw(g, fg);
			segments[5].Draw(g, fg);
			segments[6].Draw(g, bg);
			break;
		case '1':
			segments[0].Draw(g, bg);
			segments[1].Draw(g, fg);
			segments[2].Draw(g, fg);
			segments[3].Draw(g, bg);
			segments[4].Draw(g, bg);
			segments[5].Draw(g, bg);
			segments[6].Draw(g, bg);
			break;
		case '2':
			segments[0].Draw(g, fg);
			segments[1].Draw(g, fg);
			segments[2].Draw(g, bg);
			segments[3].Draw(g, fg);
			segments[4].Draw(g, fg);
			segments[5].Draw(g, bg);
			segments[6].Draw(g, fg);
			break;
		case '3':
			segments[0].Draw(g, fg);
			segments[1].Draw(g, fg);
			segments[2].Draw(g, fg);
			segments[3].Draw(g, fg);
			segments[4].Draw(g, bg);
			segments[5].Draw(g, bg);
			segments[6].Draw(g, fg);
			break;
		case '4':
			segments[0].Draw(g, bg);
			segments[1].Draw(g, fg);
			segments[2].Draw(g, fg);
			segments[3].Draw(g, bg);
			segments[4].Draw(g, bg);
			segments[5].Draw(g, fg);
			segments[6].Draw(g, fg);
			break;
		case '5':
			segments[0].Draw(g, fg);
			segments[1].Draw(g, bg);
			segments[2].Draw(g, fg);
			segments[3].Draw(g, fg);
			segments[4].Draw(g, bg);
			segments[5].Draw(g, fg);
			segments[6].Draw(g, fg);
			break;
		case '6':
			segments[0].Draw(g, fg);
			segments[1].Draw(g, bg);
			segments[2].Draw(g, fg);
			segments[3].Draw(g, fg);
			segments[4].Draw(g, fg);
			segments[5].Draw(g, fg);
			segments[6].Draw(g, fg);
			break;
		case '7':
			segments[0].Draw(g, fg);
			segments[1].Draw(g, fg);
			segments[2].Draw(g, fg);
			segments[3].Draw(g, bg);
			segments[4].Draw(g, bg);
			segments[5].Draw(g, bg);
			segments[6].Draw(g, bg);
			break;
		case '8':
			segments[0].Draw(g, fg);
			segments[1].Draw(g, fg);
			segments[2].Draw(g, fg);
			segments[3].Draw(g, fg);
			segments[4].Draw(g, fg);
			segments[5].Draw(g, fg);
			segments[6].Draw(g, fg);
			break;
		case '9':
			segments[0].Draw(g, fg);
			segments[1].Draw(g, fg);
			segments[2].Draw(g, fg);
			segments[3].Draw(g, fg);
			segments[4].Draw(g, bg);
			segments[5].Draw(g, fg);
			segments[6].Draw(g, fg);
			break;
		}

	}
}
