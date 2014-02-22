package api;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;

import serial.client.SerialClient;
import serial.client.SerialDataListener;

/**
 * An API for communicating with the TS-2000 Radio
 * 
 * @author Adam Campbell
 */
public class TS2000Radio {
	/**
	 * The timeout time in milliseconds for radio communication
	 */
	public static final int TIMEOUT_TIME = 5000;

	/**
	 * The mode for sending and receiving packets
	 */
	public static final int PACKET_MODE = 1;

	/**
	 * The mode for updating and polling the status of the server, like frequency settings, etc.
	 */
	public static final int STATUS_MODE = 2;

	/**
	 * The current mode of the radio
	 */
	private int currentMode = 0;

	/**
	 * The SerialClient used to talk to the radio via the serial port
	 */
	private SerialClient client;

	/**
	 * Construct a new radio object to talk to the given client
	 * 
	 * @param client
	 *            The SerialClient used to talk to the radio via the serial port
	 */
	public TS2000Radio(SerialClient client) {
		this.client = client;
		setMode(STATUS_MODE);
	}

	/**
	 * Set the frequency on VFO A
	 * 
	 * @param frequency
	 *            The frequency to set
	 * @throws IOException
	 *             If a read/write error occurs
	 */
	public void RadioSetFreqA(int frequency) throws IOException {
		if (currentMode != STATUS_MODE)
			throw new IllegalStateException("Radio must be in the status mode to deal with frequency!");

		if (frequency < 0) {
			throw new IllegalArgumentException("Invalid frequency: " + frequency);
		}

		String cmd = String.format("%dFA%011d;", client.getSerialPortNum(), frequency);
		client.write(cmd);
	}

	/**
	 * Set the frequency on VFO B
	 * 
	 * @param frequency
	 *            The frequency to set
	 * @throws IOException
	 *             If a read/write error occurs
	 */
	public void RadioSetFreqB(int frequency) throws IOException {
		if (currentMode != STATUS_MODE)
			throw new IllegalStateException("Radio must be in the status mode to deal with frequency!");

		if (frequency < 0) {
			throw new IllegalArgumentException("Invalid frequency: " + frequency);
		}

		String cmd = String.format("%dFB%011d;", client.getSerialPortNum(), frequency);
		client.write(cmd);
	}

	/**
	 * Set the frequency on VFO C
	 * 
	 * @param frequency
	 *            The frequency to set
	 * @throws IOException
	 *             If a read/write error occurs
	 */
	public void RadioSetFreqSub(int frequency) throws IOException {
		if (currentMode != STATUS_MODE)
			throw new IllegalStateException("Radio must be in the status mode to deal with frequency!");

		if (frequency < 0) {
			throw new IllegalArgumentException("Invalid frequency: " + frequency);
		}

		String cmd = String.format("%dFC%011d;", client.getSerialPortNum(), frequency);
		client.write(cmd);
	}

	/**
	 * Get the current frequency of VFO A
	 * 
	 * @return The current frequency of VFO A
	 * @throws IOException
	 *             If a read/write error occurs
	 */
	public int RadioGetFreqA() throws IOException {
		if (currentMode != STATUS_MODE)
			throw new IllegalStateException("Radio must be in the status mode to deal with frequency!");

		String cmd = String.format("%dFA;", client.getSerialPortNum());
		String result = "FA([0-9]{11});";
		return getIntFromRadio(cmd, result);
	}

	/**
	 * Get the current frequency of VFO B
	 * 
	 * @return The current frequency of VFO B
	 * @throws IOException
	 *             If a read/write error occurs
	 */
	public int RadioGetFreqB() throws IOException {
		if (currentMode != STATUS_MODE)
			throw new IllegalStateException("Radio must be in the status mode to deal with frequency!");

		String cmd = String.format("%dFB;", client.getSerialPortNum());
		String result = "FB([0-9]{11});";
		return getIntFromRadio(cmd, result);
	}

	/**
	 * Get the current frequency of VFO C
	 * 
	 * @return The current frequency of VFO C
	 * @throws IOException
	 *             If a read/write error occurs
	 */
	public int RadioGetFreqSub() throws IOException {
		if (currentMode != STATUS_MODE)
			throw new IllegalStateException("Radio must be in the status mode to deal with frequency!");

		String cmd = String.format("%dFC;", client.getSerialPortNum());
		String result = "FC([0-9]{11});";
		return getIntFromRadio(cmd, result);
	}

	/**
	 * Read an int value from the radio, using the given command and expected the result in the given format.
	 * 
	 * @param cmd
	 *            The command to send to the server (to be sent to the radio)
	 * @param expectedResultFormat
	 *            The expected format (in regex) of the output. There is expected to be a single grouping containing the integer result.
	 *            This grouping will be parsed as an int and returned.
	 * @return An int value from the radio's response to the given command
	 * @throws IOException
	 *             In the case of a read/write error
	 */
	private int getIntFromRadio(String cmd, String expectedResultFormat) throws IOException {
		final StringBuffer data = new StringBuffer();
		SerialDataListener serialListener = new SerialDataListener() {
			@Override
			public void dataReceived(String serialData) {
				data.append(serialData);
			}
		};
		client.addListener(serialListener);

		// Write the command to poll the radio for the frequency
		client.write(cmd);

		// Wait for data to come in, terminated by a ';' character
		long startTime = System.currentTimeMillis();
		while (!data.toString().endsWith(";") && System.currentTimeMillis() - startTime < TIMEOUT_TIME) {
			// Wait a little bit
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		client.removeListener(serialListener);

		// Parse the data string for the frequency
		int result = -1;
		try {
			Scanner dataScanner = new Scanner(data.toString());
			Pattern dataPattern = Pattern.compile(expectedResultFormat);
			if (dataScanner.hasNext(dataPattern)) {
				result = Integer.parseInt(dataScanner.match().group(1));
			} else {
				System.out.println("INVALID DATA RECEIVED: " + data + " (" + Arrays.toString(data.toString().getBytes()) + ")");
			}

			dataScanner.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Get the current mode of the TS-2000 radio
	 * 
	 * @return The current mode of the TS-2000 radio
	 */
	public int getCurrentMode() {
		return currentMode;
	}

	/**
	 * Set the mode of the radio. Valid choices are:
	 * <ul>
	 * <li><b>PACKET_MODE</b> (for sending and receiving data packets)</li>
	 * <li><b>STATUS_MODE</b> (for updating and polling the status of the radio [current frequency setting, etc.])</li>
	 * </ul>
	 * 
	 * @param mode
	 * @throws IOException
	 */
	public void setMode(int mode) {
		try {
			if (mode == TS2000Radio.PACKET_MODE) {
				if (currentMode == TS2000Radio.PACKET_MODE) {
					throw new IllegalStateException("Already in packet mode!");
				}

				String cmd = String.format("%dTC 0;\n", client.getSerialPortNum());
				client.write(cmd);
				client.write(client.getSerialPortNum() + "\n");
				client.write(client.getSerialPortNum() + "\n");

				currentMode = PACKET_MODE;
				System.out.println("Now in packet mode!");
			} else if (mode == TS2000Radio.STATUS_MODE) {
				if (currentMode == TS2000Radio.STATUS_MODE) {
					throw new IllegalStateException("Already in status mode!");
				}

				String cmd = String.format("%dTC 1;\n", client.getSerialPortNum());
				client.write(cmd);
				currentMode = STATUS_MODE;
				System.out.println("Now in status mode!");
			} else {
				throw new IllegalArgumentException("Invalid mode!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendSerialMsg(String msg) throws IOException {
		client.write(client.getSerialPortNum() + msg);
	}
}
