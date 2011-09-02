package firetalk.replay;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JInternalFrame;
import javax.swing.JTextField;

import org.jdesktop.swingx.VerticalLayout;

import firetalk.model.IEDPoint;

import javax.swing.JLabel;

import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import java.awt.Dimension;

public class MessagePlayer extends JFrame {

	private JPanel jPanel = null;
	private JTextPane jTextPane = null;
	private JScrollPane jScrollPane = null;

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.BOTH;
			gridBagConstraints1.weighty = 1.0;
			gridBagConstraints1.weightx = 1.0;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(getJScrollPane(), gridBagConstraints1);
		}
		return jPanel;
	}

	/**
	 * This method initializes jTextPane	
	 * 	
	 * @return javax.swing.JTextPane	
	 */
	private JTextPane getJTextPane() {
		if (jTextPane == null) {
			jTextPane = new JTextPane();
		}
		return jTextPane;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTextPane());
		}
		return jScrollPane;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

//	/**
//	 * This is the xxx default constructor
//	 */
	public MessagePlayer(IEDPoint mes) {
		super();
		initialize();
		this.setTitle("Spatial Message: ("+mes.getLongitude()+","+mes.getLatitude()+")");
		this.jTextPane.setText(mes.getMes());
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(479, 112);
		this.setContentPane(getJPanel());
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
