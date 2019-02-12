package telco.sensorReadServer;

import java.net.Socket;
import java.util.logging.Logger;

import telco.appConnect.AppConnectObservable;
import telco.appConnect.AppDataReceiveEvent;
import telco.appConnect.ConnectionStateChangeEvent;
import telco.appConnect.protocol.AppDataPacketBuilder;
import telco.appConnect.protocol.Channel;
import telco.appConnect.protocol.Connection;
import telco.sensorReadServer.console.LogWriter;
import telco.sensorReadServer.util.observer.Observable;
import telco.sensorReadServer.util.observer.Observer;

public class TestMain
{
	public static final Logger logger = LogWriter.createLogger(TestMain.class, "main");// 메인 로거
	
	public static void main(String[] args) throws Exception
	{
		AppConnectObservable provider = new AppConnectObservable();
		Socket socket = new Socket("127.0.0.1", 1234);
		Connection connection = new Connection(socket, provider);
		provider.addConnectionStateChangeObserver((Observable<ConnectionStateChangeEvent> object, ConnectionStateChangeEvent data)->{
			if(data.isOpen)
			{
				System.out.println("연결생성" + data.connection.getInetAddress().toString());
			}
			else
			{
				System.out.println("연결종료" + data.connection.getInetAddress().toString());
			}
		});
		Observer<AppDataReceiveEvent> ob = (Observable<AppDataReceiveEvent> object, AppDataReceiveEvent data)->{
			if(data.hasChannel)
			{
				System.out.println("채널생성" + data.connection.getInetAddress().toString() + " " + data.key + " " + data.channel.id);
			}
			else
			{
				System.out.println("데이타수신" + data.connection.getInetAddress().toString() + " " + data.key);
			}
		};
		provider.addDataReceiveObserver("test", ob);
		provider.addDataReceiveObserver("test1", ob);

		if(connection.startConnection())
		{
			System.out.println("정상 연결");
			Channel c = connection.channelOpen("test");
			Channel c1 = connection.channelOpen("test1");
			
			AppDataPacketBuilder b = new AppDataPacketBuilder();
			b.appendData("Hello World!!1");
			b.appendData("Hello World!!2");
			c.sendData(b);
			c.sendData(b);
			connection.sendData("onlyDataTest", b);
			Thread.sleep(200);
			connection.closeChannel(c1);
			connection.closeChannel(c);
			connection.closeSafe();
			
		}
		
		
		System.out.println("종료");
		Thread.sleep(2000);

	}

}
