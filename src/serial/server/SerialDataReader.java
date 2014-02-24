package serial.server;

import java.io.IOException;
import java.io.InputStream;

/**
 * A reader that sits and waits for serial data to come in 
 * 
 * @author Adam Campbell
 */
public abstract class SerialDataReader implements Runnable {
	/**
	 * The serial input stream
	 */
	private InputStream serialIn;

	/**
	 * Delimiters to use when parsing input - controls when a line ends
	 */
	private String delimiters = "\r\n;$";

	/**
	 * Construct a serial data reader that reads from the given serial port and processes output according to
	 * the handleSerialDataReceived() method
	 * 
	 * @param serialIn
	 *            The input stream for the serial port
	 * @param server
	 *            The server that this reader belongs to
	 * @param serialPortNum
	 *            The serial port number
	 */
	public SerialDataReader(InputStream serialIn) {
		this.serialIn = serialIn;
	}

	@Override
	public void run() {
		boolean keepRunning = true;
		while (keepRunning) {
			try {
				// Read a line and process it, checking every 100ms
				String data = readSerialData(serialIn, delimiters);
				if (!data.isEmpty()) {
					handleSerialDataReceived(data);
				}
			} catch (IOException e) {
				// The stream is empty
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					keepRunning = false;
				}
			}
		}
	}

	/**
	 * Read serial data from the given input stream, using the given delimiters to buffer input
	 * 
	 * @param in
	 *            Input stream
	 * @param delimiters
	 *            Delimiters to buffer input
	 * @return The first String read from the input string that consists entirely of characters not found among the delimiters
	 * @throws IOException
	 */
	private String readSerialData(InputStream in, String delimiters) throws IOException {
		StringBuilder sb = new StringBuilder();
		int data;
		while ((data = in.read()) != -1) {
			sb.append((char) data);

			if (delimiters.contains("" + ((char) data))) {
				return sb.toString();
			}
		}

		return sb.toString();
	}

	/**
	 * Handle the serial data received
	 * 
	 * @param data
	 *            The serial data received
	 * @throws IOException
	 */
	public abstract void handleSerialDataReceived(String data) throws IOException;
}