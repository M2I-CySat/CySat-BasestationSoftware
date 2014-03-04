package gui;

import java.awt.event.KeyEvent;

import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class CySatMainPanel extends JTabbedPane {
	private CySatMapTab mapTab;
	private CySatDataTab dataTab;
	private CySatCommandTab cmdTab;

	public CySatMainPanel(CySatGUI gui) {
		mapTab = new CySatMapTab(gui);
		dataTab = new CySatDataTab(gui);
		cmdTab = new CySatCommandTab(gui);

		addTab("Map View", mapTab);
		setMnemonicAt(0, KeyEvent.VK_1);

		//addTab("Data Terminal View", dataTab);
		addTab("EPS", dataTab);
		setMnemonicAt(1, KeyEvent.VK_2);

		addTab("Command Tab", cmdTab);
		setMnemonicAt(2, KeyEvent.VK_3);

		setFocusable(false);
	}
}
