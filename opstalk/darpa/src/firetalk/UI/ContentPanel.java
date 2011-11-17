package firetalk.UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

public class ContentPanel extends JPanel{

	private static final long serialVersionUID = 1L;
	private JPanel jPanel = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JLabel jLabel3 = null;
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jLabel2 = new JLabel();
			jLabel2.setText(new Date().toString());
			jLabel1 = new JLabel();
			jLabel1.setText("Jeffrey");
			jLabel = new JLabel();
			jLabel.setText("level 1");
			jLabel.setPreferredSize(new Dimension(28, 28));
			jLabel.setFont(new Font("Dialog", Font.BOLD, 12));
			jLabel.setForeground(Color.red);
			jLabel.setBackground(Color.red);
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(1);
			gridLayout.setColumns(3);
			jPanel = new JPanel();
			jPanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
			jPanel.setLayout(gridLayout);
			jPanel.setPreferredSize(new Dimension(0, 28));
			jPanel.add(jLabel, null);
			jPanel.add(jLabel1, null);
			jPanel.add(jLabel2, null);
		}
		return jPanel;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	/**
	 * This is the default constructor
	 */
	public ContentPanel() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		jLabel3 = new JLabel();
		jLabel3.setText("This is a speech sample");
		jLabel3.setPreferredSize(new Dimension(0, 0));
		this.setSize(417, 200);
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		this.setPreferredSize(new Dimension(0, 0));
		this.add(getJPanel(), BorderLayout.NORTH);
		this.add(jLabel3, BorderLayout.CENTER);
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
