package api;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;

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
			client.write(String.format("%s\n", helloQuery));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		boolean useTCP = false;
		
		SerialClient client = null;
		if (useTCP) {
			client = new SerialTCPClient("10.24.223.109", 2809, "joe", "password23", 0);
		} else {
			try {
				client = new SerialLocalClient("COM17", 9600);
			} catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException | IOException e) {
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
			
			try {
				Thread.sleep(5000);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		System.exit(0);
	}
}
