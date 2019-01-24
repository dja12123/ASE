package telco.sensorReadServer.sensorReader;

public class SensorReadManager
{
	private SerialReader reader;
	
	public SensorReadManager()
	{
		// TODO Auto-generated constructor stub
	}
	
	public void startModule()
	{
		this.reader = new SerialReader();
	}
	
	public void stopModule()
	{
		
	}
}
