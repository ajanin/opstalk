package firetalk.UI;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import firetalk.db.UIRepository;
import firetalk.model.Enemy;
import firetalk.model.IEDPoint;

public class EnemyRenderer extends JPanel implements ListCellRenderer {

	private static final long serialVersionUID = 1L;
	private JTextField jTextField = null;
	private JLabel jLabel = null;

	/**
	 * This is the default constructor
	 */
	public EnemyRenderer() {
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

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean hasFocus) {
		setEnabled(list.isEnabled());
		this.setBackground(list.getBackground());
		if (isSelected)
			this.setBackground(Color.gray);
		setFont(list.getFont());
		Enemy cp = UIRepository.enemyList.get(index);
		jTextField.setBackground(Color.red);

		jLabel.setText("<" + cp.getLatitude() + ","
				+ cp.getLongitude() + ">");
		// setText(value.toString());
		return this;
	}

	/**
	 * This method initializes jTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setText("   ");
			jTextField.setBackground(Color.yellow);
		}
		return jTextField;
	}

}
