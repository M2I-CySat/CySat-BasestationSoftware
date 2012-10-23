package radio.client;

import java.io.IOException;
import java.io.InputStream;
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
	
	private static String command;

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
				// System.out.println(host + " ::: " + portNum);
				// server = new Socket("localhost", RadioServerTest.PORTNUM);
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

			dataIn = new DataInThread(serverIn);
			dataIn.start();
			
			dataOut = new DataOutThread(serverOut);
			dataOut.start();
		} catch(ConnectException e){
			System.err.println("Unable to connect to " + host + ":" + portNum + ". Exiting...");
			System.exit(0);
		} catch(SocketException e){
			System.err.println();
			System.err.println("Server Connection Reset. Exiting...");
			System.exit(0);
		} catch(Exception e){
			e.printStackTrace();
			System.err.println("Radio Client Error. Exiting...");
			System.exit(0);
		}
	}

	private static void die(){
		dataIn.interrupt();
		dataOut.interrupt();
	}
	
	private static void processSerialDataReceived(String cmd, String data){
//		System.out.println("DATA RECEIVED: " + data + " [" + data.length() + "]");
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
				} catch (NumberFormatException e){
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
		}/* else if(command.startsWith("W")){
			if(data.length() > 10){
				try{
					int timeStepValue = Integer.parseInt(data.substring(1, 4));
					int horzAngle = Integer.parseInt(data.substring(5, 8));
					int elevationAngle = Integer.parseInt(data.substring(9, 12));
					System.out.println("Time Step Value: " + String.format("%03d", timeStepValue));
					System.out.println("Horizontal Angle: " + String.format("%03d", horzAngle));
					System.out.println("Elevation Angle: " + String.format("%03d", elevationAngle));
				} catch (NumberFormatException e){
					System.err.println("Error in W serial data received: " + data);
				}
			} else{
				try{
					int azimuth = Integer.parseInt(data.substring(1, 4));
					int elevation = Integer.parseInt(data.substring(5, 8));
					System.out.println();
					System.out.println("Azimuth: " + String.format("%03d", azimuth));
					System.out.println("Elevation: " + String.format("%03d", elevation));
					System.out.println();
				} catch (NumberFormatException e){
					System.err.println("Error in W serial data received: " + data);
				}
			}
		} */else{
			System.err.println("Serial data received: " + data + ", from unrecognized command: " + command);
		}
	}

	/*
	 * Reads from server, writes to console
	 */
	private static class DataInThread extends Thread {
		public DataInThread(final InputStream in) {
			super(new Runnable(){
				@Override
				public void run(){
					byte[] buffer = new byte[RadioUtil.BUFFER_SIZE];
					// System.out.println("READING: ");
					try{
						while(in.read(buffer) != -1){
							
							// System.out.println("Read from server: " +
							// Arrays.toString(RadioUtil.trimTrailing0s(buffer)));
							String msg = new String(buffer, 0, RadioUtil.trimTrailing0s(buffer).length);

							RadioUtil.clear(buffer);

							int windows = msg.lastIndexOf("\r\n");
							int unix = msg.lastIndexOf("\n");

							if(windows != -1){
								msg = msg.substring(0, windows);
							} else if(unix != -1){
								msg = msg.substring(0, unix);
							}

//							int serialPortNum = -1;
//							try{
//								if(msg.matches("^[0-9].*$")){
//									serialPortNum = Integer.parseInt(msg.substring(0, 0));
//									serialPortNum = (int) (msg.charAt(0) - '0');
//									if(serialPortNum >= 0 && serialPortNum < 9){
//										System.out.println("Received from server[" + serialPortNum + "]: "
//												+ msg.substring(2));
										processSerialDataReceived(msg.substring(0,2), msg.substring(2));
//										continue;
//									}
//								}
//							} catch(Exception e){
//								e.printStackTrace();
//							}

//							System.out.println("Ignoring server message without source serial port.");
						}

						die();
					}  catch(SocketException e){
						System.err.println();
						System.err.println("Server Connection Reset. Reconnect needed.");
					}  catch(Exception e){
						System.err.println("Error reading from server!");
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
							String msg = new String(buffer, 0, RadioUtil.trimTrailing0s(buffer).length);
							command = msg.substring(1);
							
							out.write(buffer);
							RadioUtil.clear(buffer);
						}
					} catch(IOException e){
						System.err.println("Error writing to server! It may not be running. Exiting...");
						System.exit(0);
					}
				}
			});
		}
	}
}
