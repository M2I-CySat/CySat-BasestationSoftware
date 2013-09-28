package worldwind;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class WorldWindTest2 {
	private static final int FRAME_WIDTH = 1024;
	private static final int FRAME_HEIGHT = 600;
	
	private JFrame frame;
	
	public static void main(String[] args){
		new WorldWindTest();
	}
	
	public WorldWindTest2(){
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run(){
				createAndShowGUI();
			}
		});
	}
	
	private void createAndShowGUI(){
		frame = new JFrame("WorldWind Test");
		frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);

		Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((scrSize.width - frame.getWidth()) / 2, (scrSize.height - frame.getHeight()) / 2);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		//create a WorldWind main object
		WorldWindowGLCanvas worldWindCanvas = new WorldWindowGLCanvas();
		worldWindCanvas.setModel(new BasicModel());
	
		frame.add(worldWindCanvas);
		
	}
}
