package serial.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * A serial client that handles reading to and writing from serial devices
 * @author Adam Campbell
 */
public abstract class SerialClient {
	/**
	 * A list of all the serial data listeners
	 */
	private List<SerialBufferedDataListener> listeners = new LinkedList<>();

	/**
	 * The input stream
	 */
	protected InputStream in = null;

	/**
	 * The output stream
	 */
	protected OutputStream out = null;
	
	/**
	 * Client states
	 */
	public enum State {
		ALIVE, DEAD, INVALID_PASSWORD
	}

	/**
	 * State of the client
	 */
	protected State state;
	
	/**
	 * Get the input stream (for reading from the server)
	 * 
	 * @return
	 */
	public InputStream getInputStream() {
		return in;
	}

	/**
	 * Get the output stream (for writing to the server)
	 * 
	 * @return
	 */
	public OutputStream getOutputStream() {
		return out;
	}
	
	/**
	 * Called when the client needs to die
	 */
	public void die() {
		state = State.DEAD;
	}

	/**
	 * Return the client state
	 * 
	 * @return the client state
	 */
	public State getState() {
		return state;
	}
	
	/**
	 * Add a listener to be notified when serial data is received
	 * 
	 * @param l
	 */
	public void addListener(SerialBufferedDataListener l) {
		if (l == null) {
			throw new IllegalArgumentException("Listener cannot be null!");
		}
		
		synchronized (listeners) {
			listeners.add(l);
		}
	}

	/**
	 * Remove a listener so it's no longer notified when serial data is received
	 * 
	 * @param l
	 */
	public void removeListener(SerialBufferedDataListener l) {
		synchronized (listeners) {
			listeners.remove(l);
		}
	}

	/**
	 * Notify all the data listeners we have about some new data
	 * 
	 * @param data
	 *            The new data received
	 */
	public void notifyListeners(String data) {
		synchronized (listeners) {
			for (SerialBufferedDataListener l : listeners) {
				l.serialBufferedDataReceived(data);
			}
		}
	}

	
	/**
	 * Write data to the serial server, adding whatever implementation-specific details are required
	 * 
	 * @param data
	 *            The data to write
	 * @throws IOException
	 */
	public abstract void write(String data) throws IOException;
	
	/**
	 * Write data to the serial server as is, nothing added or removed
	 * 
	 * @param data
	 *            The data to write
	 * @throws IOException
	 */
	public abstract void writeVerbatim(String data) throws IOException;
}
