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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.sun.java.swing.plaf.windows.WindowsButtonUI;
import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;
import com.sun.java.swing.plaf.windows.WindowsLabelUI;

import firetalk.db.UIRepository;
import firetalk.model.CheckPoint;
import firetalk.model.DBEvent;
import firetalk.model.RallyPoint;

public class RallyPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane = null;
	private JList jList = null;
	private JPanel jPanel = null;
	// final LinkedList<String> strList = new LinkedList<String>();
	private JButton addButton = null;
	private JButton deleteAllButton = null;
	private JButton deleteSelectButton = null;
	private JPanel jPanel1 = null;
	private JPanel jPanel2 = null;
	private JLabel jLabel = null;
	private JTextField nameField = null;
	private JLabel jLabel1 = null;
	private JTextField latField = null;
	private JLabel jLabel2 = null;
	private JTextField lonField = null;
	private JPanel jPanel3 = null;
	private JPanel jPanel4 = null;
	private JPanel jPanel5 = null;
	private JLabel peopleField = null;
	private JComboBox jComboBox = null;
	private DefaultListModel model = null;
	private MainWindow parent = null;
	private JPanel jPanel6 = null;
	private JButton jButton = null;
	/**
	 * This is the default constructor
	 * 
	 */
	public RallyPanel() {
		super();
		initialize();
	}

	public void setParent(MainWindow parent) {
		this.parent = parent;
	}

	public void updateListFromDB(){ //update list from repository
		model.clear();
		for (int i = 0; i < UIRepository.rallyList.size(); i++)
			model.addElement("" + i);
		this.jList.repaint();
	}
	public void updateListToDB() { //update list, and write to DB
		model.clear();
		for (int i = 0; i < UIRepository.rallyList.size(); i++)
			model.addElement("" + i);
		UIRepository.storeRallyPoints();
		parent.network.addEvent(new DBEvent(DBEvent.rally, UIRepository
				.retrieveDB(DBEvent.rally), parent.network.userId));
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
		this.add(getJPanel6(), BorderLayout.SOUTH);
	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setBorder(BorderFactory.createTitledBorder(null, "Rally Point list", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
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
			jList.setCellRenderer(new RallyRenderer());
			jList.setBackground(SystemColor.info);
			model = new DefaultListModel();
			jList.addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent e) {
					int[] inds=jList.getSelectedIndices();
					if(inds.length==1){
						CheckPoint cp=UIRepository.rallyList.get(inds[0]);
						parent.setInfoPanel(cp.toString());
						parent.mapPanel.resetMap(cp.lon, cp.lat);
					}

				}
			});
			jList.setModel(model);
			updateListFromDB();
			// jList.setModel(new javax.swing.AbstractListModel() {
			//
			// /**
			// *
			// */
			// private static final long serialVersionUID = 1L;
			//
			// public int getSize() {
			// return Repository.checkPoints.size();
			// }
			//
			// public Object getElementAt(int i) {
			// return Repository.checkPoints.get(i);
			// }
			//
			// });
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
					String name = nameField.getText();
					String id = "" + UIRepository.rallyList.size();
					double lat = Double.parseDouble(latField.getText());
					double lon = Double.parseDouble(lonField.getText());
					String userID = (String) jComboBox.getSelectedItem();
					userID = userID.substring(0, userID.indexOf(':'));
					UIRepository.rallyList.add(new RallyPoint(id, userID, name,
							lat, lon));
					updateListToDB();
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
							UIRepository.rallyList.clear();
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
							LinkedList<RallyPoint> temp = new LinkedList<RallyPoint>();
							int i = 0;
							int j = 0;
							for (RallyPoint p : UIRepository.rallyList) {
								if (j>=inds.length||i != inds[j])
									temp.add(p);
								else
									j++;
								i++;
							}

							UIRepository.rallyList = temp;

							updateListToDB();
							parent.updateMarkers();
							

						}
					});
		}
		return deleteSelectButton;
	}

	public void selectListItem(int index) {
		jList.setSelectedIndex(index);
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
			jPanel1.setVisible(false);
			jPanel1.add(getJPanel(), null);
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
			jLabel = new JLabel();
			jLabel.setText("      name");
			jLabel.setUI(new WindowsLabelUI());
			jPanel2 = new JPanel();
			jPanel2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			jPanel2.setLayout(flowLayout);
			jPanel2.add(peopleField, null);
			jPanel2.add(getJComboBox(), null);
		}
		return jPanel2;
	}

	/**
	 * This method initializes nameField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getNameField() {
		if (nameField == null) {
			nameField = new JTextField();
			nameField.setText("                        ");
		}
		return nameField;
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
			FlowLayout flowLayout2 = new FlowLayout();
			flowLayout2.setAlignment(FlowLayout.LEFT);
			jPanel3 = new JPanel();
			jPanel3.setLayout(flowLayout2);
			jPanel3.add(jLabel, null);
			jPanel3.add(getNameField(), null);
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
			String[] ids = new String[UIRepository.peopleList.size()];
			int i = 0;
			for (String id : UIRepository.peopleList.keySet()) {
				ids[i++] = id + ": " + UIRepository.peopleList.get(id).getName();
			}
			jComboBox = new JComboBox(ids);
			jComboBox.setUI(new WindowsComboBoxUI());
		}
		return jComboBox;
	}

	/**
	 * This method initializes jPanel6	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel6() {
		if (jPanel6 == null) {
			jPanel6 = new JPanel();
			jPanel6.setLayout(new BorderLayout());
			jPanel6.add(getJPanel1(), BorderLayout.CENTER);
			jPanel6.add(getJButton(), BorderLayout.NORTH);
		}
		return jPanel6;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
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
		return jButton;
	}
}
