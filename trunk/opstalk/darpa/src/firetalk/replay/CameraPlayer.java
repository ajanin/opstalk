package firetalk.replay;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;

import firetalk.model.CheckPoint;
import firetalk.model.People;
import javax.swing.JScrollPane;

import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Dimension;

public class CameraPlayer extends JFrame {

	private JPanel jContentPane = null;
	private File[] images;
	private JLabel jLabel = null;
	private double lat;
	private double lon;
	/**
	 * This is the xxx default constructor
	 */
	public CameraPlayer(CheckPoint s) {
		super();
		this.lat=s.lat;
		this.lon=s.lon;
		initialize();
	}
public void playImages(String folder){
	File file=new File(folder);
	images=file.listFiles();
	Timer timer=new Timer();
	timer.schedule(new ReadImages(), 0,1000);
}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setTitle("Video Camera: ("+lon+", "+lat+")" );
		this.setSize(300, 200);
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel = new JLabel();
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(jLabel, BorderLayout.NORTH);
		}
		return jContentPane;

	}
	public static void main(String[] args) {
		
		//CameraPlayer player=new CameraPlayer();
//		player.show();
//		player.playImages("data/jeffImages");
		
	}
	class ReadImages extends TimerTask{
		private int ind = 0;

		@Override
		public void run() {
			System.out.println("Timer task");
			try {
				BufferedImage img=ImageIO.read(images[ind]);
				jLabel.setIcon(new ImageIcon(img.getSubimage(0, 20, img.getWidth(), img.getHeight()-20)));
				resize(img.getWidth()+20,img.getHeight()+30);
				repaint();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ind++;
			if(ind==images.length)
				ind=0;
			

		}
		
	}
	
}
