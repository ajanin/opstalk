package firetalk.UI;

import java.awt.GridBagLayout;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JList;

import firetalk.db.Repository;
import firetalk.model.Event;

import java.awt.GridBagConstraints;
import java.util.Date;

import javax.swing.DefaultListCellRenderer;
import javax.swing.border.EtchedBorder;
import java.awt.SystemColor;

public class OverallObjPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JList jList = null;
	private DefaultListModel model = new DefaultListModel();
	/**
	 * This is the default constructor
	 */
	public OverallObjPanel() {
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
		this.setBorder(BorderFactory.createTitledBorder(null, "Opstalk Mission Progress", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
		this.add(getJList(), gridBagConstraints);
	}

	/**
	 * This method initializes jList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getJList() {
		if (jList == null) {
			jList = new JList();
			jList.setBackground(SystemColor.activeCaption);
			jList.setModel(model);
			jList.setCellRenderer(new ListRenderer());
			updateList();
		}
		return jList;
	}

	public void updateList(){
		model.clear();
		for (int i = 0; i < Repository.overallObjs.size(); i++)
			model.addElement(Repository.overallObjs.get(i));
		this.jList.repaint();
	}
	public void addEvent(Event event) {
		if (event != null
				&& (event.getEventType() == Event.CHECK_REACH )) {
			Date date=new Date(event.getValidTime());
			String content = "";
			for (int i = 0; i < event.getContent().length; i++)
				content += (char) event.getContent()[i];
			String mes = String.format("Team %s reached point %s at %s", event.getId(),
					content,date.toString());
			Repository.overallObjs.add(mes);
			this.updateList();
		}
	}
}
