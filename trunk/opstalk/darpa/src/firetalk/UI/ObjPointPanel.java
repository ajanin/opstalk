package firetalk.UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.sun.java.swing.plaf.windows.WindowsButtonUI;
import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;
import com.sun.java.swing.plaf.windows.WindowsTreeUI;

import firetalk.UI.UIClient.Status;
import firetalk.db.UIRepository;
import firetalk.model.CheckPoint;
import firetalk.model.DBEvent;
import firetalk.model.ObjPoint;
import firetalk.model.DBEvent.DBType;

public class ObjPointPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane = null;
	private JPanel jPanel = null;
	// final LinkedList<String> strList = new LinkedList<String>();
	private JButton addButton = null;
	private JButton deleteAllButton = null;
	private JButton deleteSelectButton = null;
	private JPanel jPanel1 = null;
	private JPanel jPanel2 = null;
	private JLabel jLabel1 = null;
	private JTextField latField = null;
	private JLabel jLabel2 = null;
	private JTextField lonField = null;
	private JPanel jPanel3 = null;
	private JPanel jPanel4 = null;
	private JPanel jPanel5 = null;
	private JLabel peopleField = null;
	private JComboBox jComboBox = null;
	private DefaultComboBoxModel comboModel = null;
	private MainWindow parent = null;
	private JTree jTree = null;
	private JPanel jPanel6 = null;
	private JCheckBox jCheckBox = null;
	private DefaultMutableTreeNode top = null; // @jve:decl-index=0:
	private DefaultTreeModel treeModel = null;
	private JLabel jLabel = null;
	private JComboBox hourCombo = null;
	private JLabel jLabel3 = null;
	private JComboBox minuteCombo = null;
	private JLabel jLabel4 = null;
	private JComboBox secondCombo = null;
	private JComboBox ampmCombo = null;
	private JPanel jPanel7 = null;
	private JButton jButton = null;

	/**
	 * This is the default constructor
	 * 
	 */
	public ObjPointPanel() {
		super();
		initialize();
	}

	public void setParent(MainWindow parent) {
		this.parent = parent;
	}
	public void updateTreeFromDB() {
		this.createNodes();
		this.repaint();
	}
	public void updateTreeToDB() {
		this.createNodes();
		UIRepository.storeObjPoints();
		UIRepository.storeCheckPoints();
		if (parent.network != null
				&& parent.network.getStatus() == Status.CONNECTED) {
			parent.network.addEvent(new DBEvent(DBType.objPoint, UIRepository
					.retrieveDB(DBType.objPoint.ordinal()), parent.network.userId));
			parent.network.addEvent(new DBEvent(DBType.wayPoint, UIRepository
					.retrieveDB(DBType.wayPoint.ordinal()), parent.network.userId));
		}
		this.repaint();
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
		this.add(getJPanel7(), BorderLayout.SOUTH);
	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setBorder(BorderFactory.createTitledBorder(null, "Way Point list", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jScrollPane.setPreferredSize(new Dimension(115, 300));
			jScrollPane.setViewportView(getJTree());
		}
		return jScrollPane;
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
			jPanel.add(getAddButton(), null);
			jPanel.add(getDeleteAllButton(), null);
			jPanel.add(getDeleteSelectButton(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes addButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getAddButton() {
		if (addButton == null) {
			addButton = new JButton();
			addButton.setText("add");
			addButton.setUI(new WindowsButtonUI());
			addButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String id = "" + UIRepository.checkPoints.size();
					double lat = Double.parseDouble(latField.getText());
					double lon = Double.parseDouble(lonField.getText());
					String userID = (String) jComboBox.getSelectedItem();
					userID = userID.substring(0, userID.indexOf(':'));
					String deadline = "" + hourCombo.getSelectedItem() + ":"
							+ minuteCombo.getSelectedItem() + ":"
							+ secondCombo.getSelectedItem() + " "
							+ ampmCombo.getSelectedItem();
					if (jCheckBox.isSelected()) {
						UIRepository.objPoints.put(userID, new ObjPoint(id,
								userID, "", lat, lon));

						CheckPoint cp = new CheckPoint(userID + "-1", userID,
								"", lat, lon, true, false, deadline);
						UIRepository.checkPoints.add(cp);

					} else {
						int count = 0;
						for (CheckPoint cp : UIRepository.checkPoints) {
							if (cp.userID.equals(userID))
								count++;
						}
						UIRepository.checkPoints.add(new CheckPoint(userID + "-"
								+ (count + 1), userID, "", lat, lon, false,
								false, deadline));
					}
					updateCombo();
					updateTreeToDB();
					parent.updateMarkers();
				}
			});
		}
		return addButton;
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
							UIRepository.checkPoints.clear();
							UIRepository.objPoints.clear();
							updateTreeToDB();
							updateCombo();
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
							// int[] inds = jList.getSelectedIndices();
							// for (int i = 0; i < inds.length; i++) {
							// Repository.checkPoints.remove(inds[i]);
							// }
							TreePath path = jTree.getSelectionPath();
							String text = path.getLastPathComponent()
									.toString();
							String id = text.substring(text.indexOf(' ') + 1,
									text.indexOf(':'));
							switch (path.getPathCount()) {
							case 2:
								// obj points
								UIRepository.removeObjPoint(id);
								break;
							case 3:
								// way points
								for (Iterator<CheckPoint> it = UIRepository.checkPoints
										.iterator(); it.hasNext();) {
									if (it.next().id.equals(id)) {
										it.remove();
										break;
									}
								}
								break;
							}

							updateTreeToDB();
							updateCombo();
							parent.updateMarkers();

						}
					});
		}
		return deleteSelectButton;
	}

	public void selectListItem(int index) {
		// jList.setSelectedIndex(index);
	}

	public void addNewCheckPointFields(double lat, double lon) {
		latField.setText("" + lat);
		lonField.setText("" + lon);
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
			jPanel1.setBorder(BorderFactory.createTitledBorder(null,
					"Setup Panel", TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, new Font("Dialog",
							Font.BOLD, 12), new Color(51, 51, 51)));
			jPanel1.setVisible(false);
			jPanel1.add(getJPanel(), null);
			jPanel1.add(getJPanel6(), null);
			jPanel1.add(getJPanel2(), null);
			jPanel1.add(getJPanel3(), null);
			jPanel1.add(getJPanel4(), null);
			jPanel1.add(getJPanel5(), null);
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
			peopleField = new JLabel();
			peopleField.setText("People ID");
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(FlowLayout.LEFT);
			jLabel2 = new JLabel();
			jLabel2.setText("longitude");
			jLabel1 = new JLabel();
			jLabel1.setText("   latitude");
			jPanel2 = new JPanel();
			jPanel2.setLayout(flowLayout);
			jPanel2.add(peopleField, null);
			jPanel2.add(getJComboBox(), null);
		}
		return jPanel2;
	}

	/**
	 * This method initializes latField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getLatField() {
		if (latField == null) {
			latField = new JTextField();
			latField.setText("                        ");
			latField.setMinimumSize(new Dimension(4, 22));
		}
		return latField;
	}

	/**
	 * This method initializes lonField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getLonField() {
		if (lonField == null) {
			lonField = new JTextField();
			lonField.setText("                        ");
		}
		return lonField;
	}

	/**
	 * This method initializes jPanel3
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			jLabel4 = new JLabel();
			jLabel4.setText("ss");
			jLabel3 = new JLabel();
			jLabel3.setText("mm");
			jLabel = new JLabel();
			jLabel.setText("hh");
			FlowLayout flowLayout2 = new FlowLayout();
			flowLayout2.setAlignment(FlowLayout.LEFT);
			jPanel3 = new JPanel();
			jPanel3.setLayout(flowLayout2);
			jPanel3.add(jLabel, null);
			jPanel3.add(getHourCombo(), null);
			jPanel3.add(jLabel3, null);
			jPanel3.add(getMinuteCombo(), null);
			jPanel3.add(jLabel4, null);
			jPanel3.add(getSecondCombo(), null);
			jPanel3.add(getAmpmCombo(), null);
		}
		return jPanel3;
	}

	/**
	 * This method initializes jPanel4
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel4() {
		if (jPanel4 == null) {
			FlowLayout flowLayout3 = new FlowLayout();
			flowLayout3.setAlignment(FlowLayout.LEFT);
			jPanel4 = new JPanel();
			jPanel4.setLayout(flowLayout3);
			jPanel4.add(jLabel1, null);
			jPanel4.add(getLatField(), null);
		}
		return jPanel4;
	}

	/**
	 * This method initializes jPanel5
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel5() {
		if (jPanel5 == null) {
			FlowLayout flowLayout4 = new FlowLayout();
			flowLayout4.setAlignment(FlowLayout.LEFT);
			jPanel5 = new JPanel();
			jPanel5.setLayout(flowLayout4);
			jPanel5.add(jLabel2, null);
			jPanel5.add(getLonField(), null);
		}
		return jPanel5;
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
			comboModel = new DefaultComboBoxModel();
			jComboBox.setModel(comboModel);
			this.updateCombo();
		}
		return jComboBox;
	}

	/**
	 * This method initializes jTree
	 * 
	 * @return javax.swing.JTree
	 */
	private JTree getJTree() {
		if (jTree == null) {

			// treeModel = new DefaultTreeModel(rootNode);
			// createNodes(treeModel);
			top = new DefaultMutableTreeNode("Objectives");
			treeModel = new DefaultTreeModel(top);
			jTree = new JTree();
			jTree.setUI(new WindowsTreeUI());
			jTree.setBackground(SystemColor.info);
			jTree.setModel(treeModel);
			this.createNodes();
			jTree.setShowsRootHandles(true);
			jTree.setCellRenderer(new ObjPanelRender());
			jTree
					.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
						public void valueChanged(
								javax.swing.event.TreeSelectionEvent e) {
							jTreeValueChanged(e);
						}
					});
			jTree.getSelectionModel().setSelectionMode(
					TreeSelectionModel.SINGLE_TREE_SELECTION);
		}
		return jTree;
	}

	private void createNodes() {
		top.removeAllChildren();
		DefaultMutableTreeNode objs = null;
		DefaultMutableTreeNode waypoints = null;
		for (ObjPoint obj : UIRepository.objPoints.values()) {
			objs = new DefaultMutableTreeNode("objPoint " + obj.userID + ": "
					+ UIRepository.peopleList.get(obj.userID).getName());
			for (CheckPoint cp : UIRepository.checkPoints) {
				if (cp.userID.equals(obj.userID)) {
					waypoints = new DefaultMutableTreeNode("wayPoint"
							+ (cp.isObj() ? "(obj) " : " ") + cp.id + ": "
							+ "("+cp.deadline+")");
					objs.add(waypoints);
				}
			}
			top.add(objs);
		}
		treeModel.reload();
		// this.jTree.setModel(new DefaultTreeModel(top));

	}

	/**
	 * This method initializes jPanel6
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel6() {
		if (jPanel6 == null) {
			FlowLayout flowLayout5 = new FlowLayout();
			flowLayout5.setAlignment(FlowLayout.LEFT);
			jPanel6 = new JPanel();
			jPanel6.setLayout(flowLayout5);
			jPanel6.add(getJCheckBox(), null);
		}
		return jPanel6;
	}

	/**
	 * This method initializes jCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBox() {
		if (jCheckBox == null) {
			jCheckBox = new JCheckBox();
			jCheckBox.setText("Objective Point");
			jCheckBox.setSelected(true);
			jCheckBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					updateCombo();

				}
			});
		}
		return jCheckBox;
	}

	private void createComboModel() {
		this.comboModel.removeAllElements();
		for (String id : UIRepository.peopleList.keySet()) {
			if ((UIRepository.objPoints.get(id) == null) == jCheckBox
					.isSelected())
				comboModel.addElement(id + ": "
						+ UIRepository.peopleList.get(id).getName());
		}
	}

	public void updateCombo() {
		this.createComboModel();
		this.jComboBox.repaint();
	}

	private void jTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {
		TreePath path = evt.getNewLeadSelectionPath();
		if (path != null) {
			String text = path.getLastPathComponent().toString();
			String id = text
					.substring(text.indexOf(' ') + 1, text.indexOf(':'));
			String userId = null;
			switch (path.getPathCount()) {
			case 2:
				// obj points
				ObjPoint objP = UIRepository.objPoints.get(id);
				this.jCheckBox.setSelected(true);
				this.latField.setText("" + objP.lat);
				this.lonField.setText("" + objP.lon);
				userId = id;
				parent.setInfoPanel(objP.toString());
				parent.mapPanel.resetMap(objP.lon, objP.lat);
				break;
			case 3:
				// way points
				for (CheckPoint cp : UIRepository.checkPoints) {
					if (cp.id.equals(id)) {
						userId = cp.userID;
						this.jCheckBox.setSelected(false);
						this.latField.setText("" + cp.lat);
						this.lonField.setText("" + cp.lon);
						parent.setInfoPanel(cp.toString());
						parent.mapPanel.resetMap(cp.lon, cp.lat);
						break;
					}
				}
				break;
			}
			if (path.getPathCount() > 1) {
				this.createComboModel();

				for (int i = 0; i < this.jComboBox.getItemCount(); i++) {
					String item = this.jComboBox.getModel().getElementAt(i)
							.toString();
					if (item.substring(0, item.indexOf(':')).equals(userId)) {
						this.jComboBox.setSelectedIndex(i);
						break;
					}
				}
				this.jComboBox.repaint();
			}
		}
	}

	/**
	 * This method initializes hourCombo
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getHourCombo() {
		if (hourCombo == null) {
			String[] hh = { "01", "02", "03", "04", "05", "06", "07", "08",
					"09", "10", "11", "12" };
			hourCombo = new JComboBox(hh);
			int hours = new Date().getHours();
			if (hours == 0 || hours == 12)
				hourCombo.setSelectedIndex(11);
			else if (hours < 12)
				hourCombo.setSelectedIndex(hours - 1);
			else
				hourCombo.setSelectedIndex(hours - 13);
		}
		return hourCombo;
	}

	/**
	 * This method initializes minuteCombo
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getMinuteCombo() {
		if (minuteCombo == null) {
			minuteCombo = new JComboBox();
			String[] mm = new String[60];
			for (int i = 0; i < 10; i++)
				mm[i] = "0" + i;
			for (int i = 10; i < 60; i++)
				mm[i] = "" + i;
			minuteCombo = new JComboBox(mm);
			int minutes = new Date().getMinutes();
			minuteCombo.setSelectedIndex(minutes);

		}
		return minuteCombo;
	}

	/**
	 * This method initializes secondCombo
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getSecondCombo() {
		if (secondCombo == null) {
			secondCombo = new JComboBox();
			String[] ss = new String[60];
			for (int i = 0; i < 10; i++)
				ss[i] = "0" + i;
			for (int i = 10; i < 60; i++)
				ss[i] = "" + i;
			secondCombo = new JComboBox(ss);
			int seconds = new Date().getSeconds();
			secondCombo.setSelectedIndex(seconds);
		}
		return secondCombo;
	}

	/**
	 * This method initializes ampmCombo
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getAmpmCombo() {
		if (ampmCombo == null) {
			String[] ampm = new String[] { "am", "pm" };
			ampmCombo = new JComboBox(ampm);
			if (new Date().getHours() >= 12)
				ampmCombo.setSelectedIndex(1); // pm
			else
				ampmCombo.setSelectedIndex(0); // am
		}
		return ampmCombo;
	}

	/**
	 * This method initializes jPanel7	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel7() {
		if (jPanel7 == null) {
			jPanel7 = new JPanel();
			jPanel7.setLayout(new BorderLayout());
			jPanel7.add(getJPanel1(), BorderLayout.CENTER);
			jPanel7.add(getJButton(), BorderLayout.NORTH);
		}
		return jPanel7;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("show");
			jButton.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					if(jPanel1.isVisible()){
						jPanel1.setVisible(false);
						jButton.setText("show");
					}
					else{
						jPanel1.setVisible(true);
						jButton.setText("hide");
					}
					
				}});
		}
		return jButton;
	}
}
