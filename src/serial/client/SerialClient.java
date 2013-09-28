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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import serial.SerialUtils;

/**
 * A client class to communicate with the base station server
 * 
 * @author Adam Campbell
 */
public class SerialClient {
	/**
	 * The timeout value for reading from the serial port, in milliseconds
	 */
	public static final int READ_TIMEOUT = 5000;

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
	 * Start the client
	 * 
	 * @param args
	 *            The host and port number of the server
	 */
	public SerialClient(String host, int portNum, String username, String password) {
		data = (List<String>) Collections.synchronizedList(new ArrayList<String>());

		try{
			// Connect to the server
			server = new Socket();
			server.connect(new InetSocketAddress(host, portNum), TIMEOUT_TIME);

			serverIn = server.getInputStream();
			serverOut = server.getOutputStream();

			// Check to see if the username and password are valid
			if(validate(username, password)){
				System.out.println();
				System.out.println("===== CLIENT READY TO HANDLE MESSAGES =====");
				System.out.println();

				// Start the data thread
				dataIn = new DataInThread(serverIn);
				dataIn.start();

				state = State.ALIVE;
			} else{
				state = State.INVALID_PASSWORD;
				System.err.println("Invalid username and password. Exiting...");
				die();
			}
		} catch(ConnectException e){
			System.err.println("Unable to connect to " + host + ":" + portNum + ". Exiting...");
			die();
		} catch(SocketException e){
			System.err.println();
			System.err.println("Server Connection Reset. Exiting...");
			die();
		} catch(SocketTimeoutException e){
			System.err.println("Unable to connect to " + host + ":" + portNum + ". Exiting...");
			die();
		} catch(Exception e){
			e.printStackTrace();
			System.err.println("Serial Client Error. Exiting...");
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
	 * @throws InterruptedException 
	 */
	private boolean validate(String username, String password) throws IOException, InterruptedException{
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
	 * If there is no data in the Queue, this method will return null without
	 * blocking.
	 * 
	 * @return
	 */
	public synchronized String getData(){
		if(!hasData())
			return null;

		return data.remove(0);
	}

	/**
	 * Get a single line of data from the server. This method will block until
	 * data is received or until <code>READ_TIMEOUT</code> milliseconds have
	 * passed.
	 */
	public synchronized String getLineOfData(){
		long startTime = System.currentTimeMillis();
		while(!hasData()){
			try{
				// If we've been waiting for too long, give up and return null
				if(System.currentTimeMillis() - startTime > READ_TIMEOUT){
					return null;
				}

				Thread.sleep(20);
			} catch(InterruptedException e){
				die();
			}
		}

		return getData();
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
	 * Write data to the serial server
	 * 
	 * @param data
	 *            The data to write
	 * @throws IOException
	 */
	public void write(String data) throws IOException{
		serverOut.write(data.getBytes());
	}

	/**
	 * Called when the client needs to die
	 */
	public void die(){
		state = State.DEAD;
	}

	/**
	 * Return the client state
	 * @return
	 * the client state
	 */
	public State getState(){
		return state;
	}

	/**
	 * Reads from server, writes to console
	 */
	private class DataInThread extends Thread {
		public DataInThread(final InputStream in) {
			super(new Runnable(){
				@Override
				public void run(){
					String line = null;
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					try{
						// Read from the server and add it to the list
						while((line = br.readLine()) != null){
							synchronized(data){
								data.add(line);
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