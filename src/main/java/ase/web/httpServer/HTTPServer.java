package ase.web.httpServer;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
import org.nanohttpd.util.ServerRunner;

import ase.fileIO.FileHandler;

public class HTTPServer extends NanoHTTPD
{
	// private static final int MAXIMUM_SIZE_OF_IMAGE = 1000000;
	public static final String rootDirectory = FileHandler.getExtResourceFile("www").toString();
	
	//private static WebSocketManager responseSocketHandler;
	public static final String WEB_RES_DIR = "/www";
	

	
	public HTTPServer(int port)
	{
		super(port);
		//responseSocketHandler = new WebSocketManager(8080, true); //소켓
	}
//
	/*
	public static void main(String[] args)
	{
		WebServiceMain main = new WebServiceMain();
		main.startModule();
	}
	*/

	private static Response serveImage(MIME_TYPE imageType, String dir)
	{
		String imageTypeStr = imageType.toString();
		
		return Response.newFixedLengthResponse(Status.OK, imageTypeStr, FileHandler.getResInputStream(dir), -1);
	}
	
	private static Response serveStrFile(MIME_TYPE mimeType, String dir)
	{
		return Response.newFixedLengthResponse(Status.OK, mimeType.toString(), FileHandler.getResInputStream(dir), -1);
	}
	
	private static Response serveError(Status status, String errorStr)
	{
		return Response.newFixedLengthResponse(status, errorStr, "");
	}

	@Override
	public Response serve(IHTTPSession session)
	{
		Method method = session.getMethod();
		String uri = session.getUri();

		//responseSocketHandler.openWebSocket(session); //소켓 세션
		
		System.out.println(method + " '" + uri + "' ");

		// 웹서비스 할 때 필요한 파일 스트림 모듈로 만들기(fileIO 패키지)
		// StringBuffer 적극 사용
		// url이용해서 어떤 요청인지 구분 ->
		// refer::
		// https://github.com/Teaonly/android-eye/blob/master/src/teaonly/droideye/TeaServer.java
		
		//System.out.println("root >> " + rootDirectory);
		//
		String msg = "";
		
		if (uri.startsWith("/"))
		{ // Root Mapping
			String dir = WEB_RES_DIR+uri;
			if(!FileHandler.isExistResFile(dir))
			{
				return HTTPServer.serveError(Status.NOT_FOUND, "Error 404: File not found");
			}
			
			int pos = uri.lastIndexOf( "." );
			
			if(pos == -1)
			{
				return HTTPServer.serveError(Status.BAD_REQUEST, "Error 400: Bad Request");
			}
			String ext = uri.substring( pos + 1 );
			
			switch(ext)
			{
			case "html":
				return serveStrFile(MIME_TYPE.MIME_HTML, dir);
			case "jpg":
				return HTTPServer.serveImage(MIME_TYPE.MIME_JPEG, dir);
			case "png":
				return HTTPServer.serveImage(MIME_TYPE.MIME_PNG, dir);
			case "js":
				return serveStrFile(MIME_TYPE.MIME_JS,  dir);
			case "css":
				return serveStrFile(MIME_TYPE.MIME_CSS,  dir);
			default:
				return serveStrFile(MIME_TYPE.MIME_PLAINTEXT,  dir);
				
			}
		}

		return Response.newFixedLengthResponse(msg);
	}

	@Override
	public void start()
	{
		ServerRunner.executeInstance(this);
	}


}
