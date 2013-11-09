package gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class CySatDataTab extends JPanel {
	public CySatDataTab(final CySatGUI gui){
		setBackground(Color.GREEN);
		setFocusable(false);
		

		JButton button = new JButton("Push me!");
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				gui.getNextPassesPanel().getNextPassesView().removePass(0);
				gui.getNextPassesPanel().getNextPassesView().refresh();
				gui.getFrame().repaint();
			}
		});
		button.setFocusable(false);
		
		JButton button2 = new JButton("No, push ME!");
		button2.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				gui.getNextPassesPanel().getNextPassesView().showNextPasses("ISS (ZARYA)", 1);
				gui.getFrame().repaint();
			}
		});
		button2.setFocusable(false);
		
		add(button);
		add(button2);
	}
}
