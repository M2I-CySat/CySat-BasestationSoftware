package gui;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class RadioTestGUI {
	private static JFrame frame;
	private static final int FRAME_WIDTH = 1024;
	private static final int FRAME_HEIGHT = 600;

	private static void createAndShowGUI() {
		frame = new JFrame("Radio Test GUI");
		frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((scrSize.width - FRAME_WIDTH) / 2, (scrSize.height - FRAME_HEIGHT) / 2);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		RadioTestGUIPanel testGUIPanel = new RadioTestGUIPanel();
		frame.add(testGUIPanel);

		frame.setVisible(true);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
