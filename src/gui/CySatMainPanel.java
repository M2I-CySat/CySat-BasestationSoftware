package gui;

import java.awt.event.KeyEvent;

import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class CySatMainPanel extends JTabbedPane {
	private CySatMapTab mapTab;
	private CySatDataTab dataTab;
	
	public CySatMainPanel(CySatGUI gui){
		mapTab = new CySatMapTab(gui);
		dataTab = new CySatDataTab(gui);
		
		addTab("Map View", mapTab);
		setMnemonicAt(0, KeyEvent.VK_1);
		
		addTab("Data Terminal View", dataTab);
		setMnemonicAt(1, KeyEvent.VK_2);
		
		setFocusable(false);
	}
}
