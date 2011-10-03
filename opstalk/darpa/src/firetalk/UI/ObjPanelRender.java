package firetalk.UI;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;

import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.tree.TreeCellRenderer;

import firetalk.db.Repository;
import firetalk.model.CheckPoint;

import java.awt.SystemColor;

public class ObjPanelRender extends JPanel implements TreeCellRenderer {

	private static final long serialVersionUID = 1L;
	private JTextField jTextField = null;
	private JLabel jLabel = null;

	/**
	 * This is the default constructor
	 */
	public ObjPanelRender() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		jLabel = new JLabel();
		jLabel.setText("JLabel");
		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		this.setLayout(flowLayout);
		this.setSize(300, 200);
		this.add(getJTextField(), null);
		this.add(jLabel, null);
	}

	/**
	 * This method initializes jTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setText("  ");
			jTextField.setBackground(Color.yellow);
		}
		return jTextField;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		setEnabled(tree.isEnabled());
		this.setBackground(tree.getBackground());
		if (selected)
			this.setBackground(Color.gray);
		setFont(tree.getFont());

		String line = value.toString();
		String type = null;
		boolean reach = false;
		if (!line.equals("Objectives")) {
			int spacePos = line.indexOf(' ');
			int comPos = line.indexOf(':');
			type = line.substring(0, spacePos);
			String id = line.substring(spacePos + 1, comPos);
			reach = true;
			if (type.equals("objPoint")) {
				for (CheckPoint cp : Repository.checkPoints) {
					if (cp.userID.equals(id) && !cp.isReached()) {
						reach = false;
						break;
					}
				}
			} else {
				for (CheckPoint cp : Repository.checkPoints) {
					if (cp.id.equals(id)) {
						reach = cp.isReached();
						break;
					}
				}
			}
		}
		if (reach) {
			jTextField.setBackground(Color.yellow);
			// jLabel.setForeground(Color.green);
		} else {
			if (type != null) {
				jTextField.setBackground(Color.green);
//				if (type.equals("objPoint"))
//					jTextField.setBackground(Color.ORANGE);
//				else
//					jTextField.setBackground(Color.yellow);
			}
			else
				jTextField.setBackground(Color.gray);
			// jLabel.setForeground(Color.yellow);
		}

		jLabel.setText(value.toString());

		// setText(value.toString());
		return this;
	}

}
