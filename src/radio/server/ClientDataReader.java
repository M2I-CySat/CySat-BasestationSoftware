package radio.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import radio.RadioUtil;
import radio.server.RadioServer.AllowedUser;

/**
 * Reads data from the client and handles it appropriately
 */
public class ClientDataReader implements Runnable {
	/**
	 * The client socket
	 */
	private Socket client;

	/**
	 * The server to which the client is connected
	 */
	private RadioServer server;

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
	public ClientDataReader(Socket client, RadioServer server, int clientNum) {
		this.client = client;
		this.server = server;
		this.clientNum = clientNum;
	}

	/**
	 * Validate this client with the server by listening for a username and
	 * password and checking it against the server's whitelist.
	 * 
	 * @return True if the client has been successfully validated, false
	 *         otherwise
	 */
	private boolean validate(){
		ArrayList<AllowedUser> whiteList = server.getWhiteList();
		boolean valid;

		try{
			// Read in the username and password from the client,
			// which will be the first two messages sent from it when it
			// connects to the server
			OutputStream clientOut = client.getOutputStream();
			InputStream clientIn = client.getInputStream();
			byte[] buffer = new byte[RadioUtil.BUFFER_SIZE];

			clientIn.read(buffer);
			String clientUser = new String(RadioUtil.trimTrailing0s(buffer));
			RadioUtil.clear(buffer);

			clientIn.read(buffer);
			String clientPass = new String(RadioUtil.trimTrailing0s(buffer));
			RadioUtil.clear(buffer);

			// Check if it appears in the whitelist and send the
			// client the appropriate result message
			if(whiteList.contains(new AllowedUser(clientUser.trim(), clientPass.trim()))){
				valid = true;
				clientOut.write(RadioUtil.VALID_USER_MESSAGE.getBytes());
			} else{
				valid = false;
				clientOut.write(RadioUtil.INVALID_USER_MESSAGE.getBytes());
			}
		} catch(IOException e){
			valid = false;
		}

		if(valid){
			System.out.println();
			System.out.println("Connected to client #" + clientNum + ": " + client.getRemoteSocketAddress());
			System.out.println("===== SERVER READY TO HANDLE MESSAGES =====");
			System.out.println();
			return true;
		} else{
			return false;
		}
	}

	/**
	 * Process a command received from the client, writing it directly to the
	 * server's serial ports
	 * 
	 * @param cmd
	 *            The command sent from the client
	 * @param serialPortNum
	 *            The serial port number to which the command is directed
	 * @throws IOException
	 *             If writing to the server's serial ports goes wrong
	 */
	private void processCommand(String cmd, int serialPortNum) throws IOException{
		// System.out.println("ddd: " + Arrays.toString(cmd.getBytes()));
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
	private void handleClientDataReceived(String data, int serialPortNum) throws IOException{
		// Only [0-9] are valid serial ports
		if(serialPortNum < 0 || serialPortNum > 9){
			return;
		}

		// Backup the data
		RadioUtil.backupData(data, RadioUtil.getJarDirectory() + "Data-Logs/Client-Data/");

		// Write the serial data if the serial port number is valid
		if(server.getSerialOut(serialPortNum) != null){
			processCommand(data, serialPortNum);
			System.out.println("Received from client #" + clientNum + ": " + data);

			// Tell the serial reader which client the most recent command
			// came from, so it can direct the serial port's response
			// to the appropriate client
			if(server.getSerialReader(serialPortNum) != null){
				server.getSerialReader(serialPortNum).CLIENT_NUM = clientNum;
			} else{
				System.out.println("Invalid serial port number: " + serialPortNum);
			}
		} else{
			System.err.println("Ignoring message with invalid serial destination: " + serialPortNum);
			return;
		}
	}

	@Override
	public void run(){
		// Validate the client, and if the username and password check out then
		// proceed with the connection
		if(validate()){
			byte[] buffer = new byte[RadioUtil.BUFFER_SIZE];
			try{
				// Read a message and process it as long as there's data
				while(client.getInputStream().read(buffer) != -1){
					String msg = new String(buffer, 0, RadioUtil.trimTrailing0s(buffer).length);
					if(msg.length() > 0){
						// Remove the line ending from the message
						int windows = msg.lastIndexOf("\r\n");
						int unix = msg.lastIndexOf("\n");
						if(windows > -1){
							msg = msg.substring(0, windows);
						} else if(unix > -1){
							msg = msg.substring(0, unix);
						}

						int serialPortNum = -1;
						if(Character.isDigit(msg.charAt(0))){
							serialPortNum = (int) (msg.charAt(0) - '0');
						} else{
							System.err.println("Ignoring message without specified serial destination.");
						}

						// Add a single '\r' to the end of the message (so the
						// TS2000 antenna rotator recognizes it)
						if(!msg.endsWith("\r"))
							msg += '\r';

						handleClientDataReceived(msg.substring(1), serialPortNum);
						RadioUtil.clear(buffer);
					}
				}

				// Close the client if there's no more data
				server.closeClient(clientNum, client.getInputStream());
			} catch(SocketException e){
				// Close the client if the connection is severed
				try{
					server.closeClient(clientNum, client.getInputStream());
				} catch(IOException e1){
					e1.printStackTrace();
				}
			} catch(Exception e){
				System.err.println("Error reading from client!");
				e.printStackTrace();
			}
		}
	}
}
