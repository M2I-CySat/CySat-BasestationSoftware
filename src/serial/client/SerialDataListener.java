package serial.client;

/**
 * A listener for handling serial data received
 * @author Adam Campbell
 */
public interface SerialDataListener {
	/**
	 * Called when data was received by the serial client
	 * @param data
	 * Data that was received - normally a single line of output from the serial device
	 */
	void dataReceived(String data);
}
