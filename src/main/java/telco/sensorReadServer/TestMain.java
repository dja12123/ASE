package telco.sensorReadServer;

import java.net.Socket;
import java.util.logging.Logger;

import telco.sensorReadServer.appConnect.AppDataPacketBuilder;
import telco.sensorReadServer.appConnect.Channel;
import telco.sensorReadServer.appConnect.Connection;
import telco.sensorReadServer.appConnect.ConnectionUser;
import telco.sensorReadServer.console.LogWriter;

public class TestMain
{
	public static final Logger logger = LogWriter.createLogger(TestMain.class, "main");// 메인 로거
	
	public static void main(String[] args) throws Exception
	{
		Socket socket = new Socket("127.0.0.1", 1234);
		Connection connection = new Connection(socket, new ConnectionUser()
		{
			
			@Override
			public void createChannel(Connection connection, Channel channel)
			{
				System.out.println("채널 생성");
				channel.setReceiveCallback((Channel ch, byte[][] data)->{
					System.out.println("receive: " + ch.id + " " + ch.key);
					
				});
				channel.setCloseCallback((Channel ch)->{
					System.out.println("close: " + ch.id + " " + ch.key);
				});
			}
			
			@Override
			public void closeConnection(Connection connection)
			{
				System.out.println("연결 삭제");
				
			}
		});
		if(connection.startConnection())
		{
			System.out.println("정상 연결");
			Channel c = connection.channelOpen("test");
			Channel c1 = connection.channelOpen("test1");
			AppDataPacketBuilder b = c.getPacketBuilder();
			b.appendData("Hello World!!1");
			b.appendData("Hello World!!2");
			c.sendData(b);
			Thread.sleep(200);
			connection.closeChannel(c1);
			connection.closeChannel(c);
			connection.closeConnection();
		}
		
		
		
		Thread.sleep(2000);

	}

}
