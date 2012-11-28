import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.OutputStream;

public class ServoControl {

	private SerialPort serialPort;
	private OutputStream out;

	public ServoControl() {

	}

	public void connect(String portName) throws Exception {
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		if (portIdentifier.isCurrentlyOwned()) {
			System.out.println("Error: Port is currently in use");
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);

			if (commPort instanceof SerialPort) {
				serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

				// InputStream in = serialPort.getInputStream();
				out = serialPort.getOutputStream();

				// (new Thread(new SerialReader(in))).start();
				// (new Thread(new SerialWriter(out))).start();

			} else {
				System.out.println("Error: Only serial ports are handled by this example.");
			}
		}
	}

	public void write(String command) {
		System.out.println("Writing \"" + command + "\" to " + serialPort.getName());
		
		try {
			StringBuilder complateCommand = new StringBuilder();
			complateCommand.append(command).append("\r\n");

			out.write(complateCommand.toString().getBytes());
		} catch (IOException e) {
		}
	}
	
	public void close(){
		serialPort.close();
	}

}
