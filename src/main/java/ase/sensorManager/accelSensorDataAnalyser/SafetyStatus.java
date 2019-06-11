package ase.sensorManager.accelSensorDataAnalyser;

public enum SafetyStatus
{
	Safe(0), Danger(2);
	
	public final int code;
	private SafetyStatus(int code)
	{
		this.code = code;
	}
	
}
