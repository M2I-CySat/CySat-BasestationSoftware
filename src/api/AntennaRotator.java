package api;

import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

import serial.client.SerialClient;
import serial.client.SerialDataListener;

/**
 * An API for the Antenna Rotator. We are using the Yaesu GS-232A
 * 
 * @author Adam Campbell
 */
public class AntennaRotator {
	/**
	 * The SerialClient used to talk to the rotator via the serial port
	 */
	private SerialClient client;

	/**
	 * The timeout time in milliseconds for rotator communication
	 */
	public static final int TIMEOUT_TIME = 5000;

	/**
	 * The azimuth of the antenna, in degress
	 */
	private int azimuth = -1;

	/**
	 * The elevation of the antenna, in degrees
	 */
	private int elevation = -1;

	/**
	 * Construct a new antenna rotator to talk to the given client
	 * 
	 * @param client
	 *            The SerialClient used to talk to the rotator via the serial port
	 */
	public AntennaRotator(SerialClient client) {
		this.client = client;
	}

	/**
	 * Rotate the antenna to a given azimuth and elevation. Valid ranges are:
	 * <ul>
	 * <li><b>[0, 360]</b> for the azimuth
	 * <li><b>[0, 180]</b> for the elevation
	 * </ul>
	 * 
	 * @param azimuth
	 *            The azimuth value
	 * @param elevation
	 *            The elevation value
	 * @throws IOException
	 *             In the event of a read/write error
	 */
	public void rotateTo(int azimuth, int elevation) throws IOException {
		if (azimuth < 0 || azimuth > 360) {
			throw new IllegalArgumentException("Invalid Azimuth value!");
		} else if (elevation < 0 || elevation > 180) {
			throw new IllegalArgumentException("Invalid Elevation value!");
		}

		String cmd = String.format("%dW%03d %03d\\r\n", client.getSerialPortNum(), azimuth, elevation);
		client.write(cmd);
	}

	/**
	 * Rotate the antenna to a given azimuth. Valid ranges are:
	 * <ul>
	 * <li><b>[0, 360]</b> for the azimuth
	 * </ul>
	 * 
	 * @param azimuth
	 *            The azimuth value
	 * @throws IOException
	 *             In the event of a read/write error
	 */
	public void rotateToAzimuth(int azimuth) throws IOException {
		if (azimuth < 0 || azimuth > 360) {
			throw new IllegalArgumentException("Invalid Azimuth value!");
		}

		String cmd = String.format("%dW%03d\\r\n", client.getSerialPortNum(), azimuth, elevation);
		client.write(cmd);
	}

	/**
	 * Gets the current azimuth value for the antenna rotator. This method <i>does not poll the server</i> for an up to date azimuth value,
	 * but merely returns the value that was stored during the last server poll. Because of this, you must call pollServer() if you wish to
	 * get an up to date azimuth value.
	 * 
	 * @return The azimuth value recorded during the last call to pollServer()
	 */
	public int getCurrentAzimuth() {
		if (azimuth < 0) {
			throw new IllegalStateException("You must poll the server before getting the azimuth!");
		}

		return azimuth;
	}

	/**
	 * Gets the current elevation value for the antenna rotator. This method <i>does not poll the server</i> for an up to date elevation
	 * value, but merely returns the value that was stored during the last server poll. Because of this, you must call pollServer() if you
	 * wish to get an up to date elevation value.
	 * 
	 * @return The elevation value recorded during the last call to pollServer()
	 */
	public int getCurrentElevation() {
		if (elevation < 0) {
			throw new IllegalStateException("You must poll the server before getting the elevation!");
		}

		return elevation;
	}

	/**
	 * Poll the server to update the azimuth and elevation values. This will send a "C2" command to the antenna rotator, and the response
	 * will be parsed to obtain the azimuth and elevation values.
	 * 
	 * @throws IOException
	 *             In the event of a read/write error
	 */
	public void pollServer() throws IOException {
		final StringBuffer data = new StringBuffer();
		SerialDataListener serialListener = new SerialDataListener() {
			@Override
			public void dataReceived(String serialData) {
				data.append(serialData);
			}
		};
		client.addListener(serialListener);

		// Send the C2 command
		String cmd = String.format("%dC2\\r\n", client.getSerialPortNum(), azimuth, elevation);
		client.write(cmd);

		long startTime = System.currentTimeMillis();
		// Get the response and parse it
		while (data.toString().isEmpty() && System.currentTimeMillis() - startTime < TIMEOUT_TIME) {
			// Wait a little bit
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		client.removeListener(serialListener);

		if (!data.toString().isEmpty()) {
			Scanner dataScanner = new Scanner(data.toString());

			// The data comes in as "+AAA+EEE"
			Pattern dataPattern = Pattern.compile("\\+([0-9]{4})\\+([0-9]{4})");
			if (dataScanner.hasNext(dataPattern)) {
				try {
					azimuth = Integer.parseInt(dataScanner.match().group(1));
					elevation = Integer.parseInt(dataScanner.match().group(2));
				} catch (Exception e) {
					// If we did something wrong with the pattern
					azimuth = -1;
					elevation = -1;
				}
			} else {
				// If the response is malformed
				azimuth = -1;
				elevation = -1;
			}
			dataScanner.close();
		} else {
			// If the read request timed out
			azimuth = -1;
			elevation = -1;
		}
	}

	public static void main(String[] args) {
		SerialClient client = new SerialClient("10.24.223.192", 2809, "joe", "password23", 0);
		if (client.getState() == SerialClient.State.ALIVE) {
			AntennaRotator r = new AntennaRotator(client);
			try {
				r.pollServer();
				System.out.println("Az: " + r.getCurrentAzimuth() + ", El: " + r.getCurrentElevation());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.exit(0);
	}
}
