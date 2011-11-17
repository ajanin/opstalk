package firetalk.UI;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.SoftBevelBorder;

public class ImageFrame extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel jLabel = null;
	private JTextPane jTextPane = null;
	/**
	 * This is the default constructor
	 */
	public ImageFrame() {
		super();
		initialize();
	}
	public void setImage(String file){
		jLabel.setIcon(new ImageIcon(file));
	}
	public void setTag(String tag){
		jTextPane.setText(tag);
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		jLabel = new JLabel();
		this.setSize(300, 200);
		this.setLayout(new BorderLayout());
		this.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
		this.add(jLabel, BorderLayout.CENTER);
		this.add(getJTextPane(), BorderLayout.SOUTH);
	}

	/**
	 * This method initializes jTextPane	
	 * 	
	 * @return javax.swing.JTextPane	
	 */
	private JTextPane getJTextPane() {
		if (jTextPane == null) {
			jTextPane = new JTextPane();
			jTextPane.setText("Tags: this is an example of image tagging");
		}
		return jTextPane;
	}

}
