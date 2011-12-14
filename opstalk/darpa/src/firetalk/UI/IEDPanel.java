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
import firetalk.model.IEDPoint;

public class IEDPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane = null;
	private JList jList = null;
	private JPanel jPanel = null;
	// final LinkedList<String> strList = new LinkedList<String>();
	private JButton addButton = null;
	private JButton deleteAllButton = null;
	private JButton deleteSelectButton = null;
	private JPanel jPanel1 = null;
	private JTextField mesField = null;
	private JTextField latField = null;
	private JTextField lonField = null;
	private JPanel jPanel3 = null;
	private JPanel jPanel4 = null;
	private JPanel jPanel5 = null;
	private DefaultListModel model = null;
	private MainWindow parent = null;
	private JLabel mesLabel = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JPanel jPanel2 = null;
	private JButton jButton = null;

	/**
	 * This is the default constructor
	 * 
	 */
	public IEDPanel() {
		super();
		initialize();
	}

	public void setParent(MainWindow parent) {
		this.parent = parent;
	}

	public void updateListFromDB() {
		model = new DefaultListModel();
		for (int i = 0; i < UIRepository.IEDList.size(); i++)
			model.addElement("" + i);
		this.jList.repaint();
	}

	public void updateListToDB() {
		model = new DefaultListModel();
		for (int i = 0; i < UIRepository.IEDList.size(); i++)
			model.addElement("" + i);
		UIRepository.storeIEDPoints();
		if (parent.network != null
				&& parent.network.getStatus() == Status.CONNECTED) {
			parent.network.addEvent(new DBEvent(DBEvent.IED, UIRepository
					.retrieveDB(DBEvent.IED), parent.network.userId));
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
					"IED list", TitledBorder.DEFAULT_JUSTIFICATION,
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
			jList.setCellRenderer(new IEDRenderer());
			jList.setBackground(SystemColor.info);
			model = new DefaultListModel();
			jList.addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent e) {
					int[] inds = jList.getSelectedIndices();
					if (inds.length == 1) {
						IEDPoint cp = UIRepository.IEDList.get(inds[0]);
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
					String mes = mesField.getText();
					double lat = Double.parseDouble(latField.getText());
					double lon = Double.parseDouble(lonField.getText());
					UIRepository.IEDList.add(new IEDPoint("server", mes, System
							.currentTimeMillis(), lat, lon));
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
							UIRepository.IEDList.clear();
							updateListToDB();
							parent.updateMarkers();
						}
					});
		}
		return deleteAllButton;
	}
	public void setMain(boolean isMain){
		jPanel1.setVisible(false);
		jButton.setText("show");
		jButton.setEnabled(isMain);
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
							LinkedList<IEDPoint> temp = new LinkedList<IEDPoint>();
							int i = 0;
							int j = 0;
							for (IEDPoint p : UIRepository.IEDList) {
								if (j >= inds.length || i != inds[j])
									temp.add(p);
								else
									j++;
								i++;
							}

							UIRepository.IEDList = temp;

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
			jPanel1.add(getJPanel3(), null);
			jPanel1.add(getJPanel4(), null);
			jPanel1.add(getJPanel5(), null);
		}
		return jPanel1;
	}

	/**
	 * This method initializes mesField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getMesField() {
		if (mesField == null) {
			mesField = new JTextField();
			mesField.setText("                        ");
		}
		return mesField;
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
			mesLabel = new JLabel();
			mesLabel.setText("Message");
			FlowLayout flowLayout2 = new FlowLayout();
			flowLayout2.setAlignment(FlowLayout.LEFT);
			jPanel3 = new JPanel();
			jPanel3.setLayout(flowLayout2);
			jPanel3.add(mesLabel, null);
			jPanel3.add(getMesField(), null);
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
			jLabel = new JLabel();
			jLabel.setText("latitude");
			FlowLayout flowLayout3 = new FlowLayout();
			flowLayout3.setAlignment(FlowLayout.LEFT);
			jPanel4 = new JPanel();
			jPanel4.setLayout(flowLayout3);
			jPanel4.add(jLabel, null);
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
			jLabel1 = new JLabel();
			jLabel1.setText("longitude");
			FlowLayout flowLayout4 = new FlowLayout();
			flowLayout4.setAlignment(FlowLayout.LEFT);
			jPanel5 = new JPanel();
			jPanel5.setLayout(flowLayout4);
			jPanel5.add(jLabel1, null);
			jPanel5.add(getLonField(), null);
		}
		return jPanel5;
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
