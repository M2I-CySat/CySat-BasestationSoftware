package gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import api.TS2000Radio;
import serial.client.SerialTCPClient;

@SuppressWarnings("serial")
public class RadioTestGUIPanel extends JPanel {
	private JTextField textField;
	private JButton enterButton;
	private JTextArea logField;
	private SerialTCPClient client;
	private TS2000Radio radio;

	@SuppressWarnings("unused")
	private boolean listening = true;

	public RadioTestGUIPanel() {
		client = new SerialTCPClient("10.24.223.192", 2809, "joe", "password23", 0);
		if (client.getState() != SerialTCPClient.State.ALIVE) {
			System.err.println("Error: Couldn't connect to serial server! Aborting...");
			return;
		}
		radio = new TS2000Radio(client);

		textField = new JTextField(50);
		enterButton = new JButton("Submit");
		enterButton.setFocusable(false);
		enterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = textField.getText();

				// Replace all "\n" occurences in the text with actual newline characters
				text = text.replaceAll("\\\\n", "\n");

				// Same with "\r"
				text = text.replaceAll("\\\\r", "\r");

				System.out.println("Text: " + text + " (" + Arrays.toString(text.getBytes()) + ")");
				try {
					client.write(text);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				textField.setText("");
			}
		});

		logField = new JTextArea(30, 80);
		logField.setEditable(false);

		// new Thread(new Runnable(){
		// @Override
		// public synchronized void run(){
		// while(true){
		// if(listening){
		// while(client.hasData()){
		// logField.append(client.getData());
		// }
		// }
		//
		// try{
		// Thread.sleep(100);
		// } catch(InterruptedException e){
		// e.printStackTrace();
		// }
		// }
		// }
		// }).start();

		add(textField);
		add(enterButton);
		add(logField);

		JButton freqA = new JButton("Get Frequency A");
		freqA.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				listening = false;
				int freq = -1;
				try {
					freq = radio.RadioGetFreqA();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (freq > 0)
					logField.append("Frequency A: " + freq + "\n");
				else
					logField.append("Error reading frequency A...\n");
				listening = true;
			}
		});

		JButton freqB = new JButton("Get Frequency B");
		freqB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				listening = false;
				int freq = -1;
				try {
					freq = radio.RadioGetFreqB();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (freq > 0)
					logField.append("Frequency B: " + freq + "\n");
				else
					logField.append("Error reading frequency B...\n");
				listening = true;
			}
		});

		JButton freqC = new JButton("Get Frequency C");
		freqC.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				listening = false;
				int freq = -1;
				try {
					freq = radio.RadioGetFreqSub();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (freq > 0)
					logField.append("Frequency C: " + freq + "\n");
				else
					logField.append("Error reading frequency C...\n");
				listening = true;
			}
		});

		add(freqA);
		add(freqB);
		add(freqC);

		setBackground(Color.DARK_GRAY);
	}

	// @Override
	// public void paintComponent(Graphics g){
	//
	// }
}
