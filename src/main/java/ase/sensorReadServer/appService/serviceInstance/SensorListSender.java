package ase.sensorReadServer.appService.serviceInstance;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import ase.clientSession.IChannel;
import ase.sensorReadServer.appService.AppServiceDefine;
import ase.sensorReadServer.sensorManager.SensorManager;
import ase.sensorReadServer.sensorManager.sensor.Sensor;

public class SensorListSender extends ServiceInstance
{
	public static final String KEY = "SensorListRequest";
	
	private final SensorManager sensorManager;
	
	public SensorListSender(Consumer<ServiceInstance> destoryCallback, IChannel channel, SensorManager sensorManager)
	{
		super(KEY, destoryCallback, channel);
		this.sensorManager = sensorManager;
	}
	
	@Override
	protected void onDestroy()
	{
		
	}

	@Override
	protected void onStartService()
	{
		List<Sensor> sensorList = new ArrayList<>(this.sensorManager.sensorMap.size());
		sensorList.addAll(this.sensorManager.sensorMap.values());
		ByteBuffer buf = ByteBuffer.allocate(1+4+(sensorList.size()*(4 + 1)));
		buf.put(AppServiceDefine.SensorDeviceData_REP_LIST);
		buf.putInt(sensorList.size());
		for(Sensor sensor : sensorList)
		{
			buf.putInt(sensor.id);
			buf.put((byte)(sensor.isOnline() ? 1 : 0));
		}
		this.channel.sendData(buf.array());
		this.destroy();
	}
}
