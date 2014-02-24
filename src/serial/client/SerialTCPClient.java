package serial.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import serial.SerialUtils;

/**
 * A client class to communicate with the base station server
 * 
 * @author Adam Campbell
 */
public class SerialTCPClient extends SerialClient {
	/**
	 * The server socket
	 */
	private Socket server = null;

	/**
	 * The thread that reads data from the server and handles it
	 */
	private DataInThread dataIn;

	/**
	 * The timeout time to wait for a server connection, in ms
	 */
	private static final int TIMEOUT_TIME = 5000;

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
	public SerialTCPClient(String host, int portNum, String username, String password) {
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
	public SerialTCPClient(String host, int portNum, String username, String password, int serialPortNum) {
		try {
			if (serialPortNum < 0 || serialPortNum > 9) {
				throw new IllegalArgumentException("Invalid serial port number: " + serialPortNum);
			}

			// Connect to the server
			server = new Socket();
			server.connect(new InetSocketAddress(host, portNum), TIMEOUT_TIME);

			in = server.getInputStream();
			out = server.getOutputStream();

			// Check to see if the username and password are valid
			if (validate(username, password)) {
				System.out.println();
				System.out.println("===== CLIENT READY TO HANDLE MESSAGES =====");
				System.out.println();

				// Start the data thread
				dataIn = new DataInThread(in);
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
		out.write((username + "\n").getBytes());
		out.write((password + "\n").getBytes());

		// Wait for the server's response
		byte[] buffer = new byte[SerialUtils.BUFFER_SIZE];
		in.read(buffer);
		String serverResponse = new String(SerialUtils.trimTrailing0s(buffer));

		// If the server responded with the valid user message, then we're good
		return serverResponse.equals(SerialUtils.VALID_USER_MESSAGE);
	}

	/**
	 * Write data to the serial server, adding whatever implementation-specific details are required
	 * 
	 * @param data
	 *            The data to write
	 * @throws IOException
	 */
	@Override
	public void write(String data) throws IOException {
		if (data != null && !data.isEmpty()) {
			out.write(String.format("%d", serialPortNum, data).getBytes());
		}
	}

	/**
	 * Write data to the serial server as is, nothing added or removed
	 * 
	 * @param data
	 *            The data to write
	 * @throws IOException
	 */
	@Override
	public void writeVerbatim(String data) throws IOException {
		if (data != null && !data.isEmpty()) {
			out.write(data.getBytes());
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
