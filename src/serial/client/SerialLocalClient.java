package serial.client;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jssc.SerialPort;
import jssc.SerialPortException;
import serial.server.SerialDataReader;

/**
 * Implementation of SerialClient for connecting to serial devices locally, without the server
 * 
 * @author Adam Campbell
 */
public class SerialLocalClient extends SerialClient {
	private SerialPort serialPort = null;
	
	private Thread reader;

	/**
	 * Construct a serial client to connect to the given serial port
	 * 
	 * @param portName
	 *            Serial port name
	 * @param baudRate
	 *            Baud rate
	 * @throws NoSuchPortException
	 * @throws PortInUseException
	 * @throws UnsupportedCommOperationException
	 * @throws IOException
	 */
	public SerialLocalClient(String portName, int baudRate, String delimiters) throws SerialPortException {
		if (portName == null) {
			throw new IllegalArgumentException("Port Name must not be null!");
		}
		
		// //RXTX stuff
		// CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		// if (portIdentifier.isCurrentlyOwned()) {
		// throw new PortInUseException();
		// } else {
		// CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);
		//
		// if (commPort instanceof SerialPort) {
		// SerialPort serialPort = (SerialPort) commPort;
		// serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		//
		// // Get the input stream and output stream
		// in = serialPort.getInputStream();
		// out = serialPort.getOutputStream();
		//
		// // Start the reader
		// SerialLocalDataReader serialReader = new SerialLocalDataReader(in, this, portName);
		// (new Thread(serialReader)).start();
		//
		// state = State.ALIVE;
		// }
		// }

		serialPort = new SerialPort(portName);
		System.out.println("Port opened: " + serialPort.openPort());
		System.out.println("Params set: " + serialPort.setParams(baudRate, 8, 1, 0));
	//	serialPort.writeString("!QUERY,HELLO,A0$");
		out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				try {
					serialPort.writeByte((byte) b);
				} catch (SerialPortException e) {
					e.printStackTrace();
				}
			}
		};
		in = new InputStream() {
			@Override
			public int read() throws IOException {
				try {
					byte[] arr = serialPort.readBytes(1);
					if (arr != null) {
						return arr[0];
					}
					
					return -1;
				} catch (SerialPortException e) {
					throw new IOException(e.getMessage());
				}
			}
		};
		
		reader = new Thread(new SerialDataReader(in, delimiters) {
			@Override
			public void handleSerialDataReceived(String data) throws IOException {
				notifyListeners(data);
			}
		});
		reader.start();
		
		state = State.ALIVE;
	}

	@Override
	public void die() {
		try {
			serialPort.closePort();
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
		super.die();
	}
	
	@Override
	public void write(String data) throws SerialPortException {
		// out.write(data.getBytes());
		serialPort.writeString(data);
	}

	@Override
	public void writeVerbatim(String data) throws IOException {
		out.write(data.getBytes());
	}
}
