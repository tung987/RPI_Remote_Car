import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import be.doubleyouit.raspberry.gpio.Boardpin;
import be.doubleyouit.raspberry.gpio.Direction;
import be.doubleyouit.raspberry.gpio.GpioGateway;
import be.doubleyouit.raspberry.gpio.impl.GpioGatewayImpl;

@WebServlet(name = "GPIOServlet", urlPatterns = { "/GPIO.Control" }, loadOnStartup = 1)
public class GPIOServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final GpioGateway gpio;
	private final ServoControl servoControl;
	private final String serialPort = "/dev/ttyACM0";
	private boolean isEnableServo = true;
	
	private ExecutorService pwmWorker;
	private MotorPWM motorPWM = null;
	
	public GPIOServlet(){
		gpio = new GpioGatewayImpl();
		gpio.setup(Boardpin.PIN7_GPIO4, Direction.OUT);
		gpio.setup(Boardpin.PIN11_GPIO17, Direction.OUT);
		gpio.setup(Boardpin.PIN13_GPIO21, Direction.OUT);
		gpio.setup(Boardpin.PIN15_GPIO22, Direction.OUT);
		
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
		}else if(type != null && type.equals("TAKE_PICTURE")){
			System.out.println(takePicture());
			isSuccess = true;
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

	private String takePicture(){
		StringBuilder output = new StringBuilder();
		try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec("/opt/script/tak_picture.sh");
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            

            String line;
            while((line=input.readLine()) != null) {
                output.append(line).append("\n");
            }

            //int exitVal = pr.waitFor();
            //System.out.println("Exited with error code "+exitVal);

        } catch(Exception e) {
            //System.out.println(e.toString());
            e.printStackTrace();
        }
		
		return output.toString();
	}
	
	private void motorUp() {
		System.out.println("MOTOR MOVE UP");
		gpio.setValue(Boardpin.PIN11_GPIO17, true);
		gpio.setValue(Boardpin.PIN13_GPIO21, true);
	}

	private void motorDown() {
		System.out.println("MOTOR MOVE DOWN");
		gpio.setValue(Boardpin.PIN7_GPIO4, true);
		gpio.setValue(Boardpin.PIN15_GPIO22, true);
	}

	private void motorLeft() {
		System.out.println("MOTOR MOVE LEFT");
		gpio.setValue(Boardpin.PIN11_GPIO17, true);
		gpio.setValue(Boardpin.PIN15_GPIO22, true);
	}

	private void motorRight() {
		System.out.println("MOTOR MOVE RIGHT");
		gpio.setValue(Boardpin.PIN7_GPIO4, true);
		gpio.setValue(Boardpin.PIN13_GPIO21, true);
	}

	private void motorReset() {
		System.out.println("MOTOR MOVE RESET");
		gpio.setValue(Boardpin.PIN7_GPIO4, false);
		gpio.setValue(Boardpin.PIN11_GPIO17, false);
		gpio.setValue(Boardpin.PIN13_GPIO21, false);
		gpio.setValue(Boardpin.PIN15_GPIO22, false);
	}
	
	
	//PWM motor control
	private void motorPWMUp(int speed) {
		System.out.println("PWM MOTOR MOVE UP");
		Boardpin [] pin = new Boardpin []{Boardpin.PIN11_GPIO17, Boardpin.PIN13_GPIO21};

		motorPWMReset();
		motorPWM = new MotorPWM(gpio, pin, speed);
		pwmWorker.execute(motorPWM);
	}

	private void motorPWMDown(int speed) {
		System.out.println("PWM MOTOR MOVE DOWN");		
		Boardpin [] pin = new Boardpin []{Boardpin.PIN7_GPIO4, Boardpin.PIN15_GPIO22};

		motorPWMReset();
		motorPWM = new MotorPWM(gpio, pin, speed);
		pwmWorker.execute(motorPWM);
	}

	private void motorPWMLeft(int speed) {
		System.out.println("PWM MOTOR MOVE LEFT");		
		Boardpin [] pin = new Boardpin []{Boardpin.PIN11_GPIO17, Boardpin.PIN15_GPIO22};

		motorPWMReset();
		motorPWM = new MotorPWM(gpio, pin, speed);
		pwmWorker.execute(motorPWM);
	}

	private void motorPWMRight(int speed) {
		System.out.println("PWM MOTOR MOVE RIGHT");		
		Boardpin [] pin = new Boardpin []{Boardpin.PIN7_GPIO4, Boardpin.PIN13_GPIO21};

		motorPWMReset();
		motorPWM = new MotorPWM(gpio, pin, speed);
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
