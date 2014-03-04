package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import serial.client.SerialLocalClient;
import api.OSBoard;

public class CySatCommandTab extends JPanel {
	private static final long serialVersionUID = 0L;

	// private CySatGUI gui;

	public CySatCommandTab(CySatGUI gui) {
		// this.gui = gui;

		initComponents();
	}

	private void initComponents() {
		//SerialTCPClient client = new SerialTCPClient("10.24.223.109", 2809, "joe", "password23", 0);
		SerialLocalClient client = null;
//		try {
//			client = new SerialLocalClient("COM5", 9600, "\r\n$");
//		} catch (SerialPortException e1) {
//			e1.printStackTrace();
//		}
		final OSBoard os = new OSBoard(client);

		JButton hello = new JButton("Send 'Hello'");
		hello.setFocusable(false);
		add(hello);

		final JTextArea response = new JTextArea(20, 30);
		DefaultCaret caret = (DefaultCaret)response.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
//		if (client != null && client.getState() == State.ALIVE) {
//			client.addListener(new SerialBufferedDataListener() {
//				@Override
//				public void serialBufferedDataReceived(String data) {
//					response.append(data + "\n");
//				}
//			});
//		}

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
