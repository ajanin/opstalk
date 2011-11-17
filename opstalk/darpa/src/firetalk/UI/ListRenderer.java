package firetalk.UI;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

public class ListRenderer extends JPanel implements ListCellRenderer {

	private static final long serialVersionUID = 1L;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;

	/**
	 * This is the default constructor
	 */
	public ListRenderer() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		jLabel1 = new JLabel();
		jLabel1.setText("JLabel");
		jLabel = new JLabel();
		jLabel.setText("");
		jLabel.setIcon(new ImageIcon("img/list.jpg"));
		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		this.setLayout(flowLayout);
		this.setSize(300, 200);
		this.add(jLabel, null);
		this.add(jLabel1, null);
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean hasFocus) {
		setEnabled(list.isEnabled());
		this.setBackground(list.getBackground());
		if (isSelected)
			this.setBackground(Color.gray);
		setFont(list.getFont());
		this.jLabel1.setText(value.toString());
		return this;
	}


}
