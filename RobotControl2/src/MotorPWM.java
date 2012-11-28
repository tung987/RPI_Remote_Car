import java.util.concurrent.TimeUnit;

import com.pi4j.io.gpio.GpioPinDigitalOutput;


public class MotorPWM implements Runnable{

	private GpioPinDigitalOutput [] pin;
	private int speed;
	
	private TimeUnit tu;
	private final double period = 10; //100Hz
	private boolean isStop;
	
	public MotorPWM(GpioPinDigitalOutput [] pin,  int speed){
		this.pin = pin;
		this.speed = speed;
		isStop = false;
		
		tu = TimeUnit.MILLISECONDS;  
	}
	
	
	public void run() {
		if(speed == 100){
			setPin(true);
			return;
		}else if(speed == 0){
			setPin(false);
			return;
		}
		
		try{
			double pulse = (speed / 100) * period;
			double waitTime = period - pulse;
			
			System.out.println("PWM start : pulse = " + pulse + ", waitTime = " + waitTime);
			while(!isStop){				
				setPin(false);
				tu.sleep((long) waitTime);
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
			if(value){
				pin[i].high();
			}else{
				pin[i].low();
			}
		}
	}
	
	public boolean isStop() {
		return isStop;
	}


	public void setStop() {
		if(speed == 100 || speed == 0){
			setPin(false);
		}else{
			this.isStop = true;
		}
	}

}
