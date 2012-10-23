package radio.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import radio.RadioUtil;

/**
 * A reader that sits and waits for serial data to come in and then ships it off to the server's clients
 * @author Adam Campbell
 */
public class RadioSerialReader implements Runnable {
	/**
	 * The serial input stream
	 */
	private InputStream serialIn;
	
	/**
	 * The server to which this reader belongs
	 */
	private RadioServer server;
	
	/**
	 * The serial port number
	 */
	private int serialPortNum;
	
	/**
	 * The client number to respond to
	 */
	public int CLIENT_NUM = -1;

	/**
	 * Construct a radio serial reader that reads from the given serial port, and outputs to
	 * the given server with the given serial port number
	 * @param serialIn
	 * The input stream for the serial port
	 * @param server
	 * The server that this reader belongs to
	 * @param serialPortNum
	 * The serial port number
	 */
	public RadioSerialReader(InputStream serialIn, RadioServer server, int serialPortNum) {
		this.serialIn = serialIn;
		this.server = server;
		this.serialPortNum = serialPortNum;
	}

	/**
	 * Handle the serial data received and ship it off to the server's clients
	 * @param data
	 * The serial data received
	 * @throws IOException 
	 * If something goes wrong writing to the server's clients
	 */
	public void handleSerialDataReceived(byte[] data) throws IOException {
		//Back up the data locally
		RadioUtil.backupData(data, RadioUtil.getJarDirectory() + "Data-Logs/Serial-Data/Port-" + serialPortNum + "/");

		//Send it to each of the server's clients
//		for(OutputStream clientOut : server.getClientOuts()){
//			if(clientOut != null){
//				clientOut.write(data);
//			}
//		}
		if(CLIENT_NUM > 0 && server.getClient(CLIENT_NUM) != null){
			server.getClient(CLIENT_NUM).getOutputStream().write(data);
		} else{
			System.out.println("INVALID CLIENT NUMBER: " + CLIENT_NUM);
		}
	}
	
	@Override
	public void run(){
		boolean keepRunning = true;
		BufferedReader br = new BufferedReader(new InputStreamReader(serialIn));
		String line;
		while(keepRunning){
			try{
				while((line = br.readLine()) != null){
					if(RadioServer.LAST_COMMAND != null){
						String data = RadioServer.LAST_COMMAND + line;
						handleSerialDataReceived(data.getBytes());
						Thread.sleep(100);
					}
				}
			} catch(IOException e){
				try{
					// ignore it, the stream is temporarily empty,RXTX's
					// just whining
					Thread.sleep(100);
				} catch(InterruptedException ex){
					// something interrupted our sleep, exit ...
					keepRunning = false;
				}
			} catch(InterruptedException e){
				keepRunning = false;
			}
		}
	}
}