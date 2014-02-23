package serial.client;

/**
 * A listener for handling serial data received
 * 
 * @author Adam Campbell
 */
public interface SerialBufferedDataListener {
	/**
	 * Called when data was received by the serial client
	 * 
	 * @param data
	 *            Data that was received - normally a single line of output from the serial device
	 */
	void serialBufferedDataReceived(String data);
}
