package firetalk.UI;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.GridBagConstraints;
import java.sql.SQLException;
import java.util.Iterator;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import firetalk.db.UIRepository;
import firetalk.model.Location;
import firetalk.model.People;
import firetalk.model.Team;
import java.awt.SystemColor;

public class EntityPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane = null;
	static JTree jTree = null;
	private MainWindow parent = null;

	/**
	 * This is the default constructor
	 * 
	 * @throws SQLException
	 */
	public EntityPanel() {
		super();
		initialize();
	}

	public void setParent(MainWindow parent) {
		this.parent = parent;
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
			jScrollPane.setViewportView(getJTree());

			// jTree.setSelectionModel(
			// TreeSelectionModel.SINGLE_TREE_SELECTION);

			jTree.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) jTree
							.getLastSelectedPathComponent();

					/* if nothing is selected */
					if (node == null)
						return;

					/* retrieve the node that was selected */
					// System.out.println(node.toString());
					jTreeValueChanged(e);
				}
			});
		}

		return jScrollPane;
	}

	/**
	 * This method initializes jTree
	 * 
	 * @return javax.swing.JTree
	 */
	private JTree getJTree() {

		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Teams");
		createNodes(top);
		jTree = new JTree(top);
		// jTree.setCellRenderer(new EntityRenderer());
		jTree.setBackground(SystemColor.info);
		jTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		return jTree;
	}

	private void createNodes(DefaultMutableTreeNode top) {

		DefaultMutableTreeNode team = null;
		DefaultMutableTreeNode members = null;
		for (Team t : UIRepository.teamList.values()) {
			team = new DefaultMutableTreeNode(t.teamID + ": " + t.teamName);
			top.add(team);
			for (String id : UIRepository.peopleList.keySet()) {
				People p = UIRepository.peopleList.get(id);
				if (p != null) {
					if (p.getTeamID().equals(t.teamID)) {
						members = new DefaultMutableTreeNode(p.getId() + ": "
								+ p.getName());
						team.add(members);
					}
				}
			}
		}
	}

	private void jTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {
		TreePath path = evt.getNewLeadSelectionPath();
		String text = path.getLastPathComponent().toString();
		String id = text.substring(0, text.indexOf(':'));
		switch (path.getPathCount()) {
		case 2:
			// team
			parent.setInfoPanel(UIRepository.teamList.get(id).toString());
			break;
		case 3:
			// people
			parent.setInfoPanel(UIRepository.peopleList.get(id).toString());

			for (People ap : UIRepository.peopleList.values())
				ap.setSelected(false);
			People people = UIRepository.peopleList.get(id);
			if (people != null) {
				people.setSelected(true);
				Location loc = people.getLocation();
				if (loc != null) {
					parent.mapPanel.resetMap(loc.lon, loc.lat);
				}
			}
			break;
		}
		UIRepository.printPeople();
	}

}
