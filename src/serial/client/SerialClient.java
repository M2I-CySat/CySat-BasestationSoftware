package serial.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;

import serial.SerialUtils;

/**
 * A client class to communicate with the base station server
 * 
 * @author Adam Campbell
 */
public class SerialClient {
	/**
	 * The server socket
	 */
	private Socket server = null;

	/**
	 * The server input stream
	 */
	private InputStream serverIn;

	/**
	 * The server output stream
	 */
	private OutputStream serverOut;

	/**
	 * The thread that reads data from the server and handles it
	 */
	private DataInThread dataIn;

	/**
	 * Client states
	 */
	public enum State {
		ALIVE, DEAD, INVALID_PASSWORD
	}

	/**
	 * State of the client
	 */
	private State state;

	/**
	 * The timeout time to wait for a server connection, in ms
	 */
	private static final int TIMEOUT_TIME = 5000;

	/**
	 * A list of all the serial data listeners
	 */
	private List<SerialBufferedDataListener> listeners = new LinkedList<>();

	/**
	 * Serial port number that this client is meant for on the server
	 */
	private int serialPortNum;

	/**
	 * Start the client
	 * 
	 * @param host
	 *            Host for the server
	 * @param portNum
	 *            Portnum for the server
	 * @param username
	 *            Username to log in
	 * @param password
	 *            Password to log in
	 */
	public SerialClient(String host, int portNum, String username, String password) {
		this(host, portNum, username, password, 0);
	}

	/**
	 * Start the client
	 * 
	 * @param host
	 *            Host for the server
	 * @param portNum
	 *            Portnum for the server
	 * @param username
	 *            Username to log in
	 * @param password
	 *            Password to log in
	 * @param serialPortNum
	 *            (optional) id for the serial port on the server computer
	 */
	public SerialClient(String host, int portNum, String username, String password, int serialPortNum) {
		try {
			if (serialPortNum < 0 || serialPortNum > 9) {
				throw new IllegalArgumentException("Invalid serial port number: " + serialPortNum);
			}

			// Connect to the server
			server = new Socket();
			server.connect(new InetSocketAddress(host, portNum), TIMEOUT_TIME);

			serverIn = server.getInputStream();
			serverOut = server.getOutputStream();

			// Check to see if the username and password are valid
			if (validate(username, password)) {
				System.out.println();
				System.out.println("===== CLIENT READY TO HANDLE MESSAGES =====");
				System.out.println();

				// Start the data thread
				dataIn = new DataInThread(serverIn);
				dataIn.start();

				state = State.ALIVE;
			} else {
				state = State.INVALID_PASSWORD;
				System.err.println("Invalid username and password. Client dying...");
				die();
			}
		} catch (ConnectException e) {
			System.err.println("Unable to connect to " + host + ":" + portNum + ". Client dying...");
			die();
		} catch (SocketException e) {
			System.err.println();
			System.err.println("Server Connection Reset. Client dying...");
			die();
		} catch (SocketTimeoutException e) {
			System.err.println("Unable to connect to " + host + ":" + portNum + ". Client dying...");
			die();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Serial Client Error. Client dying...");
			die();
		}
	}

	/**
	 * Return the serial port number that this client is attached to
	 * 
	 * @return
	 */
	public int getSerialPortNum() {
		return serialPortNum;
	}

	/**
	 * Validate this client using a username and password. This information will be checked against a whitelist of valid users that is
	 * maintained by the server. If the login info appears in the whitelist then the server shall deem this client validated and
	 * communication with the serial ports may proceed.
	 * 
	 * @param username
	 *            The username to give to the server
	 * @param password
	 *            The password to give to the server
	 * @return True if the validation was successful and false otherwise
	 * @throws IOException
	 *             If there's an error reading from or writing to the server
	 * @throws InterruptedException
	 */
	private boolean validate(String username, String password) throws IOException, InterruptedException {
		// Write the username and password to the server
		serverOut.write((username + "\n").getBytes());
		serverOut.write((password + "\n").getBytes());

		// Wait for the server's response
		byte[] buffer = new byte[SerialUtils.BUFFER_SIZE];
		serverIn.read(buffer);
		String serverResponse = new String(SerialUtils.trimTrailing0s(buffer));

		// If the server responded with the valid user message, then we're good
		return serverResponse.equals(SerialUtils.VALID_USER_MESSAGE);
	}

	/**
	 * Get the input stream (for reading from the server)
	 * 
	 * @return
	 */
	public InputStream getInputStream() {
		return serverIn;
	}

	/**
	 * Get the output stream (for writing to the server)
	 * 
	 * @return
	 */
	public OutputStream getOutputStream() {
		return serverOut;
	}

	/**
	 * Write data to the serial server
	 * 
	 * @param data
	 *            The data to write
	 * @throws IOException
	 */
	public void write(String data) throws IOException {
		serverOut.write(data.getBytes());
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
	private void notifyListeners(String data) {
		synchronized (listeners) {
			for (SerialBufferedDataListener l : listeners) {
				l.serialBufferedDataReceived(data);
			}
		}
	}

	/**
	 * Reads from server, writes to console
	 */
	private class DataInThread extends Thread {
		public DataInThread(final InputStream in) {
			super(new Runnable() {
				@Override
				public void run() {
					String line = null;
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					try {
						// Read from the server and add it to the list
						while ((line = br.readLine()) != null) {
							notifyListeners(line);
						}
					} catch (SocketException e) {
						System.err.println("Server connection reset. Reconnect needed. Client dying...");
						die();
					} catch (IOException e) {
						System.err.println("Error when reading from server.");
						e.printStackTrace();
					}
				}
			});
		}
	}
}
