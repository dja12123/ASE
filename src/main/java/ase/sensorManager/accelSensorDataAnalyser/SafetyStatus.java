package ase.sensorManager.accelSensorDataAnalyser;

public enum SafetyStatus
{
	Safe((byte)0), Danger((byte)2);
	
	public final byte code;
	private SafetyStatus(byte code)
	{
		this.code = code;
	}
	
}
