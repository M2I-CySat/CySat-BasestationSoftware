package gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.xml.ws.Response;

import jssc.SerialPortException;
import serial.client.SerialBufferedDataListener;
import serial.client.SerialClient;
import serial.client.SerialLocalClient;
import api.OSBoard;

@SuppressWarnings("serial")
public class CySatDataTab extends JPanel {
	private SerialClient client;
	private OSBoard os;
	
	public CySatDataTab(final CySatGUI gui) {
		setBackground(new Color(40, 40, 40));
		setFocusable(false);
		
		JButton btnPowpanelX = new JButton("Power Panel X");
		btnPowpanelX.setFocusable(false);
		btnPowpanelX.addActionListener(new ActionListener() {
			boolean done = false;
			
			public void actionPerformed(ActionEvent e) {
				ensureOSBoardInitialized();
				if (os != null) {
					String response = client.writeAndWaitForResponse("!COMMAND,CREAD,A0$");
					while (response != null) {
						System.out.println("RESPONSE: " + response);
						response = client.waitForResponse();
					}
				}
//				ensureOSBoardInitialized();
//				if (os != null) {
//					SerialBufferedDataListener l = new SerialBufferedDataListener() {
//						@Override
//						public void serialBufferedDataReceived(String data) {
//							System.out.println("DATA : " + data);
//							done = true;
//						}
//					};
//					client.addListener(l);
//					try {
//						client.write("!QUERY,POW_PANEL,X,A0$");
//					} catch (Exception e1) {
//						e1.printStackTrace();
//					}
//					
//					while (!done) { 
//						try {
//							Thread.sleep(100);
//						} catch (Exception e1) {
//							e1.printStackTrace();
//						}
//					}
//					client.removeListener(l);
//				}
			}
		});
		
		JTextField textField = new JTextField();
		textField.setColumns(10);
		
		JButton btnPowpanelY = new JButton("Power Panel Y");
		btnPowpanelY.setFocusable(false);
		btnPowpanelY.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ensureOSBoardInitialized();
				if (os != null) {
					OSBoard.PowPanelInfo powPanelYInfo = os.getPowPanelInfo(OSBoard.PowPanelAxis.Y);
					System.out.println(powPanelYInfo);
				}
			}
		});
		
		JTextField textField_1 = new JTextField();
		textField_1.setColumns(10);
		
		JButton btnPowpanelZ = new JButton("Power Panel Z");
		btnPowpanelZ.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ensureOSBoardInitialized();
				if (os != null) {
					String response = client.writeAndWaitForResponse("!COMMAND,ZERO,A0$");
					while (response != null) {
						System.out.println("RESPONSE: " + response);
						response = client.waitForResponse();
					}
				}
			}
		});
		btnPowpanelZ.setFocusable(false);
		
		JTextField textField_2 = new JTextField();
		textField_2.setColumns(10);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(114)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(btnPowpanelZ)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(textField_2, GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(btnPowpanelY)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(textField_1, GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(btnPowpanelX)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(textField, GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)))
					.addGap(111))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(43)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnPowpanelX)
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(18)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(btnPowpanelY)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(1)
							.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addGap(18)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(1)
							.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(btnPowpanelZ))
					.addContainerGap(152, Short.MAX_VALUE))
		);
		setLayout(groupLayout);

//		JButton button = new JButton("Push me!");
//		button.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				gui.getNextPassesPanel().getNextPassesView().removePass(0);
//				gui.getNextPassesPanel().getNextPassesView().refresh();
//				gui.getFrame().repaint();
//			}
//		});
//		button.setFocusable(false);
//
//		JButton button2 = new JButton("No, push ME!");
//		button2.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				gui.getNextPassesPanel().getNextPassesView().showNextPasses("ISS (ZARYA)", 1);
//				gui.getFrame().repaint();
//			}
//		});
//		button2.setFocusable(false);
//
//		add(button);
//		add(button2);
	}
	
	private void ensureOSBoardInitialized() {
		if (client == null) {
			try {
				client = new SerialLocalClient("COM5", 9600, "$\n");
			} catch (SerialPortException e) {
				e.printStackTrace();
			}
		}
		
		if (client != null) {
			if (os == null) {
				os = new OSBoard(client);
			}
		}
	}
}
