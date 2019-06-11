package ase.sensorManager.o2SensorDataAnalyser;

public enum SafetyStatus
{
	Safe(1), Warning(2), Danger(3);
	
	public final int code;
	private SafetyStatus(int code)
	{
		this.code = code;
	}
	
}
