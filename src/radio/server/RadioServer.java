package radio.server;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.TooManyListenersException;

import radio.client.ClientDataReader;

/**
 * The server that listens to the serial port and echoes the serial data back to
 * the clients, as well as listen for data from the clients and send it to the
 * serial ports.
 * 
 * @author Adam Campbell
 */
public class RadioServer {
	/**
	 * The most recent command issued to the serial ports
	 */
	public static String LAST_COMMAND = null;

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
	 * The serial input streams used for reading data from the radios
	 */
	private InputStream[] serialIns;

	/**
	 * The serial output streams used for writing data to the radios
	 */
	private OutputStream[] serialOuts;

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

		public int getClientNum(){
			return clientNum;
		}

		public Socket getClient(){
			return client;
		}
	}

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
	private ArrayList<RadioSerialReader> serialReaders = new ArrayList<RadioSerialReader>();

	/**
	 * Construct a new Radio Server on the given port
	 * 
	 * @param portNum
	 *            The port that the server is assigned to
	 */
	public RadioServer(int portNum) {
		this.portNum = portNum;
		clientIns = new ArrayList<InputStream>();
		clientOuts = new ArrayList<OutputStream>();
		clients = new ArrayList<ClientPair>();
	}

	/**
	 * Start the server and establish connections to the given serial ports
	 * 
	 * @param serialPorts
	 *            The serial port names to which the server shall attach
	 * @throws Exception
	 *             If something goes wrong
	 */
	public void startServer(String... serialPorts) throws Exception{
		if(portNum == -1){
			throw new IllegalStateException("Must have a valid port number.");
		} else if(serialPorts == null || serialPorts.length == 0){
			throw new IllegalArgumentException("Must have at least 1 port to connect to .");
		} else{
			NSERIAL_CONNECTIONS = serialPorts.length;
		}

		serialIns = new InputStream[NSERIAL_CONNECTIONS];
		serialOuts = new OutputStream[NSERIAL_CONNECTIONS];

		for(int i = 0; i < serialPorts.length; i++){
			connect(serialPorts[i], i);
			System.out.println("Connected to: " + serialPorts[i]);
		}

		for(int i = 0; i < serialIns.length; i++){
			if(serialIns[i] == null){
				System.err.println("Unable to connect to serial port: " + serialPorts[i]);
				return;
			}
		}

		startServer(portNum);

		System.out.println("Waiting for clients to connect...");

		int clientNum = 0;
		while(true){
			Socket client = server.accept();
			clientNum++;

			clients.add(new ClientPair(client, clientNum));
			clientIns.add(client.getInputStream());
			clientOuts.add(client.getOutputStream());
			System.out.println();
			System.out.println("Connected to client #" + clientNum + ": " + client.getRemoteSocketAddress());
			System.out.println("===== SERVER READY TO HANDLE MESSAGES =====");
			System.out.println();

			(new Thread(new ClientDataReader(client.getInputStream(), this, clientNum))).start();
		}
	}

	/**
	 * Get all of the clients assigned to this server
	 * 
	 * @return All of the server's clients
	 */
	public ArrayList<ClientPair> getClients(){
		return clients;
	}

	/**
	 * Get the serial reader for the given port num
	 * 
	 * @param serialPortNum
	 *            The serial port number
	 * @return The serial reader for that port
	 */
	public RadioSerialReader getSerialReader(int serialPortNum){
		if(serialPortNum < 0 || serialPortNum >= serialReaders.size()){
			return null;
		}

		return serialReaders.get(serialPortNum);
	}

	/**
	 * Get all of the client output streams
	 * 
	 * @return All of the server's clients' output streams
	 */
	public ArrayList<OutputStream> getClientOuts(){
		return clientOuts;
	}

	/**
	 * Get all of the client input streams
	 * 
	 * @return All of the server's clients' input streams
	 */
	public ArrayList<InputStream> getClientIns(){
		return clientIns;
	}

	/**
	 * Get a client from this server, identified by its client number
	 * 
	 * @param clientNum
	 *            The number of the client to retrieve
	 * @return The socket for that client
	 */
	public Socket getClient(int clientNum){
		for(ClientPair cp : clients){
			if(cp.getClientNum() == clientNum){
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
	public OutputStream getClientOut(int idx){
		return clientOuts.get(idx);
	}

	/**
	 * Get the input stream for the client at an index
	 * 
	 * @param idx
	 *            The index of the client in the array list
	 * @return The client's input stream
	 */
	public InputStream getClientIn(int idx){
		return clientIns.get(idx);
	}

	/**
	 * Close the client identified by its number, severing its connection and
	 * removing it from the list of clients
	 * 
	 * @param clientNum
	 *            The client's identification number
	 * @param in
	 *            The client's input stream
	 */
	public void closeClient(int clientNum, InputStream in){
		try{
			int inIdx = clientIns.indexOf(in);
			clientIns.get(inIdx).close();
			clientOuts.get(inIdx).close();

			clientIns.remove(inIdx);
			clientOuts.remove(inIdx);

			System.out.println("Client #" + clientNum + " has disconnected.");
		} catch(IOException e){
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
	public OutputStream getSerialOut(int serialPortNum){
		if(serialPortNum >= serialOuts.length){
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
	public InputStream getSerialIn(int serialPortNum){
		if(serialPortNum >= serialIns.length){
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
	private void startServer(int portNum) throws IOException{
		server = new ServerSocket(portNum);
		System.out.println();
		System.out.println("Radio Server started on IP: " + server.getLocalSocketAddress());
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
			UnsupportedCommOperationException, IOException, TooManyListenersException{
		if(portName == null){
			throw new IllegalArgumentException("Port Name must not be null!");
		}

		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		if(portIdentifier.isCurrentlyOwned()){
			System.out.println("Error: Port is currently in use");
		} else{
			CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

			if(commPort instanceof SerialPort){
				SerialPort serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
						SerialPort.PARITY_NONE);

				// InputStream in = serialPort.getInputStream();
				serialIns[serialPortNum] = serialPort.getInputStream();
				serialOuts[serialPortNum] = serialPort.getOutputStream();

				RadioSerialReader serialReader = new RadioSerialReader(serialIns[serialPortNum], this, serialPortNum);
				serialReaders.add(serialReader);
				(new Thread(serialReader)).start();
			} else{
				System.out.println("Error: Only serial ports are handled by this example.");
			}
		}
	}
}
