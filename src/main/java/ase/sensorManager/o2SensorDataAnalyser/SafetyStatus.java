package ase.sensorManager.o2SensorDataAnalyser;

public enum SafetyStatus
{
	Safe(0), Warning(1), Danger(2);
	
	public final int code;
	private SafetyStatus(int code)
	{
		this.code = code;
	}
	
}
