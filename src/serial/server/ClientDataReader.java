package serial.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

import serial.SerialUtils;
import serial.server.SerialServer.AllowedUser;

/**
 * Reads data from the client and handles it appropriately
 * 
 * @author Adam Campbell
 */
public class ClientDataReader implements Runnable {
	/**
	 * The client socket
	 */
	private Socket client;

	/**
	 * The server to which the client is connected
	 */
	private SerialServer server;

	/**
	 * The client number
	 */
	private int clientNum;

	/**
	 * Construct a new ClientDataReader with the given parameters
	 * 
	 * @param client
	 *            The client from which to listen for data
	 * @param server
	 *            The server to which to report the data
	 * @param clientNum
	 *            The client number
	 */
	public ClientDataReader(Socket client, SerialServer server, int clientNum) {
		this.client = client;
		this.server = server;
		this.clientNum = clientNum;
	}

	/**
	 * Validate this client with the server by listening for a username and password and checking it against the server's whitelist.
	 * 
	 * @return True if the client has been successfully validated, false otherwise
	 */
	private boolean validate() {
		ArrayList<AllowedUser> whiteList = server.getWhiteList();
		boolean valid;

		try {
			// Read in the username and password from the client,
			// which will be the first two messages sent from it when it
			// connects to the server
			OutputStream clientOut = client.getOutputStream();
			InputStream clientIn = client.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(clientIn));

			String clientUser = br.readLine();
			String clientPass = br.readLine();

			// Check if it appears in the whitelist and send the
			// client the appropriate result message
			if (clientUser != null && clientPass != null && whiteList.contains(new AllowedUser(clientUser.trim(), clientPass.trim()))) {
				valid = true;
				clientOut.write((SerialUtils.VALID_USER_MESSAGE + "\n").getBytes());
			} else {
				valid = false;
				clientOut.write((SerialUtils.INVALID_USER_MESSAGE + "\n").getBytes());
			}
		} catch (IOException e) {
			valid = false;
		}

		if (valid) {
			System.out.println();
			System.out.println("Connected to client #" + clientNum + ": " + client.getRemoteSocketAddress());
			System.out.println("===== SERVER READY TO HANDLE MESSAGES =====");
			System.out.println();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Process a command received from the client, writing it directly to the server's serial ports
	 * 
	 * @param cmd
	 *            The command sent from the client
	 * @param serialPortNum
	 *            The serial port number to which the command is directed
	 * @throws IOException
	 *             If writing to the server's serial ports goes wrong
	 */
	private void processCommand(String cmd, int serialPortNum) throws IOException {
		cmd = cmd.replaceAll("\\\\r", "\r");
		cmd = cmd.replaceAll("\\\\n", "\n");

		server.getSerialOut(serialPortNum).write(cmd.getBytes());
	}

	/**
	 * Handle data received from the client
	 * 
	 * @param data
	 *            The data sent from the client
	 * @param serialPortNum
	 *            The serial port to which the data is directed
	 * @throws IOException
	 *             If writing goes wrong
	 */
	private void handleClientDataReceived(String data, int serialPortNum) throws IOException {
		// Only [0-9] are valid serial ports
		if (serialPortNum < 0 || serialPortNum > 9) {
			return;
		}

		// Backup the data
		SerialUtils.backupData(data, SerialUtils.getJarDirectory() + "Data-Logs/Client-Data/");

		// Write the serial data if the serial port number is valid
		if (server.getSerialOut(serialPortNum) != null) {
			processCommand(data, serialPortNum);
			System.out.println("Received from client #" + clientNum + ": " + data + " (" + Arrays.toString(data.getBytes()) + ")");

			// Tell the serial reader which client the most recent command
			// came from, so it can direct the serial port's response
			// to the appropriate client
			if (server.getSerialReader(serialPortNum) != null) {
				server.getSerialReader(serialPortNum).CLIENT_NUM = clientNum;
			} else {
				System.out.println("Invalid serial port number: " + serialPortNum);
			}
		} else {
			System.err.println("Ignoring message with invalid serial destination: " + serialPortNum);
			return;
		}
	}

	@Override
	public void run() {
		// Validate the client, and if the username and password check out then
		// proceed with the connection
		if (validate()) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
				String line = null;

				// Read a message and process it as long as there's data
				while ((line = br.readLine()) != null) {
					if (line.length() > 0) {
						int serialPortNum = -1;
						if (Character.isDigit(line.charAt(0))) {
							serialPortNum = (int) (line.charAt(0) - '0');
						} else {
							System.err.println("Ignoring message without specified serial destination.");
						}

						handleClientDataReceived(line.substring(1), serialPortNum);
					}
				}

				// Close the client if there's no more data
				server.closeClient(clientNum, client.getInputStream());
			} catch (SocketException e) {
				// Close the client if the connection is severed
				try {
					server.closeClient(clientNum, client.getInputStream());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} catch (Exception e) {
				System.err.println("Error reading from client!");
				e.printStackTrace();
			}
		}
	}
}
