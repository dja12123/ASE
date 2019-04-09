package ase.hardware;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import ase.ServerCore;
import ase.console.LogWriter;

public class Control
{
	public static final Logger logger = LogWriter.createLogger(Control.class, "control");
	
	private static Control inst;
	
	private GpioPinListenerDigital btnListener;
	private long pushTime;
	private boolean shutdownCommand;
	private boolean isPush;

	public static void init()
	{
		inst = new Control();
	}
	
	public static Control inst()
	{
		return inst;
	}
	
	private Control()
	{
		this.shutdownCommand = false;
		this.btnListener = this::btnListener;
		GPIOControl.inst().btn1.addListener(this.btnListener);
		logger.log(Level.INFO, "제어 활성화");
		
	}
	
	private void btnListener(GpioPinDigitalStateChangeEvent event)
	{
		if(event.getState().isHigh())
		{
			this.pushTime = System.currentTimeMillis();
			this.isPush = true;
		}
		else
		{
			long nowTime = System.currentTimeMillis();
			int btnTime = (int) (nowTime - this.pushTime);
			if(btnTime >= 2000 && btnTime <= 6000)
			{
				logger.log(Level.INFO, "종료버튼 누름1");
				if(!this.shutdownCommand && this.isPush)
				{
					logger.log(Level.INFO, "종료버튼 누름2");
					this.shutdownCommand = true;
					ServerCore.endProgram();
				}
			}
			this.isPush = false;
		}
	}
}
