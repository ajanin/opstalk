package firetalk.UI;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import firetalk.db.UIRepository;
import firetalk.model.People;
import firetalk.util.DataUtil;

public class EntityRenderer extends JPanel implements TreeCellRenderer {

	private static final long serialVersionUID = 1L;
	private JLabel jLabel = null;
	private JLabel iconLabel = null;

	/**
	 * This is the default constructor
	 */
	public EntityRenderer() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		iconLabel = new JLabel();
		iconLabel.setText("");
		jLabel = new JLabel();
		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		this.setLayout(flowLayout);
		this.setSize(300, 200);
		this.add(iconLabel, null);
		this.add(jLabel, null);
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

		String text = value.toString();
		if (text.indexOf(':') != -1&&text.length() > 10) {
				String id = text.substring(0, text.indexOf(':'));

				People people = UIRepository.peopleList.get(id);
				if (people != null) {
					if (people.getLevel().equals("1"))
						iconLabel.setIcon(new ImageIcon("img/squadLead.jpg"));

					if (people.getLevel().equals("2"))
						iconLabel.setIcon(new ImageIcon("img/teamLead.jpg"));

					if (people.getLevel().equals("3"))
						iconLabel.setIcon(new ImageIcon("img/soldier.jpg"));

				}	
		}
		else{
			try {
				BufferedImage img=ImageIO.read(new File("img/fold.jpg"));
				img=DataUtil.resizeImage(img, 15, 15);
				iconLabel.setIcon(new ImageIcon(img));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		// iconLabel.setIcon(new ImageIcon("list.png"));
		jLabel.setText(value.toString());
		// setText(value.toString());
		return this;
	}

}
