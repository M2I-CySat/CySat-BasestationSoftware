package api;

import jssc.SerialPortException;
import serial.client.SerialBufferedDataListener;
import serial.client.SerialClient;
import serial.client.SerialLocalClient;
import serial.client.SerialTCPClient;

/**
 * An API for the operating system board that handles C&DH stuff on the satellite
 * 
 * @author Adam Campbell
 */
public class OSBoard {
	public class PowPanelInfo {
		public final PowPanelAxis axis;
		public final double voltage;
		public final double minusCurrent;
		public final double plusCurrent;
		
		public PowPanelInfo(PowPanelAxis axis, double voltage, double minusCurrent, double plusCurrent) {
			this.axis = axis;
			this.voltage = voltage;
			this.minusCurrent = minusCurrent;
			this.plusCurrent = plusCurrent;
		}
		
		@Override
		public String toString() {
			return String.format("[%s] Axis: %s, Voltage: %0.2f, -Current: %0.2f, +Current: %0.2f", 
								getClass().getName(), axis.name(), voltage, minusCurrent, plusCurrent);
		}
	}
	
	public enum PowPanelAxis {
		X, Y, Z
	}
	
	/**
	 * The SerialClient used to talk to the os via the serial port
	 */
	private SerialClient client;

	/**
	 * Construct an OSBoard API wrapper
	 * 
	 * @param client
	 *            Serial client for the os
	 */
	public OSBoard(SerialClient client) {
		this.client = client;
	}

	/**
	 * Send the hello world query
	 */
	public void sendHello() {
		String helloQuery = "!QUERY,HELLO,A0$";
		try {
			client.write(String.format("%s", helloQuery));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public PowPanelInfo getPowPanelInfo(PowPanelAxis panel) {
		String response = client.writeAndWaitForResponse("!QUERY,POW_PANEL," + panel.name() + ",A0$");
		response = client.waitForResponse();
		if (response != null) {
			String[] fields = response.split(",");
			if (fields.length != 7) {
				return null;
			}
			
			try {
				PowPanelAxis axis = PowPanelAxis.valueOf(fields[1]);
				double voltage = Double.parseDouble(fields[2]);
				double minusCurrent = Double.parseDouble(fields[3]);
				double plusCurrent = Double.parseDouble(fields[4]);
				return new PowPanelInfo(axis, voltage, minusCurrent, plusCurrent);
			} catch (NumberFormatException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	public static void main(String[] args) {
		boolean useTCP = false;
		
		SerialClient client = null;
		if (useTCP) {
			client = new SerialTCPClient("10.24.223.109", 2809, "joe", "password23", 0);
		} else {
			try {
				client = new SerialLocalClient("COM5", 9600, "\r\n$");
			} catch (SerialPortException e) {
				e.printStackTrace();
			}
		}
		
		
		// Test hello world working
		if (client.getState() == SerialClient.State.ALIVE) {
			OSBoard os = new OSBoard(client);
			client.addListener(new SerialBufferedDataListener() {
				@Override
				public void serialBufferedDataReceived(String data) {
					System.out.println("DATA RECEIVED: " + data);
				}
			});
			os.sendHello();
			
			while(true) {
				try {
					Thread.sleep(5000);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}

		System.exit(0);
	}
}
