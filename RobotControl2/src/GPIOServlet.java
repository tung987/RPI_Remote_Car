import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

@WebServlet(name = "GPIOServlet", urlPatterns = { "/GPIO.Control" }, loadOnStartup = 1)
public class GPIOServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//for the movement
	private final GpioPinDigitalOutput [] upPin;
	private final GpioPinDigitalOutput [] downPin;
	private final GpioPinDigitalOutput [] leftPin;
	private final GpioPinDigitalOutput [] rightPin;
	
	private final ServoControl servoControl;
	private final String serialPort = "/dev/ttyACM0";
	private boolean isEnableServo = false;
	
	private ExecutorService pwmWorker;
	private MotorPWM motorPWM = null;
		
	public GPIOServlet(){
		GpioPinDigitalOutput pin7 = null;
		GpioPinDigitalOutput pin11 = null;
		GpioPinDigitalOutput pin13 = null;
		GpioPinDigitalOutput pin15 = null;
		
		try{
			GpioController gpio =  GpioFactory.getInstance();
			pin7 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "Movement #1", PinState.LOW);
			pin11 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_11, "Movement #2", PinState.LOW);
			pin13 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_13, "Movement #3", PinState.LOW);
			pin15 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_15, "Movement #4", PinState.LOW);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		upPin = new GpioPinDigitalOutput[]{pin11, pin13};
		downPin = new GpioPinDigitalOutput[]{pin7, pin15};
		leftPin = new GpioPinDigitalOutput[]{pin11, pin15};
		rightPin = new GpioPinDigitalOutput[]{pin7, pin13};
		
		if(isEnableServo){
			servoControl = new ServoControl();
			try {
				servoControl.connect(serialPort);
				servoControl.write("#1P1900T1000#2P1500T1000");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}else{
			servoControl = null;
		}
		
		pwmWorker = Executors.newSingleThreadExecutor();		
		doShutDownWork();
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String type = req.getParameter("type");
		String motion  = req.getParameter("motion");

		boolean isSuccess = false;
		
		if(type != null && type.equals("MOVE")){
			motorReset();
			if(motion != null && motion.equals("UP")){
				motorUp();
				isSuccess = true;
			}else if(motion != null && motion.equals("DOWN")){
				motorDown();
				isSuccess = true;
			}else if(motion != null && motion.equals("LEFT")){
				motorLeft();
				isSuccess = true;
			}else if(motion != null && motion.equals("RIGHT")){
				motorRight();
				isSuccess = true;
			}else if(motion != null && motion.equals("RESET")){
				isSuccess = true;
			}
		}else if(type != null && type.equals("PWM_MOVE")){
			int speed = 0;
			try{speed = Integer.parseInt(req.getParameter("speed"));}catch(Exception e){}
			
			motorPWMReset();
			if(motion != null && motion.equals("UP")){
				motorPWMUp(speed);
				isSuccess = true;
			}else if(motion != null && motion.equals("DOWN")){
				motorPWMDown(speed);
				isSuccess = true;
			}else if(motion != null && motion.equals("LEFT")){
				motorPWMLeft(speed);
				isSuccess = true;
			}else if(motion != null && motion.equals("RIGHT")){
				motorPWMRight(speed);
				isSuccess = true;
			}else if(motion != null && motion.equals("RESET")){
				isSuccess = true;
			}
		}else if(type != null && type.equals("SERVO")){
			if(servoControl != null)
				servoControl.write("#" + motion);
		}
		
		
		PrintWriter out = resp.getWriter();
		out.println(isSuccess ? "SUCCESS" : "FAIL");
		out.close();
	}
	
	private void doShutDownWork() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				motorReset();
				if(servoControl != null)
					servoControl.close();
			}
		});
	}
	
	private void motorUp() {
		System.out.println("MOTOR MOVE UP");
		motorPWMUp(100);
	}

	private void motorDown() {
		System.out.println("MOTOR MOVE DOWN");
		motorPWMDown(100);
	}

	private void motorLeft() {
		System.out.println("MOTOR MOVE LEFT");
		motorPWMLeft(100);
	}

	private void motorRight() {
		System.out.println("MOTOR MOVE RIGHT");
		motorPWMRight(100);
	}

	private void motorReset() {
		System.out.println("MOTOR MOVE RESET");
		motorPWMReset();
	}
	
	
	//PWM motor control
	private void motorPWMUp(int speed) {
		System.out.println("PWM MOTOR MOVE UP");
		motorPWMReset();
		motorPWM = new MotorPWM(upPin, speed);
		pwmWorker.execute(motorPWM);
	}

	private void motorPWMDown(int speed) {
		System.out.println("PWM MOTOR MOVE DOWN");	
		motorPWMReset();
		motorPWM = new MotorPWM(downPin, speed);
		pwmWorker.execute(motorPWM);
	}

	private void motorPWMLeft(int speed) {
		System.out.println("PWM MOTOR MOVE LEFT");		
		motorPWMReset();
		motorPWM = new MotorPWM(leftPin, speed);
		pwmWorker.execute(motorPWM);
	}

	private void motorPWMRight(int speed) {
		System.out.println("PWM MOTOR MOVE RIGHT");		
		motorPWMReset();
		motorPWM = new MotorPWM(rightPin, speed);
		pwmWorker.execute(motorPWM);
	}

	private void motorPWMReset() {
		System.out.println("PWM MOTOR MOVE RESET");
		if(motorPWM != null){
			motorPWM.setStop();
			motorPWM = null;
		}
	}
}
