package radio.client;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;

import radio.RadioUtil;

/**
 * Run the radio client
 * 
 * @author Adam Campbell
 */
public class StartRadioClient {
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
			System.out.println("Username: ");
			username = scan.next();
			System.out.println("Password: ");
			password = scan.next();
		} else if(args.length == 2){
			host = args[0];
			portNum = Integer.parseInt(args[1]);

			Scanner scan = new Scanner(System.in);
			System.out.println("Username: ");
			username = scan.next();
			System.out.println("Password: ");
			password = scan.next();
		} else if(args.length == 4){
			host = args[0];
			portNum = Integer.parseInt(args[1]);
			username = args[2];
			password = args[3];
		} else{
			System.out.println("Usage: StartRadioClient <host-ip> <host-port> <username> <password>");
			return;
		}

		// Make the client and start the data out thread
		RadioClient client = new RadioClient(host, portNum, username, password);
		new DataOutThread(client).start();

		// Listen for data and process it
		while(true){
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

	/**
	 * Reads from console, writes to server
	 */
	private static class DataOutThread extends Thread {
		public DataOutThread(final RadioClient client) {
			super(new Runnable(){
				@Override
				public void run(){
					OutputStream out = client.getOutputStream();
					byte[] buffer = new byte[RadioUtil.BUFFER_SIZE];
					try{
						// Read from System.in and transmit the command to the
						// server
						while(System.in.read(buffer) != -1){
							// Save the command in prevCommand
							prevCommand = new String(RadioUtil.trimTrailing0s(buffer));

							// Transmit the command and clear the buffer
							out.write(buffer);
							RadioUtil.clear(buffer);
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
			System.out.println("Data from command \"" + command.trim() + "\": " + data);
		}
	}
}
