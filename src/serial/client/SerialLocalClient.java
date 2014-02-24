package serial.client;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;

/**
 * Implementation of SerialClient for connecting to serial devices locally, without the server
 * 
 * @author Adam Campbell
 */
public class SerialLocalClient extends SerialClient {
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
	public SerialLocalClient(String portName, int baudRate) throws NoSuchPortException, PortInUseException,
			UnsupportedCommOperationException, IOException {
		if (portName == null) {
			throw new IllegalArgumentException("Port Name must not be null!");
		}

		//RXTX stuff
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		if (portIdentifier.isCurrentlyOwned()) {
			throw new PortInUseException();
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

			if (commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

				// Get the input stream and output stream
				in = serialPort.getInputStream();
				out = serialPort.getOutputStream();

				// Start the reader
				SerialLocalDataReader serialReader = new SerialLocalDataReader(in, this, portName);
				(new Thread(serialReader)).start();
				
				state = State.ALIVE;
			}
		}
	}

	@Override
	public void write(String data) throws IOException {
		out.write(data.getBytes());
	}

	@Override
	public void writeVerbatim(String data) throws IOException {
		out.write(data.getBytes());
	}
}
