package ase.hardware;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;

import ase.console.LogWriter;

public class GPIOControl
{
	public static final Logger logger = LogWriter.createLogger(GPIOControl.class, "gpio");
	
	private static GPIOControl inst;
	private final GpioController gpio;

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
		this.gpio = GpioFactory.getInstance();
		this.btn1 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_05, "btn1", PinPullResistance.PULL_DOWN);
		this.btn1.setDebounce(100);
	}
}
