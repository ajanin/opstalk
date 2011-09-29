package firetalk.UI;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.io.File;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JSlider;

import firetalk.db.Repository;
import firetalk.operators.speech.PlaySound;

public class AudioPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JComboBox jComboBox = null;
	private JButton jButton = null;
	private JSlider jSlider = null;
	private PlaySound player = null;

	/**
	 * This is the default constructor
	 */
	public AudioPanel() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 200);
		this.setLayout(new FlowLayout());
		this.add(getJComboBox(), null);
		this.add(getJButton(), null);
		this.add(getJSlider(), null);
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBox() {
		if (jComboBox == null) {
			jComboBox = new JComboBox();
			DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
			jComboBox.setModel(comboModel);
			jComboBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					updateSlider();
				}
			});
			for (String id : Repository.peopleList.keySet()) {
				comboModel.addElement(id + ": "
						+ Repository.peopleList.get(id).getName());
			}

		}
		return jComboBox;
	}

	protected void updateSlider() {
		if (this.jSlider != null && this.jComboBox != null) {
			String select = (String) jComboBox.getSelectedItem();
			String id = select.substring(0, select.indexOf(":"));
			if (player != null && player.isPlaying())
				player.stopPlay();
			player = new PlaySound(id, 0,this.jSlider);
			jButton.setText(">");
			Vector<File> files = Repository.audioFiles.get(id);
			if (files != null && files.size() != 0) {
				this.jSlider.setMaximum(files.size());
				this.jSlider.setValue(0);
				this.jSlider.setEnabled(true);
			} else
				this.jSlider.setEnabled(false);
		}
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText(">");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (jButton.getText().equals("||")) {
						// stop
						if (player != null && player.isPlaying()) {
							player.stopPlay();
							jButton.setText(">");
						}
					} else {
						// start
						if (player != null && !player.isPlaying()) {
							player=new PlaySound(player.getUserId(),player.getInd(),jSlider);
							player.start();
							jButton.setText("||");
						}
					}

				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jSlider
	 * 
	 * @return javax.swing.JSlider
	 */
	private JSlider getJSlider() {
		if (jSlider == null) {
			jSlider = new JSlider();
			jSlider.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					int ind = jSlider.getValue();
					if (player != null)
						player.setInd(ind);
				}
			});
			this.updateSlider();
		}
		return jSlider;
	}

}
