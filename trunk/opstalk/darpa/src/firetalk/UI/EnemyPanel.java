package firetalk.UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.sun.java.swing.plaf.windows.WindowsButtonUI;

import firetalk.UI.UIClient.Status;
import firetalk.db.UIRepository;

import firetalk.model.DBEvent;
import firetalk.model.Enemy;


public class EnemyPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane = null;
	private JList jList = null;
	private JPanel jPanel = null;
	// final LinkedList<String> strList = new LinkedList<String>();
	private JButton addButton = null;
	private JButton deleteAllButton = null;
	private JButton deleteSelectButton = null;
	private JPanel jPanel1 = null;
	private DefaultListModel model = null;
	private MainWindow parent = null;
	private JPanel jPanel2 = null;
	private JButton jButton = null;

	/**
	 * This is the default constructor
	 * 
	 */
	public EnemyPanel() {
		super();
		initialize();
	}

	public void setParent(MainWindow parent) {
		this.parent = parent;
	}

	public void updateListFromDB() {
		model.clear();
		for (int i = 0; i < UIRepository.enemyList.size(); i++)
			model.addElement("" + i);
		this.jList.repaint();
	}
	public void updateListToDB() {
		model.clear();
		for (int i = 0; i < UIRepository.enemyList.size(); i++)
			model.addElement("" + i);
		UIRepository.storeEnemys();
		if (parent.network != null
				&& parent.network.getStatus() == Status.CONNECTED) {
			parent.network.addEvent(new DBEvent(DBEvent.enemy, UIRepository
					.retrieveDB(DBEvent.enemy), parent.network.userId));
		}
		this.jList.repaint();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 200);
		this.setLayout(new BorderLayout());
		this.add(getJScrollPane(), BorderLayout.CENTER);
		this.add(getJPanel2(), BorderLayout.SOUTH);
	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setBorder(BorderFactory.createTitledBorder(null,
					"Enemy list", TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, new Font("Dialog",
							Font.BOLD, 12), new Color(51, 51, 51)));
			jScrollPane.setPreferredSize(new Dimension(266, 300));
			jScrollPane.setViewportView(getJList());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jList
	 * 
	 * @return javax.swing.JList
	 */
	private JList getJList() {
		if (jList == null) {
			jList = new JList();
			jList.setCellRenderer(new EnemyRenderer());
			jList.setBackground(SystemColor.info);
			model = new DefaultListModel();
			jList.addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent e) {
					int[] inds = jList.getSelectedIndices();
					if (inds.length == 1) {
						Enemy cp = UIRepository.enemyList.get(inds[0]);
						parent.setInfoPanel(cp.toString());
						parent.mapPanel.resetMap(cp.getLongitude(), cp
								.getLatitude());
					}

				}
			});
			jList.setModel(model);
			updateListFromDB();
		}
		return jList;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			FlowLayout flowLayout1 = new FlowLayout();
			flowLayout1.setAlignment(FlowLayout.LEFT);
			jPanel = new JPanel();
			jPanel.setBorder(BorderFactory.createLineBorder(
					SystemColor.activeCaptionBorder, 1));
			jPanel.setLayout(flowLayout1);
			jPanel.add(getDeleteAllButton(), null);
			jPanel.add(getDeleteSelectButton(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes deleteAllButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getDeleteAllButton() {
		if (deleteAllButton == null) {
			deleteAllButton = new JButton();
			deleteAllButton.setText("delete all");
			deleteAllButton.setUI(new WindowsButtonUI());
			deleteAllButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							UIRepository.enemyList.clear();
							updateListToDB();
							parent.updateMarkers();
						}
					});
		}
		return deleteAllButton;
	}

	/**
	 * This method initializes deleteSelectButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getDeleteSelectButton() {
		if (deleteSelectButton == null) {
			deleteSelectButton = new JButton();
			deleteSelectButton.setText("delete selected");
			deleteSelectButton.setUI(new WindowsButtonUI());
			deleteSelectButton
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							int[] inds = jList.getSelectedIndices();
							LinkedList<Enemy> temp = new LinkedList<Enemy>();
							int i = 0;
							int j = 0;
							for (Enemy p : UIRepository.enemyList) {
								if (j >= inds.length || i != inds[j])
									temp.add(p);
								else
									j++;
								i++;
							}

							UIRepository.enemyList = temp;
							updateListToDB();
							parent.updateMarkers();
						}
					});
		}
		return deleteSelectButton;
	}
	public void setMain(boolean isMain){
		jPanel1.setVisible(false);
		jButton.setText("show");
		jButton.setEnabled(isMain);
	}
	public void selectListItem(int index) {
		jList.setSelectedIndex(index);
	}

	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.setLayout(new BoxLayout(getJPanel1(), BoxLayout.Y_AXIS));
			jPanel1.setVisible(false);
			jPanel1.add(getJPanel(), null);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jPanel2
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jPanel2 = new JPanel();
			jPanel2.setLayout(new BorderLayout());
			jPanel2.add(getJPanel1(), BorderLayout.CENTER);
			jPanel2.add(getJButton(), BorderLayout.NORTH);
		}
		return jPanel2;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		jButton = new JButton();
		jButton.setText("show");
		jButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (jPanel1.isVisible()) {
					jPanel1.setVisible(false);
					jButton.setText("show");
				} else {
					jPanel1.setVisible(true);
					jButton.setText("hide");
				}

			}
		});
		return jButton;
	}
}
