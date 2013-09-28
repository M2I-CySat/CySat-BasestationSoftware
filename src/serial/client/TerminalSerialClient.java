package serial.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Scanner;

/**
 * A terminal application to communicate with the serial server
 * 
 * @author Adam Campbell
 */
public class TerminalSerialClient {
	/**
	 * The command that was just sent
	 */
	private static String prevCommand;

	@SuppressWarnings("resource")
	public static void main(String[] args){
		String host = "";
		int portNum = 0;
		String username;
		String password;

		// Get the host, port, username, and password, either from the command
		// line arguments or reading from System.in
		if(args.length == 0){
			Scanner scan = new Scanner(System.in);
			System.out.print("Host: ");
			host = scan.next();
			System.out.print("Port: ");
			portNum = scan.nextInt();
			System.out.print("Username: ");
			username = scan.next();
			System.out.print("Password: ");
			password = scan.next();
		} else if(args.length == 2){
			host = args[0];
			portNum = Integer.parseInt(args[1]);

			Scanner scan = new Scanner(System.in);
			System.out.print("Username: ");
			username = scan.next();
			System.out.print("Password: ");
			password = scan.next();
		} else if(args.length == 4){
			host = args[0];
			portNum = Integer.parseInt(args[1]);
			username = args[2];
			password = args[3];
		} else{
			System.out.println("Usage: StartSerialClient <host-ip> <host-port> <username> <password>");
			return;
		}

		// Make the client and start the data out thread
		SerialClient client = new SerialClient(host, portNum, username, password);
		if(client.getState() == SerialClient.State.ALIVE){
			new DataOutThread(client).start();
	
			// Listen for data and process it
			while(client.getState() == SerialClient.State.ALIVE){
				while(client.hasData()){
					processSerialDataReceived(prevCommand, client.getData());
				}
	
				try{
					Thread.sleep(100);
				} catch(InterruptedException e){
					client.die();
				}
			}
		}
		
		System.exit(0);
	}
	
	/**
	 * Reads from console, writes to server
	 */
	private static class DataOutThread extends Thread {
		public DataOutThread(final SerialClient client) {
			super(new Runnable(){
				@Override
				public void run(){
					OutputStream out = client.getOutputStream();
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					String line = null;
					try{
						// Read from System.in and transmit the command to the server
						while(client.getState() == SerialClient.State.ALIVE && (line = br.readLine()) != null){
							// Remove the line ending from the message
							int windows = line.lastIndexOf("\r\n");
							int unix = line.lastIndexOf("\n");
							if(windows > -1){
								line = line.substring(0, windows);
							} else if(unix > -1){
								line = line.substring(0, unix);
							}
							
							// Add a single '\r' to the end of the message (so the
							// Yaesu antenna rotator recognizes it)
//							line += "\\r";
							
							// Save the command in prevCommand
							prevCommand = line;

							// Transmit the command and clear the buffer
							out.write((line + "\n").getBytes());
						}
					} catch(IOException e){
						System.err.println("Error writing to server! It may not be running. Exiting...");
						client.die();
					}
				}
			});
		}
	}

	/**
	 * Handle the serial data that's received
	 * 
	 * @param command
	 * @param data
	 */
	private static void processSerialDataReceived(String command, String data){
		if(command == null || command.isEmpty()){
			System.out.println(data);
		} else{
			System.out.println("Data from command \"" + command.trim() + "\": " + data + " [" + Arrays.toString(data.getBytes()) + "]");
		}
	}
}
