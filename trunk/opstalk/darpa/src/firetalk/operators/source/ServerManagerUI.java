package firetalk.operators.source;

import java.awt.BorderLayout;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JList;
import java.awt.GridBagConstraints;
import javax.swing.JButton;

import firetalk.UI.ServerListRenderer;
import firetalk.db.Repository;
import javax.swing.JScrollPane;

public class ServerManagerUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JPanel jPanel = null;
	private JPanel jPanel1 = null;
	private JList jList = null;
	private JList jList1 = null;
	private JPanel jPanel2 = null;
	private JButton jButton = null;
	private DefaultListModel modelAndroid = new DefaultListModel();
	private DefaultListModel modelUI = new DefaultListModel();
	private Server server = null;
	private UIServer serverUI = null;
	private JScrollPane jScrollPane = null;
	private JScrollPane jScrollPane1 = null;

	/**
	 * This is the default constructor
	 */
	public ServerManagerUI() {
		super();
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
		this.setSize(388, 200);
		this.setContentPane(getJContentPane());
		this.setTitle("JFrame");

	}

	public void updateAndroidList() {
		modelAndroid.clear();
		for (StreamHandle handle : Repository.androidHandles.values())
			modelAndroid.addElement(handle.getUserId());
		this.jList.repaint();
	}

	public void updateUIList() {
		modelUI.clear();
		for (UIStreamHandle handle : Repository.uiHandles.values())
			modelUI.addElement(handle.userId);
		this.jList.repaint();
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BoxLayout(getJContentPane(),
					BoxLayout.X_AXIS));
			jContentPane.add(getJPanel(), null);
			jContentPane.add(getJPanel1(), null);
		}
		return jContentPane;
	}

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
			jPanel.setBorder(BorderFactory.createTitledBorder(null,
					"android list", TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, new Font("Dialog",
							Font.BOLD, 12), new Color(51, 51, 51)));
			jPanel.setPreferredSize(new Dimension(500, 500));
			jPanel.add(getJScrollPane(), gridBagConstraints1);
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
			jPanel1 = new JPanel();
			jPanel1.setLayout(new BorderLayout());
			jPanel1.setBorder(BorderFactory.createTitledBorder(null,
					"display list", TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, new Font("Dialog",
							Font.BOLD, 12), new Color(51, 51, 51)));
			jPanel1.setPreferredSize(new Dimension(500, 500));
			jPanel1.add(getJPanel2(), BorderLayout.NORTH);
			jPanel1.add(getJScrollPane1(), BorderLayout.CENTER);
		}
		return jPanel1;
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
			// jList.setCellRenderer(new ServerListRenderer());
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
			jList1.setCellRenderer(new ServerListRenderer());
			this.updateUIList();
		}
		return jList1;
	}

	/**
	 * This method initializes jPanel2
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jPanel2 = new JPanel();
			jPanel2.setLayout(new GridBagLayout());
			jPanel2.add(getJButton(), new GridBagConstraints());
		}
		return jPanel2;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("set main");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Object obj = jList1.getSelectedValue();
					String id=obj==null?null:obj.toString();			
					if (id != null) {
						for (UIStreamHandle h : Repository.uiHandles.values()) {
							if (!h.userId.equals(id)) {
								if (h.isMainDisplay())
									h.setMainDisplay(false);
							} else if (!h.isMainDisplay())
								h.setMainDisplay(true);
						}
						updateUIList();
					}
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJList());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jScrollPane1
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getJList1());
		}
		return jScrollPane1;
	}

	public static void main(String[] args) {
		new ServerManagerUI().setVisible(true);
	}
} // @jve:decl-index=0:visual-constraint="10,10"
