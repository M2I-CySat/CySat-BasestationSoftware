package radio.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

import radio.RadioUtil;

public class RadioClient {
	private static Socket server = null;
	private static InputStream serverIn;
	private static OutputStream serverOut;

	private static DataInThread dataIn;
	private static DataOutThread dataOut;

	@SuppressWarnings("resource")
	public static void main(String[] args){
		String host = "";
		int portNum = 0;
		try{
			if(args.length != 2){
				Scanner scan = new Scanner(System.in);
				System.out.print("Host: ");
				host = scan.next();
				System.out.print("Port: ");
				portNum = scan.nextInt();
			} else{
				host = args[0];
				portNum = Integer.parseInt(args[1]);
			}
			server = new Socket(host, portNum);

			serverIn = server.getInputStream();
			serverOut = server.getOutputStream();

			System.out.println();
			System.out.println("===== CLIENT READY TO HANDLE MESSAGES =====");
			System.out.println();

			dataIn = new DataInThread(new BufferedReader(new InputStreamReader(serverIn)));
			dataIn.start();

			dataOut = new DataOutThread(serverOut);
			dataOut.start();
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

	private static void die(){
		System.exit(0);
	}

	private static void processSerialDataReceived(String command, String data){
		// System.out.println("DATA RECEIVED: " + data + " [" + data.length() +
		// "]");
		if(command == null){
			System.out.println("Error in command...");
			return;
		}
		command = command.toUpperCase();
		if(command.startsWith("H")){
			System.out.println(data);
		} else if(command.startsWith("C2")){
			if(data.length() >= 10){
				try{
					int azimuth = Integer.parseInt(data.substring(2, 5));
					int elevation = Integer.parseInt(data.substring(7, 10));
					System.out.println("Azimuth: " + String.format("%03d", azimuth));
					System.out.println("Elevation: " + String.format("%03d", elevation));
				} catch(NumberFormatException e){
					System.err.println("Error in C2 serial data received: " + data);
				}
			} else{
				System.err.println("Error in C2 serial data received: " + data);
			}
		} else if(command.startsWith("C")){
			if(data.length() >= 4){
				int azimuth = Integer.parseInt(data.substring(2, 5));
				System.out.println("Azimuth: " + String.format("%03d", azimuth));
			}
		} else{
			System.err.println("Serial data received: " + data + ", from unrecognized command: " + command);
		}
	}

	/*
	 * Reads from server, writes to console
	 */
	private static class DataInThread extends Thread {
		public DataInThread(final BufferedReader in) {
			super(new Runnable(){
				@Override
				public void run(){
					String msg = null;
					try{
						while((msg = in.readLine()) != null){
							if(msg.length() < 3){
								System.err.println("Ignoring invalid message received.");
							} else{
								processSerialDataReceived(msg.substring(0, 2), msg.substring(2));
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

	/*
	 * Reads from console, writes to server
	 */
	private static class DataOutThread extends Thread {
		public DataOutThread(final OutputStream out) {
			super(new Runnable(){
				@Override
				public void run(){
					byte[] buffer = new byte[RadioUtil.BUFFER_SIZE];
					try{
						while(System.in.read(buffer) != -1){
							out.write(buffer);
							RadioUtil.clear(buffer);
						}
					} catch(IOException e){
						System.err.println("Error writing to server! It may not be running. Exiting...");
						die();
					}
				}
			});
		}
	}
}
