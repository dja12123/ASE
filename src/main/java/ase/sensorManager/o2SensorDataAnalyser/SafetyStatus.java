package ase.sensorManager.o2SensorDataAnalyser;

public enum SafetyStatus
{
	Safe((byte)1), Warning((byte)2), Danger((byte)3);
	
	public final byte code;
	private SafetyStatus(byte code)
	{
		this.code = code;
	}
	
}
