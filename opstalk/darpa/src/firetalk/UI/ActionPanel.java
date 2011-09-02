package firetalk.UI;

import java.awt.GridBagLayout;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JScrollPane;
import java.awt.GridBagConstraints;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.JList;

import firetalk.db.Repository;
import firetalk.model.Event;
import javax.swing.DefaultListCellRenderer;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.ImageIcon;
import javax.swing.ListSelectionModel;
import java.awt.SystemColor;

public class ActionPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane = null;
	private JList jList = null;
	private DefaultListModel model = null;
	private LinkedList<Event> realEvents = new LinkedList<Event>();
	private LinkedList<String> events = new LinkedList<String>(); // @jve:decl-index=0:
	private MainWindow parent = null;

	public void setParent(MainWindow parent) {
		this.parent = parent;
	}

	public void updateList() {
		model.clear();
		for (int i = 0; i < events.size(); i++)
			model.addElement(events.get(i));
		this.jList.repaint();
	}

	public void addEvent(String event) {
		model.addElement(event);
		this.jList.repaint();
	}

	public void clearEvents() {
		model.clear();
		this.jList.repaint();
	}

	/**
	 * This is the default constructor
	 */
	public ActionPanel() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.weightx = 1.0;
		this.setSize(300, 200);
		this.setLayout(new GridBagLayout());
		this.setBorder(BorderFactory.createTitledBorder(null, "Actions",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION,
				new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
		this.add(getJScrollPane(), gridBagConstraints);
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
	 * This method initializes jList
	 * 
	 * @return javax.swing.JList
	 */
	private JList getJList() {
		if (jList == null) {
			jList = new JList();
			model = new DefaultListModel();
			jList.setModel(model);
			jList.setCellRenderer(new ListRenderer());
			jList.setBackground(SystemColor.activeCaption);
			jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jList.addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent e) {
					int[] inds = jList.getSelectedIndices();
					if (inds.length == 1) {
						Event event = realEvents.get(inds[0]);
						parent.setInfoPanel("time: " + (new Date()) + "\n"
								+ event.extractInfo());
						parent.mapPanel.resetMap(event.getLongitude(), event
								.getLatitude());
					}

				}
			});
		}
		return jList;
	}

	public void addEvent(Event event) {
		if (event != null
				&& (event.getEventType() == Event.CHECK_REACH || event
						.getEventType() == Event.MESSAGE)) {
			realEvents.add(event);
			String mes = event.extractInfo();
			if (mes != null)
				this.addEvent(mes);
		}
	}
}
