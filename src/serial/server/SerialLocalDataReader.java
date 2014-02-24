package serial.server;

import java.io.IOException;
import java.io.InputStream;

import serial.SerialUtils;
import serial.client.SerialClient;

/**
 * Implementation of SerialDataReader for doing local serial communication without the server
 * 
 * @author Adam Campbell
 */
public class SerialLocalDataReader extends SerialDataReader {
	/**
	 * The serial client
	 */
	private SerialClient client;
	
	/**
	 * The port name, for logging purposes
	 */
	private String serialPortName;

	/**
	 * Constructs a new SerialLocalDataReader to read from the serial input stream and write data to the serial client
	 * 
	 * @param serialIn
	 *            Serial input stream from the device
	 * @param client
	 *            Client to direct output to
	 * @param serialPortNum
	 *            Port number ID for logging purposes
	 */
	public SerialLocalDataReader(InputStream serialIn, SerialClient client, String serialPortName) {
		super(serialIn);
		this.client = client;
		this.serialPortName = serialPortName;
	}

	@Override
	public void handleSerialDataReceived(String data) throws IOException {
		// Back up the data locally
		SerialUtils.backupData(data, SerialUtils.getJarDirectory() + "Data-Logs/Serial-Data/Port-" + serialPortName + "/");

		// Write the data to the client
		client.notifyListeners(data);
	}
}
