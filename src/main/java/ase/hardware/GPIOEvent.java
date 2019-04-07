package ase.hardware;

import com.pi4j.io.gpio.GpioPinDigitalInput;

public class GPIOEvent
{
	public final GpioPinDigitalInput btn;
	public final boolean action;
	
	public GPIOEvent(GpioPinDigitalInput btn, boolean action)
	{
		this.btn = btn;
		this.action = action;
	}
}
