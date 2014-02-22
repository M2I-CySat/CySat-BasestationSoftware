package serial.server;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.TooManyListenersException;

/**
 * The server that listens to the serial port and echoes the serial data back to the clients, as well as listen for data from the clients
 * and send it to the serial ports.
 * 
 * @author Adam Campbell
 */
public class SerialServer {
	/**
	 * A class to hold an allowed user to the server. It has a username and password.
	 */
	public static class AllowedUser {
		private String username;
		private String password;

		public AllowedUser(String username, String password) {
			this.username = username;
			this.password = password;
		}

		public String getUsername() {
			return username;
		}

		public String getPassword() {
			return password;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || o.getClass() != this.getClass())
				return false;

			AllowedUser au = (AllowedUser) o;
			return au.username != null && au.username.equals(username) && au.password != null && au.password.equals(password);
		}
	}

	/**
	 * A holder class to pair a client number with its socket
	 */
	private class ClientPair {
		private int clientNum;
		private Socket client;

		public ClientPair(Socket client, int clientNum) {
			this.client = client;
			this.clientNum = clientNum;
		}

		public int getClientNum() {
			return clientNum;
		}

		public Socket getClient() {
			return client;
		}
	}

	/**
	 * The number of serial connections handled by the server
	 */
	private int NSERIAL_CONNECTIONS;

	/**
	 * The baud rate of the antenna
	 */
	private int BAUD_RATE = 9600;

	/**
	 * The port number that this server is assigned to
	 */
	private int portNum = -1;

	/**
	 * The serial input streams used for reading data from the serial ports
	 */
	private InputStream[] serialIns;

	/**
	 * The serial output streams used for writing data to the serial ports
	 */
	private OutputStream[] serialOuts;

	/**
	 * The white list of AllowedUsers that are permitted to login to this server
	 */
	private ArrayList<AllowedUser> whiteList;

	/**
	 * The clients currently maintained by this server
	 */
	private ArrayList<ClientPair> clients;

	/**
	 * The input streams for each client, used for reading data from them
	 */
	private ArrayList<InputStream> clientIns;

	/**
	 * The output streams for each client, used for writing data to them
	 */
	private ArrayList<OutputStream> clientOuts;

	/**
	 * The server socket that is the server itself
	 */
	private ServerSocket server;

	/**
	 * The serial readers for each port
	 */
	private ArrayList<SerialDataReader> serialReaders = new ArrayList<SerialDataReader>();

	/**
	 * Construct a new Serial Server on the given port
	 * 
	 * @param portNum
	 *            The port that the server is assigned to
	 */
	public SerialServer(int portNum) {
		this.portNum = portNum;
		clientIns = new ArrayList<InputStream>();
		clientOuts = new ArrayList<OutputStream>();
		clients = new ArrayList<ClientPair>();
		whiteList = new ArrayList<AllowedUser>();
	}

	/**
	 * Set the white list information for the server. The white list contains a list of valid usernames and passwords with which each client
	 * shall "login" to the server. <br/>
	 * <br/>
	 * The white list file itself is a text file in CSV format like so: <br/>
	 * <i> username1,password1 <br/>
	 * username2,password2 <br/>
	 * ... </i>
	 * 
	 * @param whiteListFileName
	 *            The name of the white list file.
	 * @throws FileNotFoundException
	 */
	public void setWhiteList(String whiteListFileName) throws FileNotFoundException {
		List<AllowedUser> newWhiteList = new ArrayList<>();

		// Read the usernames and passwords and fill the list with AllowedUsers
		Scanner whiteListReader = new Scanner(new File(whiteListFileName));
		while (whiteListReader.hasNextLine()) {
			Scanner lineScanner = new Scanner(whiteListReader.nextLine());
			lineScanner.useDelimiter(",");
			if (lineScanner.hasNext()) {
				String username = lineScanner.next();
				if (lineScanner.hasNext()) {
					String password = lineScanner.next();
					newWhiteList.add(new AllowedUser(username.trim(), password.trim()));
				}
			}
			lineScanner.close();
		}
		whiteListReader.close();

		// Update the whitelist
		whiteList.clear();
		whiteList.addAll(newWhiteList);
	}

	/**
	 * Get the white list of AllowedUsers who are permitted to login to this server
	 * 
	 * @return
	 */
	public ArrayList<AllowedUser> getWhiteList() {
		return whiteList;
	}

	/**
	 * Start the server and establish connections to the given serial ports
	 * 
	 * @param whiteListFileName
	 *            The name of the white list file
	 * @param serialPorts
	 *            The serial port names to which the server shall attach
	 * @throws Exception
	 *             If something goes wrong
	 */
	public void startServer(String whiteListFileName, String... serialPorts) throws Exception {
		if (portNum == -1) {
			throw new IllegalStateException("Must have a valid port number.");
		} else if (serialPorts == null || serialPorts.length == 0) {
			throw new IllegalArgumentException("Must have at least 1 port to connect to .");
		} else {
			NSERIAL_CONNECTIONS = serialPorts.length;
		}

		System.out.println("Number of serial connections=" + NSERIAL_CONNECTIONS + " : " + Arrays.toString(serialPorts));

		// Get the white list information
		try {
			setWhiteList(whiteListFileName);
		} catch (IOException e) {
			System.err.println("Unable to find white list file: " + whiteListFileName);
			System.err.println("Exiting...");
			System.exit(0);
		}

		serialIns = new InputStream[NSERIAL_CONNECTIONS];
		serialOuts = new OutputStream[NSERIAL_CONNECTIONS];

		for (int i = 0; i < serialPorts.length; i++) {
			connect(serialPorts[i], i);
			System.out.println("Connected to: " + serialPorts[i]);
		}

		for (int i = 0; i < serialIns.length; i++) {
			if (serialIns[i] == null) {
				System.err.println("Unable to connect to serial port: " + serialPorts[i]);
				return;
			}
		}

		startServer(portNum);

		System.out.println("Waiting for clients to connect...");

		int clientNum = 0;
		while (true) {
			Socket client = server.accept();
			clientNum++;

			clients.add(new ClientPair(client, clientNum));
			clientIns.add(client.getInputStream());
			clientOuts.add(client.getOutputStream());

			(new Thread(new ClientDataReader(client, this, clientNum))).start();
		}
	}

	/**
	 * Get all of the clients assigned to this server
	 * 
	 * @return All of the server's clients
	 */
	public ArrayList<ClientPair> getClients() {
		return clients;
	}

	/**
	 * Get the serial reader for the given port num
	 * 
	 * @param serialPortNum
	 *            The serial port number
	 * @return The serial reader for that port
	 */
	public SerialDataReader getSerialReader(int serialPortNum) {
		if (serialPortNum < 0 || serialPortNum >= serialReaders.size()) {
			return null;
		}

		return serialReaders.get(serialPortNum);
	}

	/**
	 * Get all of the client output streams
	 * 
	 * @return All of the server's clients' output streams
	 */
	public ArrayList<OutputStream> getClientOuts() {
		return clientOuts;
	}

	/**
	 * Get all of the client input streams
	 * 
	 * @return All of the server's clients' input streams
	 */
	public ArrayList<InputStream> getClientIns() {
		return clientIns;
	}

	/**
	 * Get a client from this server, identified by its client number
	 * 
	 * @param clientNum
	 *            The number of the client to retrieve
	 * @return The socket for that client
	 */
	public Socket getClient(int clientNum) {
		for (ClientPair cp : clients) {
			if (cp.getClientNum() == clientNum) {
				return cp.getClient();
			}
		}

		return null;
	}

	/**
	 * Get the output stream for the client at an index
	 * 
	 * @param idx
	 *            The index of the client in the array list
	 * @return The client's output stream
	 */
	public OutputStream getClientOut(int idx) {
		return clientOuts.get(idx);
	}

	/**
	 * Get the input stream for the client at an index
	 * 
	 * @param idx
	 *            The index of the client in the array list
	 * @return The client's input stream
	 */
	public InputStream getClientIn(int idx) {
		return clientIns.get(idx);
	}

	/**
	 * Close the client identified by its number, severing its connection and removing it from the list of clients
	 * 
	 * @param clientNum
	 *            The client's identification number
	 * @param in
	 *            The client's input stream
	 */
	public void closeClient(int clientNum, InputStream in) {
		try {
			int inIdx = clientIns.indexOf(in);
			clientIns.get(inIdx).close();
			clientOuts.get(inIdx).close();

			clientIns.remove(inIdx);
			clientOuts.remove(inIdx);

			System.out.println("Client #" + clientNum + " has disconnected.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the serial output stream for the given serial port number
	 * 
	 * @param serialPortNum
	 *            The serial port number
	 * @return That serial port's output stream
	 */
	public OutputStream getSerialOut(int serialPortNum) {
		if (serialPortNum >= serialOuts.length) {
			return null;
		}

		return serialOuts[serialPortNum];
	}

	/**
	 * Get the serial input stream for the given serial port number
	 * 
	 * @param serialPortNum
	 *            The serial port number
	 * @return That serial port's input stream
	 */
	public InputStream getSerialIn(int serialPortNum) {
		if (serialPortNum >= serialIns.length) {
			return null;
		}

		return serialIns[serialPortNum];
	}

	/**
	 * Start the server on a given port
	 * 
	 * @param portNum
	 *            The port number to start the server on
	 * @throws IOException
	 *             If something goes wrong
	 */
	private void startServer(int portNum) throws IOException {
		server = new ServerSocket(portNum);
		System.out.println();
		System.out.print("Serial Server started");

		try {
			// Try to get our IP address from Amazon AWS
			URL aws = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(aws.openStream()));
			String ip = in.readLine();
			System.out.println(" on IP: " + ip + ":" + portNum);
		} catch (IOException e) {
			System.out.println(" on port " + portNum);
		}
	}

	/**
	 * Connect to a serial port identified by its name and serial port number
	 * 
	 * @param portName
	 *            The name of the serial port [COM3, /dev/ttyUSB0, etc.]
	 * @param serialPortNum
	 *            The number of the serial port
	 * @throws NoSuchPortException
	 * @throws PortInUseException
	 * @throws UnsupportedCommOperationException
	 * @throws IOException
	 * @throws TooManyListenersException
	 *             If something goes wrong
	 */
	private void connect(String portName, int serialPortNum) throws NoSuchPortException, PortInUseException,
			UnsupportedCommOperationException, IOException, TooManyListenersException {
		if (portName == null) {
			throw new IllegalArgumentException("Port Name must not be null!");
		}

		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		if (portIdentifier.isCurrentlyOwned()) {
			throw new PortInUseException();
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

			if (commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

				// Get the input stream and output stream
				serialIns[serialPortNum] = serialPort.getInputStream();
				serialOuts[serialPortNum] = serialPort.getOutputStream();

				// Start the reader
				SerialDataReader serialReader = new SerialDataReader(serialIns[serialPortNum], this, serialPortNum);
				serialReaders.add(serialReader);
				(new Thread(serialReader)).start();
			}
		}
	}
}
