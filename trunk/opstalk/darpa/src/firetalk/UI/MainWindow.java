package firetalk.UI;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import firetalk.db.UIRepository;
import firetalk.model.Event;
import firetalk.operators.source.Server;

import Task.Support.GUISupport.GUIUtils;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import java.awt.GridBagConstraints;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import java.awt.Font;
import java.awt.Color;
import javax.swing.plaf.metal.MetalSplitPaneUI;

import myclock.MyClock;

import com.sun.java.swing.plaf.windows.WindowsSplitPaneUI;
import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;
import java.awt.FlowLayout;
import java.awt.Cursor;
import java.io.File;
import java.io.IOException;

import javax.swing.border.EtchedBorder;
import java.awt.SystemColor;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JSplitPane jSplitPane = null;
	private JScrollPane jScrollPane = null;
	private JSplitPane jSplitPane1 = null;
	private JPanel infoPanel = null;
	private JScrollPane jScrollPane1 = null;
	public MapPanel mapPanel = null;
	private JTabbedPane jTabbedPane = null;
	private RallyPanel rallyPointPanel = null;
	private EntityPanel entityPanel = null;
	private JTextArea jTextArea = null;
	private JSplitPane jSplitPane2 = null;
	private JScrollPane jScrollPane2 = null;
	private OverallObjPanel overallObjPanel = null;
	private JSplitPane jSplitPane3 = null;
	private ActionPanel actionPanel = null;
	private JPanel jPanel = null;
	private JPanel jPanel1 = null;
	private ObjPointPanel objPointPanel = null;
	private IEDPanel IEDPanel = null;
	private JPanel jPanel2 = null;
	private OpsClock opsClock = null;
	private AudioPanel audioPanel = null;
	public UIClient network=null;
	/**
	 * This is the default constructor
	 */
	public MainWindow() {
		super();
		initialize();
		this.doInit();
		(network=new UIClient(this)).start();
		Timer timer = new Timer();
		timer.schedule(new ReadLocation(), 1000, 1000);

	}

	public void addEventResponse(String str) {
		this.actionPanel.addEvent(str);
	}

	public void addEvent2UI(Event event) {
		// make UI to reflect new event
		this.actionPanel.addEvent(event);
		this.overallObjPanel.addEvent(event);
	}

	public void selectListItem(int i) { // select list item
		this.rallyPointPanel.selectListItem(i);
	}

	public void addNewCheckPointFields(double lat, double lon) {
		switch (this.jTabbedPane.getSelectedIndex()) {
		case 1:
			this.objPointPanel.addNewCheckPointFields(lat, lon);
			break;
		case 2:
			this.rallyPointPanel.addNewCheckPointFields(lat, lon);
			break;
		case 3:
			this.IEDPanel.addNewCheckPointFields(lat, lon);
			break;
		}

	}

	public void setInfoPanel(String text) {
		this.jTextArea.setText(text);
	}

	public void updateList() {
		this.objPointPanel.updateTreeToDB();
		this.IEDPanel.updateListToDB();
	}
	public void updateRallyList(){
		this.rallyPointPanel.updateListFromDB();
	}
	public void updateObjList(){
		this.objPointPanel.updateTreeFromDB();
	}
	public void updateIEDList(){
		this.IEDPanel.updateListFromDB();
	}
	public void updateMarkers() {
		mapPanel.reloadMap();
	}

	private void doInit() {
		// GUIUtils.setAppIcon(this, "img/systemIcon.png");
		try {
			this.setIconImage(ImageIO.read(new File("img/systemIcon.png")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GUIUtils.centerOnScreen(this);
		setVisible(true);
	}

	class ReadLocation extends TimerTask {

		@Override
		public void run() {
			mapPanel.drawExtraInfo();
		}

	}

	/**
	 * This method initializes jSplitPane
	 * 
	 * @return javax.swing.JSplitPane
	 */
	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setDividerLocation(300);
			jSplitPane.setUI(new WindowsSplitPaneUI());
			jSplitPane.setLeftComponent(getJSplitPane2());
			jSplitPane.setRightComponent(getJSplitPane3());
			jSplitPane.setDividerSize(4);
		}
		return jSplitPane;
	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTabbedPane());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jSplitPane1
	 * 
	 * @return javax.swing.JSplitPane
	 */
	private JSplitPane getJSplitPane1() {
		if (jSplitPane1 == null) {
			jSplitPane1 = new JSplitPane();
			jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
			jSplitPane1.setDividerLocation(65);
			jSplitPane1.setUI(new WindowsSplitPaneUI());
			jSplitPane1.setEnabled(false);
			jSplitPane1.setTopComponent(getInfoPanel());
			jSplitPane1.setBottomComponent(getJScrollPane1());
			jSplitPane1.setDividerSize(4);
		}
		return jSplitPane1;
	}

	/**
	 * This method initializes infoPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getInfoPanel() {
		if (infoPanel == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.BOTH;
			gridBagConstraints1.weighty = 1.0;
			gridBagConstraints1.weightx = 1.0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.gridx = 0;
			infoPanel = new JPanel();
			infoPanel.setLayout(new BorderLayout());
			infoPanel.setBorder(BorderFactory
					.createEtchedBorder(EtchedBorder.RAISED));
			infoPanel.add(getAudioPanel(), BorderLayout.NORTH);
		}
		return infoPanel;
	}

	/**
	 * This method initializes jScrollPane1
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getMapPanel());
		}
		return jScrollPane1;
	}

	/**
	 * This method initializes mapPanel
	 * 
	 * @return firetalk.UI.MapPanel
	 */
	private MapPanel getMapPanel() {
		if (mapPanel == null) {
			mapPanel = new MapPanel();
			mapPanel.setBorder(BorderFactory.createTitledBorder(null, "Map",
					TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD
							| Font.ITALIC, 14), new Color(51, 51, 51)));
			mapPanel.setParent(this);
		}
		return mapPanel;
	}

	/**
	 * This method initializes jTabbedPane
	 * 
	 * @return javax.swing.JTabbedPane
	 */
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
			jTabbedPane.setUI(new WindowsTabbedPaneUI());
			jTabbedPane.addTab("Entity Infomation", null, getEntityPanel(),
					null);
			jTabbedPane.addTab("Objective Points", null, getObjPointPanel(),
					null);
			jTabbedPane
					.addTab("Rally Points", null, getRallyPointPanel(), null);
			jTabbedPane.addTab("IED", null, getIEDPanel(), null);
		}
		return jTabbedPane;
	}

	/**
	 * This method initializes rallyPointPanel
	 * 
	 * @return firetalk.UI.CheckPointPanel
	 */
	private RallyPanel getRallyPointPanel() {
		if (rallyPointPanel == null) {
			rallyPointPanel = new RallyPanel();
			rallyPointPanel.setParent(this);
		}
		return rallyPointPanel;
	}

	/**
	 * This method initializes entityPanel
	 * 
	 * @return firetalk.UI.EntityPanel
	 */
	private EntityPanel getEntityPanel() {
		if (entityPanel == null) {
			entityPanel = new EntityPanel();
			entityPanel.setParent(this);
		}
		return entityPanel;
	}

	/**
	 * This method initializes jTextArea
	 * 
	 * @return javax.swing.JTextArea
	 */
	private JTextArea getJTextArea() {
		if (jTextArea == null) {
			jTextArea = new JTextArea();
			jTextArea.setBackground(SystemColor.info);
		}
		return jTextArea;
	}

	/**
	 * This method initializes jSplitPane2
	 * 
	 * @return javax.swing.JSplitPane
	 */
	private JSplitPane getJSplitPane2() {
		if (jSplitPane2 == null) {
			jSplitPane2 = new JSplitPane();
			jSplitPane2.setUI(new WindowsSplitPaneUI());
			jSplitPane2.setDividerSize(3);
			jSplitPane2.setDividerLocation(100);
			jSplitPane2.setTopComponent(getJScrollPane2());
			jSplitPane2.setBottomComponent(getJScrollPane());
			jSplitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		}
		return jSplitPane2;
	}

	/**
	 * This method initializes jScrollPane2
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane2() {
		if (jScrollPane2 == null) {
			jScrollPane2 = new JScrollPane();
			jScrollPane2.setViewportView(getOverallObjPanel());
		}
		return jScrollPane2;
	}

	/**
	 * This method initializes overallObjPanel
	 * 
	 * @return firetalk.UI.OverallObjPanel
	 */
	private OverallObjPanel getOverallObjPanel() {
		if (overallObjPanel == null) {
			overallObjPanel = new OverallObjPanel();
			overallObjPanel.setBorder(BorderFactory.createTitledBorder(null,
					"OpsTalk Mission Progress",
					TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD
							| Font.ITALIC, 14), new Color(51, 51, 51)));
			overallObjPanel.setBackground(SystemColor.controlHighlight);
			overallObjPanel.setForeground(SystemColor.text);
		}
		return overallObjPanel;
	}

	/**
	 * This method initializes jSplitPane3
	 * 
	 * @return javax.swing.JSplitPane
	 */
	private JSplitPane getJSplitPane3() {
		if (jSplitPane3 == null) {
			jSplitPane3 = new JSplitPane();
			jSplitPane3.setDividerLocation(650);
			jSplitPane3.setUI(new WindowsSplitPaneUI());
			jSplitPane3.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			jSplitPane3.setDividerSize(4);
			jSplitPane3.setRightComponent(getJPanel1());
			jSplitPane3.setLeftComponent(getJSplitPane1());
		}
		return jSplitPane3;
	}

	/**
	 * This method initializes actionPanel
	 * 
	 * @return firetalk.UI.ActionPanel
	 */
	private ActionPanel getActionPanel() {
		if (actionPanel == null) {
			actionPanel = new ActionPanel();
			actionPanel.setParent(this);
			actionPanel.setBorder(BorderFactory.createTitledBorder(null,
					"Event/Response", TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD
							| Font.ITALIC, 14), new Color(51, 51, 51)));
		}
		return actionPanel;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.weightx = 1.0;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.setBorder(BorderFactory.createTitledBorder(null,
					"Information", TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD
							| Font.ITALIC, 14), new Color(51, 51, 51)));
			jPanel.setPreferredSize(new Dimension(10, 200));
			jPanel.add(getJTextArea(), gridBagConstraints);
		}
		return jPanel;
	}

	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.setLayout(new BorderLayout());
			jPanel1.add(getActionPanel(), BorderLayout.CENTER);
			jPanel1.add(getJPanel2(), BorderLayout.NORTH);
			jPanel1.add(getJPanel(), BorderLayout.SOUTH);
		}
		return jPanel1;
	}

	/**
	 * This method initializes objPointPanel
	 * 
	 * @return firetalk.UI.ObjPointPanel
	 */
	private ObjPointPanel getObjPointPanel() {
		if (objPointPanel == null) {
			objPointPanel = new ObjPointPanel();
			objPointPanel.setParent(this);
		}
		return objPointPanel;
	}

	/**
	 * This method initializes IEDPanel
	 * 
	 * @return firetalk.UI.IEDPanel
	 */
	private IEDPanel getIEDPanel() {
		if (IEDPanel == null) {
			IEDPanel = new IEDPanel();
			IEDPanel.setParent(this);
		}
		return IEDPanel;
	}

	/**
	 * This method initializes jPanel2
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jPanel2 = new JPanel();
			jPanel2.setLayout(new FlowLayout());
			jPanel2.setBackground(Color.white);
			jPanel2.add(getOpsClock(), null);
		}
		return jPanel2;
	}

	/**
	 * This method initializes opsClock
	 * 
	 * @return firetalk.UI.OpsClock
	 */
	private OpsClock getOpsClock() {
		if (opsClock == null) {
			opsClock = new OpsClock();
		}
		return opsClock;
	}

	/**
	 * This method initializes audioPanel
	 * 
	 * @return firetalk.UI.AudioPanel
	 */
	private AudioPanel getAudioPanel() {
		if (audioPanel == null) {
			audioPanel = new AudioPanel();
			audioPanel.setBorder(BorderFactory.createTitledBorder(null,
					"Speech Monitor", TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, new Font("Dialog",
							Font.BOLD, 12), new Color(51, 51, 51)));
		}
		return audioPanel;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		UIRepository.init(); // initialize database
		System.out.println(Long.MAX_VALUE);
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MainWindow thisClass = new MainWindow();
				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				thisClass.setVisible(true);
			}
		});
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(1075, 648);
		this.setContentPane(getJContentPane());
		this.setTitle("OpsTalk");
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJSplitPane(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	public void updateCheckPoints() {
		this.updateMarkers();
		this.updateList();
		
	}

} // @jve:decl-index=0:visual-constraint="28,10"
