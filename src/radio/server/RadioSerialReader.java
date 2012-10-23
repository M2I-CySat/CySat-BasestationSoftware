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
	public void handleSerialDataReceived(String data) throws IOException {
		//Back up the data locally
		RadioUtil.backupData(data, RadioUtil.getJarDirectory() + "Data-Logs/Serial-Data/Port-" + serialPortNum + "/");

		if(CLIENT_NUM > 0 && server.getClient(CLIENT_NUM) != null){
			server.getClient(CLIENT_NUM).getOutputStream().write((data + "\n").getBytes());
		} else{
			System.out.println("INVALID CLIENT NUMBER: " + CLIENT_NUM);
		}
	}
	
	@Override
	public void run(){
		boolean keepRunning = true;
		BufferedReader br = new BufferedReader(new InputStreamReader(serialIn));
		String line = null;
		while(keepRunning){
			try{
				while((line = br.readLine()) != null){
					if(RadioServer.LAST_COMMAND != null){
						String data = RadioServer.LAST_COMMAND + line;
						handleSerialDataReceived(data);
						Thread.sleep(100);
					}
				}
			} catch(IOException e){
				try{
					//The stream is empty
					Thread.sleep(100);
				} catch(InterruptedException ex){
					keepRunning = false;
				}
			} catch(InterruptedException e){
				keepRunning = false;
			}
		}
	}
}