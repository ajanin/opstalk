package firetalk.UI;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Font;
import javax.swing.BoxLayout;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.Color;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.SwingConstants;

public class OpsClock extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel jLabel = null;
	private JPanel jPanel = null;
	private JLabel jLabel1 = null;
	private JPanel jPanel1 = null;
	private JLabel jLabel2 = null;
	private JLabel jLabel3 = null;
	private long startTime;

	/**
	 * This is the default constructor
	 */
	public OpsClock() {
		super();
		initialize();
		startTime=System.currentTimeMillis(); // time when the app start
		Timer timer = new Timer();
		timer.schedule(new RemindTask(), 0,1000);
	}

	class RemindTask extends TimerTask {
		public void run() {
			Date date = new Date();
			
			String str = String.format("%02d:%02d:%02d", date
					.getHours(), date.getMinutes(),date.getSeconds());
			jLabel.setText(str);
			long diff=System.currentTimeMillis()-startTime;
			int hour=(int) (diff/3600000);
			int minute= (int) (diff%3600000/60000);
			int second=(int) (diff%3600000%60000/1000);
			str=String.format("%02d:%02d:%02d", hour,minute,second);
			jLabel3.setText(str);
			
		}
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		jLabel = new JLabel();
		jLabel.setText("08:23:12");
		jLabel.setForeground(Color.blue);
		jLabel.setFont(new Font("Dialog", Font.BOLD, 18));
		this.setSize(300, 200);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(getJPanel(), null);
		this.add(getJPanel1(), null);
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jLabel1 = new JLabel();
			jLabel1.setText("Current Time ");
			jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
			jPanel = new JPanel();
			jPanel.setLayout(new FlowLayout());
			jPanel.add(jLabel1, null);
			jPanel.add(jLabel, null);
		}
		return jPanel;
	}

	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jLabel3 = new JLabel();
			jLabel3.setText("08:23:12");
			jLabel3.setForeground(Color.blue);
			jLabel3.setFont(new Font("Dialog", Font.BOLD, 18));
			jLabel2 = new JLabel();
			jLabel2.setText("Elapsed Time  ");
			jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.add(jLabel2, new GridBagConstraints());
			jPanel1.add(jLabel3, new GridBagConstraints());
		}
		return jPanel1;
	}

}
