import java.util.concurrent.TimeUnit;

import be.doubleyouit.raspberry.gpio.Boardpin;
import be.doubleyouit.raspberry.gpio.GpioGateway;


public class MotorPWM implements Runnable{

	private GpioGateway gpio;
	private Boardpin [] pin;
	private int speed;
	
	private TimeUnit tu;
	private final double period = 100;
	private boolean isStop;
	
	public MotorPWM(GpioGateway gpio, Boardpin [] pin,  int speed){
		this.gpio = gpio;
		this.pin = pin;
		this.speed = speed;
		isStop = false;
		
		tu = TimeUnit.NANOSECONDS;  
	}
	
	
	public void run() {
		try{
			double pulse = speed;
			double calcPeriod = period - pulse;
			
			System.out.println("PWM start : " + pulse);
			while(!isStop){				
				setPin(false);
				tu.sleep((long) calcPeriod);
				setPin(true);
				tu.sleep((long) pulse);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			setPin(false);
			System.out.println("PWM finish");
		}
	}
	
	public void setPin(boolean value){
		for(int i = 0 ; i < pin.length ; i++){
			gpio.setValue(pin[i], value);
		}
	}
	
	public boolean isStop() {
		return isStop;
	}


	public void setStop() {
		this.isStop = true;
	}

}
