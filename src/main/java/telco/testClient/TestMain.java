package telco.testClient;

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
{
	public static final Logger logger = LogWriter.createLogger(TestMain.class, "main");// 메인 로거
	
	public static void main(String[] args) throws Exception
	{
		ClientSocketManager socket = new ClientSocketManager("192.168.0.68", 1234);
		socket.startConnection();
		Channel ch = socket.getConenction().channelOpen(AppServiceDefine.REQ_SensorData);
		ch.setReceiveCallback((Channel c, byte[][] data)->{
			
			if(data[0][0] == AppServiceDefine.SensorData_PROTO_REP_ALLDATA)
			{
				
				ByteBuffer buf = ByteBuffer.wrap(data[1]);
				int count = buf.getInt();
				System.out.println("데이타 수신" + count);
				for(int i = 0; i < count; ++i)
				{
					
					Date time;
					try
					{
						time = AppServiceDefine.DATE_FORMAT.parse(new String(data[2 + (i * 2)]));
					}
					catch (ParseException e)
					{
						e.printStackTrace();
						break;
					}
					ByteBuffer b = ByteBuffer.wrap(data[(2 + (i * 2)) + 1]);
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
			else if(data[0][0] == AppServiceDefine.SensorData_PROTO_REP_REALTIMEDATA)
			{
				Date time;
				try
				{
					time = AppServiceDefine.DATE_FORMAT.parse(new String(data[1]));
				}
				catch (ParseException e)
				{
					e.printStackTrace();
					return;
				}
				ByteBuffer b = ByteBuffer.wrap(data[2]);
				
				
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
		b.appendData(AppServiceDefine.SensorData_PROTO_REQ_DEVICEID);
		b.appendData(ProtocolDefine.intToByteArray(1001));
		ch.sendData(b);
		Thread.sleep(20000);
		socket.closeConnection();
		System.out.println("종료");
		Thread.sleep(2000);

	}

}
