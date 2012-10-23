package radio.server;

import gnu.io.NoSuchPortException;
/**
 * A class that, when run, starts the radio server.
 * @author Adam Campbell
 */
public class StartRadioServer {
	/**
	 * The port number for the server
	 */
	public static final int PORTNUM = 2809;

	/**
	 * The default serial port names
	 */
	public static final String[] defaultPorts = {"COM3", "COM4"};
	
	/**
	 * Start the server
	 * @param args
	 * Command line arguments, optionally the names of the serial ports to which the server should 
	 * connect. If no command line arguments are given, the default serial ports are used.
	 */
	public static void main(final String[] args) {
		try{
			if(args.length > 0){
				new RadioServer(PORTNUM).startServer(args);
			} else{
				new RadioServer(PORTNUM).startServer(defaultPorts);
			}
		} catch (NoSuchPortException e){
			System.err.println("Invalid serial port(s). Exiting...");
			System.exit(0);
		} catch(Exception e){
			e.printStackTrace();
			System.err.println("Error initializing server GUI. Exiting...");
			System.exit(0);
		}
		
	}
}
