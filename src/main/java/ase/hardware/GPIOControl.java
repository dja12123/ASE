package ase.hardware;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import ase.console.LogWriter;
import ase.util.observer.Observable;

public class GPIOControl
{
	public static final Logger logger = LogWriter.createLogger(GPIOControl.class, "gpio");
	
	private static GPIOControl inst;
	private final GpioController gpio;
	private GpioPinListenerDigital gpioListener;
	public final Observable<GPIOEvent> gpioEventProvider;
	
	public final GpioPinDigitalInput btn1;

	public static void init()
	{
		inst = new GPIOControl();
	}
	
	public static GPIOControl inst()
	{
		return inst;
	}
	
	private GPIOControl()
	{
		logger.log(Level.INFO, "gpio 제어기 활성화");
		this.gpioEventProvider = new Observable<>();
		this.gpio = GpioFactory.getInstance();
		this.gpioListener = this::gpioListener;
		this.btn1 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_05, "btn1", PinPullResistance.PULL_DOWN);
		this.btn1.setDebounce(1000);
		this.btn1.addListener(this.gpioListener);
		this.gpioEventProvider.addObserver((Observable<GPIOEvent> provider, GPIOEvent e)->{
			System.out.println(e.btn + " "+ e.action);
		});
	}
	
	private void gpioListener(GpioPinDigitalStateChangeEvent event)
	{
		if(event.getSource() == this.btn1)
		{
			this.gpioEventProvider.notifyObservers(new GPIOEvent(this.btn1, event.getState().isHigh()));
		}
	}
}
