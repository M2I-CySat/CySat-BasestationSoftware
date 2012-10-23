package radio.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;

import radio.RadioUtil;
import radio.server.RadioServer;

/*
 * Reads from client, writes to console
 */
public class ClientDataReader implements Runnable {
	private InputStream in;
	private RadioServer server;
	private int clientNum;

	public ClientDataReader(InputStream in, RadioServer server, int clientNum) {
		this.in = in;
		this.server = server;
		this.clientNum = clientNum;
	}
	
	private void processCommand(String cmd, int serialPortNum) throws IOException{
//		System.out.println("ddd: " + Arrays.toString(cmd.getBytes()));
		server.getSerialOut(serialPortNum).write(cmd.getBytes());
	}

	private void handleClientDataReceived(byte[] buffer, int serialPortNum) throws IOException{
		if(serialPortNum < 0 || serialPortNum > 9){
			return;
		}
		
		String msg = new String(RadioUtil.trimTrailing0s(buffer));
		
//		if(!msg.endsWith("\r\n")){
//			msg = msg.substring(0, msg.length() - 1) + "\r\n";
//		}

		RadioUtil.backupData(buffer, RadioUtil.getJarDirectory() + "Data-Logs/Client-Data/");
		if(server.getSerialOut(serialPortNum) != null){
//			server.getSerialOut(serialPortNum).write(buffer);
//			ClientTextArea.processMessage("Received from client: " + msg);
			processCommand(msg, serialPortNum);
			System.out.println("Received from client #" + clientNum + ": " + msg);
			if(server.getSerialReader(serialPortNum) != null){
				server.getSerialReader(serialPortNum).CLIENT_NUM = clientNum;
			} else{
				System.out.println("INVALID SERIAL PORT NUMBER: " + serialPortNum);
			}
		} else{
			System.err.println("Ignoring message with invalid serial destination: " + serialPortNum);
			return;
		}
	}

	@Override
	public void run(){
		byte[] buffer = new byte[RadioUtil.BUFFER_SIZE];
		try{
			while(in.read(buffer) != -1){
//				System.out.println("HELLO!");
//				if(buffer[0] == HeartBeatGenerator.HEARTBEAT_VALUE){
//					long lastHeartBeatTime = System.currentTimeMillis();
//					System.out.println("Got heartbeat #" + clientNum + 
//										" @" + lastHeartBeatTime/1000);
//					continue;
//				}
				
				String msg = new String(buffer, 0, RadioUtil.trimTrailing0s(buffer).length);
				if(msg.length() > 0){
					int windows = msg.lastIndexOf("\r\n");
					int unix = msg.lastIndexOf("\n");
					if(windows > -1){
						msg = msg.substring(0, windows);
					} else if(unix > -1){
						msg = msg.substring(0, unix);
					}
					
					msg += '\r';
					
//					System.out.println("MESSAGE: " + Arrays.toString(msg.getBytes()) + "(" + msg + ")");
					int serialPortNum = -1;

					if(Character.isDigit(msg.charAt(0))){
						serialPortNum = (int) (msg.charAt(0) - '0'); 
					} else{
						System.err.println("Ignoring message without specified serial destination.");
					}

					//Get just the base part of the command
					String cmd = msg.substring(1);
					while(cmd.length() < 2){
						cmd += ' '; 	
					}
					cmd = cmd.substring(0, 2);
					RadioServer.LAST_COMMAND = cmd;
					
					handleClientDataReceived(msg.substring(1).getBytes(), serialPortNum);
					RadioUtil.clear(buffer);
				}
			}
			
			server.closeClient(clientNum, in);
		} catch(SocketTimeoutException e){
			
		} catch(Exception e){
			System.err.println("Error reading from client!");
			e.printStackTrace();
		}
	}
}
