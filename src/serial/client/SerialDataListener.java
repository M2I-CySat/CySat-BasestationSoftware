package serial.client;

/**
 * A listener for handling serial data received
 * @author Adam Campbell
 */
public interface SerialDataListener {
	void dataReceived(String data);
}
