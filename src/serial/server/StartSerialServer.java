package serial.server;

import gnu.io.NoSuchPortException;

/**
 * A class that, when run, starts the serial server.
 * 
 * @author Adam Campbell
 */
public class StartSerialServer {
	/**
	 * The port number for the server
	 */
	public static final int PORTNUM = 2809;

	/**
	 * The default serial port names
	 */
	public static final String[] defaultPorts = { "COM4", "COM5" };

	/**
	 * The white list file location
	 */
	public static final String WHITE_LIST = "whitelist.txt";

	/**
	 * Start the server
	 * 
	 * @param args
	 *            Command line arguments, optionally the names of the serial ports to which the server should connect. If no command line
	 *            arguments are given, the default serial ports are used.
	 */
	public static void main(final String[] args) {
		try {
			SerialServer server = new SerialServer(PORTNUM);
			if (args.length > 0) {
				server.startServer(WHITE_LIST, args);
			} else {
				server.startServer(WHITE_LIST, defaultPorts);
			}
		} catch (NoSuchPortException e) {
			System.err.println("Invalid serial port(s). Exiting...");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
