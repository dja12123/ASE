package telco.test;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Logger;

import telco.appConnect.channel.AppDataPacketBuilder;
import telco.appConnect.channel.Channel;
import telco.appConnect.channel.ProtocolDefine;
import telco.console.LogWriter;
import telco.sensorReadServer.appService.AppServiceDefine;

public class TestMain
{//testtest
	public static final Logger logger = LogWriter.createLogger(TestMain.class, "main");// 메인 로거
	
	public static void main(String[] args) throws Exception
	{
		ClientSocketManager socket = new ClientSocketManager("192.168.0.68", 1234);
		socket.startConnection();
		Channel ch = socket.getConenction().channelOpen(AppServiceDefine.CHKEY_SensorData);
		ch.setReceiveCallback((Channel c, byte[][] data)->{
			
			if(data[0][0] == AppServiceDefine.SensorData_REP_ALLDATA)
			{
				
				ByteBuffer buf = ByteBuffer.wrap(data[1]);
				int count = buf.getInt();
				System.out.println("데이타 수신" + count);
				byte[] timeBuf = new byte[AppServiceDefine.DATE_FORMAT_SIZE];
				for(int i = 0; i < count; ++i)
				{
					ByteBuffer b = ByteBuffer.wrap(data[2 + i]);
					b.get(timeBuf);
					Date time;
					try
					{
						time = AppServiceDefine.DATE_FORMAT.parse(new String(timeBuf));
					}
					catch (ParseException e)
					{
						e.printStackTrace();
						break;
					}
					
					float xg = b.getFloat();
					float yg = b.getFloat();
					
					float xa = b.getFloat();
					float ya = b.getFloat();
					float za = b.getFloat();
					
					float al = b.getFloat();
							
					StringBuffer strbuf = new StringBuffer();
					strbuf.append(time.toString()); strbuf.append(' ');
					strbuf.append(xg); strbuf.append(' ');
					strbuf.append(yg); strbuf.append(' ');
					strbuf.append(xa); strbuf.append(' ');
					strbuf.append(ya); strbuf.append(' ');
					strbuf.append(za); strbuf.append(' ');
					strbuf.append(al);
					System.out.println(strbuf.toString());
				}
			}
			else if(data[0][0] == AppServiceDefine.SensorData_REP_REALTIMEDATA)
			{
				ByteBuffer b = ByteBuffer.wrap(data[1]);
				byte[] timeBuf = new byte[AppServiceDefine.DATE_FORMAT_SIZE];
				b.get(timeBuf);
				Date time;
				try
				{
					time = AppServiceDefine.DATE_FORMAT.parse(new String(timeBuf));
				}
				catch (ParseException e)
				{
					e.printStackTrace();
					return;
				}
				
				float xg = b.getFloat();
				float yg = b.getFloat();
				
				float xa = b.getFloat();
				float ya = b.getFloat();
				float za = b.getFloat();
				
				float al = b.getFloat();
				
				StringBuffer strbuf = new StringBuffer();
				strbuf.append(time.toString()); strbuf.append(' ');
				strbuf.append(xg); strbuf.append(' ');
				strbuf.append(yg); strbuf.append(' ');
				strbuf.append(xa); strbuf.append(' ');
				strbuf.append(ya); strbuf.append(' ');
				strbuf.append(za); strbuf.append(' ');
				strbuf.append(al);
				System.out.println(strbuf.toString());
			}
		});
		AppDataPacketBuilder b = new AppDataPacketBuilder();
		b.appendData(AppServiceDefine.SensorData_REQ_DEVICEID);
		b.appendData(ProtocolDefine.intToByteArray(1001));
		ch.sendData(b);
		
		b = new AppDataPacketBuilder();
		b.appendData(AppServiceDefine.SensorData_REQ_ALLDATA);
		ch.sendData(b);
		
		Channel ch1 = socket.getConenction().channelOpen(AppServiceDefine.CHKEY_SensorDeviceData);
		ch1.setReceiveCallback((Channel c, byte[][] data)->{
			if(data[0][0] == AppServiceDefine.SensorDeviceData_REP_LIST)
			{
				ByteBuffer buf = ByteBuffer.wrap(data[1]);
				int size = buf.getInt();
				for(int i = 0; i < size; ++i)
				{
					ByteBuffer buf1 = ByteBuffer.wrap(data[i + 2]);
					int id = buf1.getInt();
					boolean isActive = buf1.get() == 1 ? true : false;
					
					System.out.println("센서"+id+" 활성여부:"+isActive);
				}
			}
			
			if(data[0][0] == AppServiceDefine.SensorDeviceData_REP_REALTIMEDATA_ADDREMOVE)
			{
				ByteBuffer buf = ByteBuffer.wrap(data[1]);
				int id = buf.getInt();
				boolean isAdd = data[2][0] == 1 ? true : false;
				System.out.println("센서"+id+" 추가됨여부"+isAdd);
			}
			if(data[0][0] == AppServiceDefine.SensorDeviceData_REP_REALTIMEDATA_ONOFF)
			{
				ByteBuffer buf = ByteBuffer.wrap(data[1]);
				int id = buf.getInt();
				boolean isOnline = data[2][0] == 1 ? true : false;
				System.out.println("센서"+id+" 활성 이벤트"+isOnline);
			}
		
			
		});
		b = new AppDataPacketBuilder();
		b.appendData(AppServiceDefine.SensorDeviceData_REQ_LIST);
		ch1.sendData(b);
		Thread.sleep(2000000);
		socket.closeConnection();
		System.out.println("종료");
		Thread.sleep(2000);
	}

}
