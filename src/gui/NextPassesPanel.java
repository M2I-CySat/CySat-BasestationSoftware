package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class NextPassesPanel extends JPanel {
	private NextPassesView nextPassesView;
	
	public NextPassesPanel(){
		super();
		setLayout(new BorderLayout());
		
		String satName = "ISS (ZARYA)";
		nextPassesView = new NextPassesView();
		nextPassesView.showNextPasses(satName, 10);
		
		LabelText panelTitle = new LabelText("Next Passes - " + satName);
		panelTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
		panelTitle.setBackground(Color.LIGHT_GRAY);
		panelTitle.setForeground(Color.BLACK);
		
		add(nextPassesView, BorderLayout.CENTER);
		add(panelTitle, BorderLayout.NORTH);
	}
	
	public NextPassesView getNextPassesView(){
		return nextPassesView;
	}
}
