package gui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class CurrentStatePanel extends JPanel {
	public static JTextField up2 = new JTextField(18);
	public static JTextField status2 = new JTextField(10);

	public CurrentStatePanel() {
		setBackground(new Color(30, 30, 30));
		up2.setBackground(null);
		up2.setHorizontalAlignment(JTextField.HORIZONTAL);
		up2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
		up2.setForeground(Color.WHITE);
		up2.setEditable(false);
		up2.setFocusable(false);
		status2.setBackground(null);
		status2.setHorizontalAlignment(JTextField.HORIZONTAL);
		status2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
		status2.setForeground(Color.WHITE);
		status2.setEditable(false);
		status2.setFocusable(false);
		add(status2);
		add(up2);
	}
}
