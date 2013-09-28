package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import util.SatelliteUtils;

public class CySatGUI {
	static {
		//Disable the generation of light-weight popups so that they don't 
		//get hidden behind the WorldWindowGLCanvas
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	}
	
	public static void main(String[] args){
		new CySatGUI().start();
	}

	private JFrame frame;
	private NextPassesPanel nextPassesPanel;
	private CurrentStatePanel currentStatePanel;
	private CySatMainPanel cySatMainPanel;

	private final String WINDOW_TITLE = "CySat Base Station GUI";
	private final String APPLICATION_ICON_FILE = "app-icon.png";
	private final String APPLICATION_ICON_LARGE_FILE = "app-icon-large.png";
	private final int INITIAL_FRAME_WIDTH = 800;
	private final int INITIAL_FRAME_HEIGHT = 600;
	private final int MINIMUM_FRAME_WIDTH = 640;
	private final int MINIMUM_FRAME_HEIGHT = 480;

	private String operatingSystem;
	
	private CySatGUI() {

	}

	private void start(){
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run(){
				createAndShowGUI();
			}
		});
	}

	private void createAndShowGUI(){
		try{
			SatelliteUtils.init();
		} catch(Exception e){
			e.printStackTrace();
			System.err.println("Unable to load satellites information! Exiting...");
			System.exit(1);
		}
		
		operatingSystem = System.getProperty("os.name").toLowerCase();
		
		frame = new JFrame(WINDOW_TITLE);
		frame.setSize(INITIAL_FRAME_WIDTH, INITIAL_FRAME_HEIGHT);
		frame.setMinimumSize(new Dimension(MINIMUM_FRAME_WIDTH, MINIMUM_FRAME_HEIGHT));
		Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((int) (scrSize.getWidth() - frame.getWidth()) / 2,
				(int) (scrSize.getHeight() - frame.getHeight()) / 2);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		initializePanels();
		addComponentsToPane(frame.getContentPane());

		String appIconFile = "";
		if(operatingSystem.contains("linux")){
			appIconFile = APPLICATION_ICON_LARGE_FILE;
		} else{
			appIconFile = APPLICATION_ICON_FILE;
		}
		
		try{
			frame.setIconImage(ImageIO.read(new File(appIconFile)));
		} catch(IOException e){
			System.err.println("Unable to find application image icon <" + appIconFile + ">! Using Java default.");
		}

		frame.setVisible(true);
	}

	private void initializePanels(){
		nextPassesPanel = new NextPassesPanel();
		nextPassesPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		nextPassesPanel.setMinimumSize(new Dimension(350, 100));

		currentStatePanel = new CurrentStatePanel();
		currentStatePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		currentStatePanel.setPreferredSize(new Dimension(0, 150));
		currentStatePanel.setMinimumSize(new Dimension(0, 150));

		cySatMainPanel = new CySatMainPanel(this);
		cySatMainPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		cySatMainPanel.setPreferredSize(new Dimension(0, 400));
	}

	private void addComponentsToPane(Container pane){
		pane.setLayout(new BorderLayout());
		
		JSplitPane horzSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, nextPassesPanel, cySatMainPanel);
		horzSplitPane.setMinimumSize(new Dimension(0, 200));
		horzSplitPane.setResizeWeight(0);
		horzSplitPane.setPreferredSize(new Dimension(0, cySatMainPanel.getPreferredSize().height));

		JSplitPane vertSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, horzSplitPane, currentStatePanel);
		vertSplitPane.setResizeWeight(1);
		pane.add(vertSplitPane, BorderLayout.CENTER);
	}
	
	public NextPassesPanel getNextPassesPanel(){
		return nextPassesPanel;
	}
	
	public CySatMainPanel getMainPanel(){
		return cySatMainPanel;
	}
	
	public CurrentStatePanel getCurrentStatePanel(){
		return currentStatePanel;
	}
	
	public JFrame getFrame(){
		return frame;
	}
}
