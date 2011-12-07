package firetalk.operators.source;

import javax.swing.JPanel;
import java.awt.Frame;
import java.awt.BorderLayout;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.JList;
import javax.swing.JButton;

import firetalk.db.Repository;
import firetalk.db.UIRepository;
import java.awt.GridBagLayout;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import java.awt.Font;
import java.awt.Color;

public class ServerManagerUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JList jList = null;
	private JList jList1 = null;
	private JButton jButton = null;
	private DefaultListModel modelAndroid = new DefaultListModel();
	private DefaultListModel modelUI = new DefaultListModel();
	private Server server = null;
	private UIServer serverUI = null;
	private JPanel jPanel = null;

	/**
	 */
	public ServerManagerUI() {
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		(server = new Server(this)).start();
		(serverUI = new UIServer(this)).start();
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
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJButton(), BorderLayout.NORTH);
			jContentPane.add(getJPanel(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	public void updateAndroidList() {
		modelAndroid.clear();
		for (StreamHandle handle : this.server.handles.values())
			modelAndroid.addElement(handle.getUserId());
		this.jList.repaint();
	}

	public void updateUIList() {
		modelUI.clear();
		for (UIStreamHandle handle : Repository.handles.values())
			modelUI.addElement(handle.userId);
		this.jList.repaint();
	}

	/**
	 * This method initializes jList
	 * 
	 * @return javax.swing.JList
	 */
	private JList getJList() {
		if (jList == null) {
			jList = new JList();
			jList.setModel(modelAndroid);
			this.updateAndroidList();
		}
		return jList;
	}

	/**
	 * This method initializes jList1
	 * 
	 * @return javax.swing.JList
	 */
	private JList getJList1() {
		if (jList1 == null) {
			jList1 = new JList();
			jList1.setModel(modelUI);
			this.updateUIList();
		}
		return jList1;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
		}
		return jButton;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new BoxLayout(getJPanel(), BoxLayout.X_AXIS));
			jPanel.setBorder(BorderFactory.createTitledBorder(null, "status", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanel.add(getJList(), null);
			jPanel.add(getJList1(), null);
		}
		return jPanel;
	}

	public static void main(String[] args) {
		new ServerManagerUI().setVisible(true);
	}
}
