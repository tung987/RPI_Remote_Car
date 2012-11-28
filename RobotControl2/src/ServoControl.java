import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;

public class ServoControl {

	private Serial serial;

	public ServoControl() {

	}

	public void connect(String portName) throws Exception {
		serial = SerialFactory.createInstance();
		int ret = serial.open(portName, 115200);
        if (ret == -1){
            System.out.println(" ==>> SERIAL SETUP FAILED");
        }
	}

	public void write(String command) {
		System.out.println("sending command \"" + command + "\"");
		
		StringBuilder complateCommand = new StringBuilder();
		complateCommand.append(command).append("\r\n");
		serial.write(complateCommand.toString().getBytes());

	}
	
	public void close(){
		serial.close();
	}

}
