package telco.testClient;

import java.nio.ByteBuffer;
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
				for(int i = 0; i < count; ++i)
				{
					ByteBuffer b = ByteBuffer.wrap(data[i + 2]);
					long time = b.getLong();
					float xg = b.getFloat();
					float yg = b.getFloat();
					
					float xa = b.getFloat();
					float ya = b.getFloat();
					float za = b.getFloat();
					
					float al = b.getFloat();
							
					StringBuffer strbuf = new StringBuffer();
					strbuf.append(new Date(time).toString()); strbuf.append(' ');
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
				
			}
		});
		AppDataPacketBuilder b = new AppDataPacketBuilder();
		b.appendData(AppServiceDefine.SensorData_PROTO_REQ_DEVICEID);
		b.appendData(ProtocolDefine.intToByteArray(1001));
		ch.sendData(b);
		System.out.println("종료");
		Thread.sleep(2000);

	}

}