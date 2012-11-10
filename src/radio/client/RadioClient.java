package radio.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import radio.RadioUtil;

/**
 * A client class to communicate with the base station server
 * 
 * @author Adam Campbell
 */
public class RadioClient {
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
	 * The data that the client has received from the server
	 */
	private List<String> data;

	/**
	 * Start the client
	 * 
	 * @param args
	 *            The host and port number of the server
	 */
	public RadioClient(String host, int portNum, String username, String password) {
		data = (List<String>) Collections.synchronizedList(new ArrayList<String>());

		try{
			// Connect to the server
			server = new Socket(host, portNum);

			serverIn = server.getInputStream();
			serverOut = server.getOutputStream();

			// Check to see if the username and password are valid
			if(validate(username, password)){
				System.out.println();
				System.out.println("===== CLIENT READY TO HANDLE MESSAGES =====");
				System.out.println();

				// Start the data thread
				dataIn = new DataInThread(new BufferedReader(new InputStreamReader(serverIn)));
				dataIn.start();
			} else{
				System.out.println("Invalid username and password. Exiting...");
				die();
			}
		} catch(ConnectException e){
			System.err.println("Unable to connect to " + host + ":" + portNum + ". Exiting...");
			die();
		} catch(SocketException e){
			System.err.println();
			System.err.println("Server Connection Reset. Exiting...");
			die();
		} catch(Exception e){
			e.printStackTrace();
			System.err.println("Radio Client Error. Exiting...");
			die();
		}
	}

	/**
	 * Validate this client using a username and password. This information will
	 * be checked against a whitelist of valid users that is maintained by the
	 * server. If the login info appears in the whitelist then the server shall
	 * deem this client validated and communication with the serial ports may
	 * proceed.
	 * 
	 * @param username
	 *            The username to give to the server
	 * @param password
	 *            The password to give to the server
	 * @return True if the validation was successful and false otherwise
	 * @throws IOException
	 *             If there's an error reading from or writing to the server
	 */
	private boolean validate(String username, String password) throws IOException{
		// Write the username and password to the server
		serverOut.write(username.getBytes());
		serverOut.write(password.getBytes());

		// Wait for the server's response
		byte[] buffer = new byte[RadioUtil.BUFFER_SIZE];
		serverIn.read(buffer);
		String serverResponse = new String(RadioUtil.trimTrailing0s(buffer));

		// If the server responded with the valid user message, then we're good
		return serverResponse.equals(RadioUtil.VALID_USER_MESSAGE);
	}

	/**
	 * Whether or not the client has data from the server
	 * 
	 * @return
	 */
	public synchronized boolean hasData(){
		return data.size() > 0;
	}

	/**
	 * Get the oldest data element from the server. The data acts like a queue
	 * in that regard (FIFO), so if there are multiple data elements, this
	 * method can be called to get them in order from oldest to most recent.
	 * 
	 * @return
	 */
	public synchronized String getData(){
		if(!hasData())
			return null;

		return data.remove(0);
	}

	/**
	 * Get the input stream (for reading from the server)
	 * 
	 * @return
	 */
	public InputStream getInputStream(){
		return serverIn;
	}

	/**
	 * Get the output stream (for writing to the server)
	 * 
	 * @return
	 */
	public OutputStream getOutputStream(){
		return serverOut;
	}

	/**
	 * Called when the client needs to die
	 */
	public void die(){
		System.exit(0);
	}

	/**
	 * Reads from server, writes to console
	 */
	private class DataInThread extends Thread {
		public DataInThread(final BufferedReader in) {
			super(new Runnable(){
				@Override
				public void run(){
					String msg = null;
					try{
						// Read from the server and add it to the list
						while((msg = in.readLine()) != null){
							synchronized(data){
								data.add(msg);
							}
						}
					} catch(SocketException e){
						System.err.println("Server connection reset. Reconnect needed. Exiting...");
						die();
					} catch(IOException e){
						System.err.println("Error when reading from server.");
						e.printStackTrace();
					}
				}
			});
		}
	}
}
