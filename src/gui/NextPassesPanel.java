package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class NextPassesPanel extends JPanel {
	private NextPassesView nextPassesView;
	private LabelText panelTitle;
	
	public NextPassesPanel(){
		super();
		setLayout(new BorderLayout());
		
		
		nextPassesView = new NextPassesView();
//		String satName = "ISS (ZARYA)";
//		nextPassesView.showNextPasses(satName, 5);
		
		panelTitle = new LabelText("- No Satellite Chosen -");
		panelTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
		panelTitle.setBackground(Color.LIGHT_GRAY);
		panelTitle.setForeground(Color.BLACK);
		
		add(nextPassesView, BorderLayout.CENTER);
		add(panelTitle, BorderLayout.NORTH);
	}
	
	public NextPassesView getNextPassesView(){
		return nextPassesView;
	}
	
	public void setSatTitle(String satName) {
		panelTitle.setText("- Next Passes - " + satName + " -");
	}
}
