package serial.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import api.TS2000Radio;

public class APITest {
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		String host = "";
		int portNum = 0;
		String username;
		String password;

		// Get the host, port, username, and password, either from the command
		// line arguments or reading from System.in
		if (args.length == 0) {
			Scanner scan = new Scanner(System.in);
			System.out.print("Host: ");
			host = scan.next();
			System.out.print("Port: ");
			portNum = scan.nextInt();
			System.out.print("Username: ");
			username = scan.next();
			System.out.print("Password: ");
			password = scan.next();
			scan.close();
		} else if (args.length == 2) {
			host = args[0];
			portNum = Integer.parseInt(args[1]);

			Scanner scan = new Scanner(System.in);
			System.out.print("Username: ");
			username = scan.next();
			System.out.print("Password: ");
			password = scan.next();
			scan.close();
		} else if (args.length == 4) {
			host = args[0];
			portNum = Integer.parseInt(args[1]);
			username = args[2];
			password = args[3];
		} else {
			System.out.println("Usage: StartSerialClient <host-ip> <host-port> <username> <password>");
			return;
		}

		// Make the client and start the data out thread
		SerialClient client = new SerialClient(host, portNum, username, password, 0);
		client.addListener(new SerialDataListener() {
			@Override
			public void dataReceived(String data) {
				System.out.println("DATA: " + data + " (" + Arrays.toString(data.getBytes()) + ")");
			}
		});
		TS2000Radio radio = new TS2000Radio(client);
		int frequency = -1;
		try {
			Scanner s = new Scanner(System.in);
			while (true) {
				try {
					System.out.print(">");
					String cmd = s.next();
					if (cmd.contains("cm")) {
						int mode = radio.getCurrentMode();
						if (mode == TS2000Radio.PACKET_MODE)
							radio.setMode(TS2000Radio.STATUS_MODE);
						else
							radio.setMode(TS2000Radio.PACKET_MODE);
					} else if (cmd.contains("sa")) {
						int freq = s.nextInt();
						radio.RadioSetFreqA(freq);
					} else if (cmd.contains("sb")) {
						int freq = s.nextInt();
						radio.RadioSetFreqB(freq);
					} else if (cmd.contains("sc")) {
						int freq = s.nextInt();
						radio.RadioSetFreqSub(freq);
					} else if (cmd.contains("a")) {
						frequency = radio.RadioGetFreqA();
						System.out.println("Frequency A: " + frequency);
					} else if (cmd.contains("b")) {
						frequency = radio.RadioGetFreqB();
						System.out.println("Frequency B: " + frequency);
					} else if (cmd.contains("c")) {
						frequency = radio.RadioGetFreqSub();
						System.out.println("Frequency C: " + frequency);
					} else if (cmd.contains("u")) {
						radio.sendSerialMsg("\r\r");
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
