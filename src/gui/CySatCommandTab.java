package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import serial.client.SerialClient;
import serial.client.SerialBufferedDataListener;
import api.OSBoard;

public class CySatCommandTab extends JPanel {
	private static final long serialVersionUID = 0L;

	// private CySatGUI gui;

	public CySatCommandTab(CySatGUI gui) {
		// this.gui = gui;

		initComponents();
	}

	private void initComponents() {
		SerialClient client = new SerialClient("10.24.223.109", 2809, "joe", "password23", 0);
		final OSBoard os = new OSBoard(client);

		JButton hello = new JButton("Send 'Hello'");
		hello.setFocusable(false);
		add(hello);

		final JTextArea response = new JTextArea(5, 30);
		client.addListener(new SerialBufferedDataListener() {
			@Override
			public void serialBufferedDataReceived(String data) {
				response.append(data + "\n");
			}
		});

		response.setText("Testing...\n\n");
		hello.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				os.sendHello();
			}
		});
		JScrollPane scrollPane = new JScrollPane(response, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane);
	}
}
