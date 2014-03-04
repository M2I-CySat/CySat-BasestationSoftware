package serial;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import serial.server.SerialServer;

/**
 * Utilities for the serial server & client
 * 
 * @author Adam Campbell
 */
public class SerialUtils {
	/**
	 * The server's response when the user login is successful
	 */
	public static final String VALID_USER_MESSAGE = "GOOD";

	/**
	 * The server's response when the user login is not successful
	 */
	public static final String INVALID_USER_MESSAGE = "BAD";

	/**
	 * The size of the buffer to be used for reading/writing
	 */
	public static final int BUFFER_SIZE = 1024;
	
	/**
	 * Default amount of time to wait for a serial response
	 */
	public static final int DEFAULT_SERIAL_RESPONSE_TIMEOUT = 500;

	/**
	 * Running this class lists all of the communiation ports available
	 */
	public static void main(String[] args) {
		CommunicationPortUtil.listPorts();
	}

	/**
	 * Get the directory where the jar file is stored on the computer
	 * 
	 * @return The jar file's directory on the computer
	 */
	public static String getJarDirectory() {
		String path = SerialServer.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		try {
			String dir = URLDecoder.decode(path, "UTF-8");

			// Get rid of the jar file ending (this happens if we run the server from an .exe)
			if (dir.endsWith(".jar")) {
				dir = dir.substring(0, dir.lastIndexOf('/'));
			}

			// Make sure there's a directory separator
			if (!dir.endsWith("/")) {
				dir += "/";
			}

			return dir;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * Backup an array of byte data to a file. The data will be appended to the log file, and if the file does not exist, it shall be
	 * created. The file name will be <i>logBaseString/yyyy-MM-dd.txt</i>, where yyyy-MM-dd is the date at which the file is created.
	 * 
	 * @param buffer
	 *            The byte data to be appended to the file
	 * @param logBaseString
	 *            The name of the file base directory.
	 */
	public static void backupData(String data, String logBaseString) {
		// //Get a timestamp to attach to the message (in local time)
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		// String timeStamp = sdf.format(new Date());

		// Get a timestamp from the log entry, so that it can be put in the
		// appropriate log file
		SimpleDateFormat logsdf = new SimpleDateFormat("yyyy-MM-dd");
		logsdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String logTimeStamp = logsdf.format(new Date());

		// Get a text log and append the message to it
		BufferedWriter out;
		File logDir = new File(logBaseString);
		if (!logDir.exists() || !logDir.isDirectory()) {
			logDir.mkdirs();
		}
		try {
			out = new BufferedWriter(new FileWriter(new File(logBaseString + logTimeStamp + ".txt"), true));
			if (data.length() > 0) {
				out.write(data.trim() + "\r\n");
			}
			out.close();
		} catch (Exception e) {
			System.err.println("Error writing to log file!");
			e.printStackTrace();
		}
	}
}
